/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.core.p4java.synchronize.PerforceSyncFile;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.core.p4java.synchronize.PerforceSyncFile.VariantType;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.DeleteAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.RemoveAction;
import com.perforce.team.ui.p4java.actions.SyncAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceSubscriberTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        addFile(client, project.getFile("Jamfile"), new ByteArrayInputStream(
                "test".getBytes()));
        addFile(client, project.getFile("p4eclipse.rc"),
                new ByteArrayInputStream("test".getBytes()));
        for (int i = 0; i < 3; i++) {
            addFile(client, project.getFile("Makefile"),
                    new ByteArrayInputStream("test".getBytes()));
            addFile(client, project.getFile(new Path("Debug/makefile")),
                    new ByteArrayInputStream("test".getBytes()));
            addFile(client, project.getFile(".cproject"),
                    new ByteArrayInputStream("test".getBytes()));
        }

        addFile(client, project.getFile(new Path("src/Client.cpp")));
        deleteFile(client, project.getFile(new Path("src/Client.cpp")));

        addFile(client, project.getFile("Makefile.bat"),
                new ByteArrayInputStream("test".getBytes()));
        deleteFile(client, project.getFile("Makefile.bat"));

        addDepotFile(client,
                "//depot/p07.2/p4-eclipse/native/vstudio/p4api/p4api.sln",
                new ByteArrayInputStream("test".getBytes()));
        addDepotFile(
                client,
                "//depot/p07.2/p4-eclipse/native/vstudio/p4api/p4api/Debug/BuildLog.htm",
                new ByteArrayInputStream("test".getBytes()));
    }

    private void checkVariants(IResourceVariant base, IResourceVariant remote) {
        if (remote != null) {
            assertNotNull(remote.getName());
            assertFalse(remote.isContainer());
            byte[] remoteBytes = remote.asBytes();
            assertNotNull(remoteBytes);
            assertTrue(remoteBytes.length > 0);
            IStorage storage;
            try {
                storage = remote.getStorage(null);
                assertNotNull(storage);
                assertNotNull(storage.getContents());
                assertTrue(storage.getContents().available() > 0);
                assertNotNull(storage.getFullPath());
                assertNotNull(storage.getName());
            } catch (TeamException e) {
                assertFalse("Team exception thrown", true);
            } catch (CoreException e) {
                assertFalse("Core exception thrown", true);
            } catch (IOException e) {
                assertFalse("IO exception thrown", true);
            }
        }

        if (base != null) {
            assertNotNull(base.getName());
            assertFalse(base.isContainer());
            byte[] baseBytes = base.asBytes();
            assertNotNull(baseBytes);
            assertTrue(baseBytes.length > 0);
            IStorage storage;
            try {
                storage = base.getStorage(null);
                assertNotNull(storage);
                assertNotNull(storage.getContents());
                assertTrue(storage.getContents().available() > 0);
                assertNotNull(storage.getFullPath());
                assertNotNull(storage.getName());
            } catch (TeamException e) {
                assertFalse("Team exception thrown", true);
            } catch (CoreException e) {
                assertFalse("Core exception thrown", true);
            } catch (IOException e) {
                assertFalse("IO exception thrown", true);
            }
        }
    }

    private void checkProject() {
        try {
            assertTrue(PerforceSubscriber.getSubscriber().isSupervised(project));
            IResource[] members = PerforceSubscriber.getSubscriber().members(
                    project);
            assertNotNull(members);
            assertTrue(members.length > 0);
        } catch (TeamException e1) {
            assertFalse("Team exception thrown", false);
        }
    }

    /**
     * Basic test of variant types used in {@link PerforceSyncFile}
     */
    public void testVariantTypes() {
        VariantType[] types = VariantType.values();
        assertNotNull(types);
        assertTrue(types.length > 0);
        for (VariantType type : types) {
            assertNotNull(type);
            assertNotNull(type.toString());
            assertEquals(type, VariantType.valueOf(type.toString()));
        }
    }

    /**
     * Test outgoing add
     */
    public void testOutgoingAdd() {
        checkProject();
        IFile addFile = project.getFile("newFileToAdd.txt");
        assertFalse(addFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            addFile.create(fileUrl.openStream(), true, null);
            assertTrue(addFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(addFile));
        add.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(addFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertTrue(p4File.openedForAdd());

        try {
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { addFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    addFile);
            assertNotNull(info);
            assertEquals(SyncInfo.OUTGOING | SyncInfo.ADDITION, info.getKind());
            assertEquals(addFile, info.getLocal());
            assertNull(info.getRemote());
            assertNull(info.getBase());
            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Test outgoing edit
     */
    public void testOutgoingEdit() {
        checkProject();
        IFile editFile = project.getFile("Makefile");
        assertTrue(editFile.exists());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(editFile));
        edit.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(editFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertTrue(p4File.openedForEdit());

        try {
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { editFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    editFile);
            assertNotNull(info);
            assertEquals(SyncInfo.OUTGOING | SyncInfo.CHANGE, info.getKind());
            assertEquals(editFile, info.getLocal());
            assertNotNull(info.getRemote());
            assertNotNull(info.getBase());
            checkVariants(info.getBase(), info.getRemote());
            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Test outgoing delete
     */
    public void testOutgoingDelete() {
        checkProject();
        IFile deleteFile = project.getFile("Jamfile");
        assertTrue(deleteFile.exists());

        DeleteAction delete = new DeleteAction();
        delete.setAsync(false);
        delete.selectionChanged(null, new StructuredSelection(deleteFile));
        delete.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(
                deleteFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertTrue(p4File.openedForDelete());

        try {
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { deleteFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    deleteFile);
            assertNotNull(info);
            assertEquals(SyncInfo.OUTGOING | SyncInfo.DELETION, info.getKind());
            assertEquals(deleteFile, info.getLocal());

            assertNotNull(info.getBase());
            assertNotNull(info.getRemote());
            checkVariants(info.getBase(), info.getRemote());

            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Tests an incoming addition
     */
    public void testIncomingAdd() {
        checkProject();
        IFile removedFile = project.getFile("Jamfile");
        assertTrue(removedFile.exists());

        RemoveAction remove = new RemoveAction();
        remove.setAsync(false);
        RemoveAction.setNeedConfirm(false);
        remove.selectionChanged(null, new StructuredSelection(removedFile));
        remove.run(null);

        assertFalse(removedFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(
                removedFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertEquals(0, p4File.getHaveRevision());

        try {
            PerforceSubscriber.getSubscriber().remove(removedFile);
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { removedFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    removedFile);
            assertNotNull(info);
            assertEquals(SyncInfo.INCOMING | SyncInfo.ADDITION, info.getKind());
            assertEquals(removedFile, info.getLocal());
            assertNotNull(info.getRemote());
            assertNull(info.getBase());
            checkVariants(info.getBase(), info.getRemote());
            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Tests an incoming edit
     */
    public void testIncomingEdit() {
        checkProject();
        IFile oldFile = project.getFile(new Path("Debug/makefile"));
        assertTrue(oldFile.exists());

        SyncRevisionAction getOldRevision = new SyncRevisionAction();
        getOldRevision.setAsync(false);
        getOldRevision.selectionChanged(null, new StructuredSelection(oldFile));
        getOldRevision.runAction("#1");

        IP4Resource resource = P4Workspace.getWorkspace().getResource(oldFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertEquals(1, p4File.getHaveRevision());

        try {
            PerforceSubscriber.getSubscriber().remove(oldFile);
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { oldFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    oldFile);
            assertNotNull(info);
            assertEquals(SyncInfo.INCOMING | SyncInfo.CHANGE, info.getKind());
            assertEquals(oldFile, info.getLocal());
            assertNotNull(info.getRemote());
            assertNotNull(info.getBase());
            checkVariants(info.getBase(), info.getRemote());
            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Test incoming outside edit (job032095)
     */
    public void testIncomingOutsideEdit() {
        checkProject();
        IFile oldFile = project.getFile(new Path("Debug/makefile"));
        assertTrue(oldFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(oldFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        p4File.refresh();
        assertTrue(p4File.getHaveRevision() > 1);

        IP4Connection connection = createConnection();
        List<IFileSpec> specs = P4FileSpecBuilder
                .makeFileSpecList(new String[] { oldFile.getLocation()
                        .makeAbsolute().toOSString()
                        + "#1" });
        try {
            connection.getClient().sync(specs, false, false, false, false);
        } catch (P4JavaException e1) {
            assertFalse("P4J exception thrown", true);
        }

        try {
            PerforceSubscriber.getSubscriber().remove(oldFile);
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { oldFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    oldFile);
            assertNotNull(info);
            assertEquals(SyncInfo.INCOMING | SyncInfo.CHANGE, info.getKind());
            assertEquals(oldFile, info.getLocal());
            assertNotNull(info.getRemote());
            assertNotNull(info.getBase());
            checkVariants(info.getBase(), info.getRemote());
            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;

            assertEquals(1, p4File.getHaveRevision());
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Tests an incoming delete
     */
    public void testIncomingDelete() {
        checkProject();
        IFile oldFile = project.getFile(new Path("Makefile.bat"));
        assertFalse(oldFile.exists());

        SyncRevisionAction getOldRevision = new SyncRevisionAction();
        getOldRevision.setAsync(false);
        getOldRevision.selectionChanged(null, new StructuredSelection(oldFile));
        getOldRevision.runAction("#1");

        assertTrue(oldFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(oldFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertEquals(1, p4File.getHaveRevision());

        try {
            PerforceSubscriber.getSubscriber().remove(oldFile);
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { oldFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    oldFile);
            assertNotNull(info);
            assertEquals(SyncInfo.INCOMING | SyncInfo.DELETION, info.getKind());
            assertEquals(oldFile, info.getLocal());
            assertNull(info.getRemote());
            assertNotNull(info.getBase());
            checkVariants(info.getBase(), info.getRemote());
            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Tests a resource in sync
     */
    public void testInSync() {
        checkProject();
        IFile editFile = project.getFile("p4eclipse.rc");
        assertTrue(editFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(editFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertTrue(p4File.isSynced());

        try {
            PerforceSubscriber.getSubscriber().remove(editFile);
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { editFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    editFile);
            assertNull(info);

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Test for job032976, file exists locally, not opened for add, and head
     * revision deleted and have revision is 0
     */
    public void testHeadRevisionDelete() {
        checkProject();
        IFile deleted = project.getFile(new Path("src/Client.cpp"));
        assertFalse(deleted.exists());
        try {
            Utils.fillFile(deleted);
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        }
        assertTrue(deleted.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(deleted);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertEquals(0, p4File.getHaveRevision());
        assertEquals(FileAction.DELETE, p4File.getHeadAction());
        assertNotNull(p4File.getRemotePath());
        assertFalse(p4File.isOpened());

        try {
            PerforceSubscriber.getSubscriber().remove(deleted);
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { deleted }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    deleted);
            assertNull(info);

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Tests conflict
     */
    public void testConflict() {
        checkProject();
        IFile oldFile = project.getFile("Makefile");
        assertTrue(oldFile.exists());

        SyncRevisionAction getOldRevision = new SyncRevisionAction();
        getOldRevision.setAsync(false);
        getOldRevision.selectionChanged(null, new StructuredSelection(oldFile));
        getOldRevision.runAction("#2");

        IP4Resource resource = P4Workspace.getWorkspace().getResource(oldFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertEquals(2, p4File.getHaveRevision());

        p4File.edit();
        p4File.refresh();

        try {
            PerforceSubscriber.getSubscriber().remove(oldFile);
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { oldFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    oldFile);
            assertNotNull(info);
            assertEquals(SyncInfo.CONFLICTING | SyncInfo.CHANGE, info.getKind());
            assertEquals(oldFile, info.getLocal());
            assertNotNull(info.getRemote());
            assertNotNull(info.getBase());
            checkVariants(info.getBase(), info.getRemote());
            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Test the subscriber resource comparator
     */
    public void testComparator() {
        IFile file = project.getFile("Makefile");
        assertTrue(file.exists());
        PerforceSubscriber.getSubscriber().remove(file);
        IResourceVariantComparator comparator = PerforceSubscriber
                .getSubscriber().getResourceComparator();
        assertNotNull(comparator);
        assertTrue(comparator.isThreeWay());
        IResourceVariant variant = new IResourceVariant() {

            public boolean isContainer() {
                return false;
            }

            public IStorage getStorage(IProgressMonitor monitor)
                    throws TeamException {
                return null;
            }

            public String getName() {
                return null;
            }

            public String getContentIdentifier() {
                return null;
            }

            public byte[] asBytes() {
                return null;
            }

        };
        assertTrue(comparator.compare(variant, variant));
        assertTrue(comparator.compare((IResourceVariant) null,
                (IResourceVariant) null));
        assertFalse(comparator.compare(variant, null));
        assertFalse(comparator.compare((IResourceVariant) null, variant));
        assertFalse(comparator.compare(file, variant));
    }

    /**
     * Test the members method on the Perforce subscriber
     */
    public void testMembers() {
        PerforceSubscriber subscriber = PerforceSubscriber.getSubscriber();
        assertNotNull(subscriber);
        IFile file = project.getFile(new Path("Debug/makefile"));
        assertTrue(file.exists());
        try {
            assertNotNull(subscriber.members(file));
            assertEquals(0, subscriber.members(file).length);
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Test the members method on the PerforceSubscribe with a container that
     * will thrown an exception when processed
     */
    public void testMembersFailure() {
        PerforceSubscriber subscriber = PerforceSubscriber.getSubscriber();
        assertNotNull(subscriber);
        IContainer container = project.getFolder("invalid_folder_path");
        assertFalse(container.exists());
        try {
            assertNotNull(subscriber.members(container));
            assertEquals(0, subscriber.members(container).length);
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Test unresolve file
     */
    public void testOutgoingResolve() {
        checkProject();
        IFile resolveFile = project.getFile(".cproject");
        assertTrue(resolveFile.exists());

        SyncRevisionAction getOldRevision = new SyncRevisionAction();
        getOldRevision.setAsync(false);
        getOldRevision.selectionChanged(null, new StructuredSelection(
                resolveFile));
        getOldRevision.runAction("#1");

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(resolveFile));
        edit.run(null);

        SyncAction sync = new SyncAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(resolveFile));
        sync.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(
                resolveFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertTrue(p4File.openedForEdit());
        assertTrue(p4File.isUnresolved());

        try {
            PerforceSubscriber.getSubscriber().remove(resolveFile);
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { resolveFile }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                    resolveFile);
            assertNotNull(info);
            assertEquals(SyncInfo.OUTGOING | SyncInfo.CHANGE
                    | SyncInfo.CONFLICTING, info.getKind());
            assertEquals(resolveFile, info.getLocal());
            assertNotNull(info.getRemote());
            assertNotNull(info.getBase());
            checkVariants(info.getBase(), info.getRemote());
            assertTrue(info instanceof PerforceSyncInfo);
            PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
            assertEquals(p4File, pInfo.getP4File());

        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    /**
     * Test empty sync file with no input stream
     */
    public void testEmptySyncFile() {
        IFile file = project.getFile(".cproject");
        IP4File p4File = new P4File(createConnection(), file.getLocation()
                .makeAbsolute().toOSString()) {

            @Override
            public InputStream getRemoteContents(int revision) {
                return null;
            }

        };
        PerforceSyncFile syncFile = new PerforceSyncFile(p4File,
                PerforceSyncFile.VariantType.BASE);
        assertNotNull(syncFile.getName());
        assertFalse(syncFile.isContainer());
        byte[] baseBytes = syncFile.asBytes();
        assertNotNull(baseBytes);
        assertTrue(baseBytes.length > 0);
        IStorage storage;
        try {
            storage = syncFile.getStorage(null);
            assertNotNull(storage);
            assertNotNull(storage.getContents());
            assertTrue(storage.getContents().available() == 0);
            assertNotNull(storage.getFullPath());
            assertNotNull(storage.getName());
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
    }

    /**
     * Test null
     */
    public void testNull() {
        try {
            PerforceSubscriber.getSubscriber().getSyncInfo(null);
        } catch (Throwable e) {
            assertFalse("Throwable thrown: " + e.getMessage(), true);
        }
    }

    /**
     * Test a linked folder with files opened for edit showing up in the
     * Synchronize view
     */
    public void testLinkedFolderEdit() {
        String linkedPath = "//depot/p07.2/p4-eclipse/native/vstudio/p4api/p4api/Debug";

        IP4Connection connection = createConnection();
        IP4Folder folder = connection.getFolder(linkedPath, true);
        assertNotNull(folder);
        new P4Collection(new IP4Resource[] { folder }).sync(true, false, null,new NullProgressMonitor(), null);
        folder.updateLocation();
        assertNotNull(folder.getLocalPath());

        checkProject();
        IFolder linkedFolder = project.getFolder("Linked_folder_1");
        assertFalse(linkedFolder.exists());
        assertFalse(linkedFolder.isLinked());
        IPath folderPath = new Path(folder.getLocalPath());
        try {
            linkedFolder.createLink(folderPath, 0, null);
        } catch (CoreException e) {
            assertTrue("Linking folder failed:" + e.getMessage(), false);
        }

        IFile editFile = linkedFolder.getFile("BuildLog.htm");
        assertTrue(editFile.exists());
        assertFalse(editFile.isLinked());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(editFile));
        edit.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(editFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        try {

            assertTrue(p4File.openedForEdit());

            try {
                // Test refresh on project
                PerforceSubscriber.getSubscriber().remove(editFile);
                PerforceSubscriber.getSubscriber().refresh(
                        new IResource[] { project }, 0, null);
                SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                        editFile);
                assertNotNull(info);
                assertEquals(SyncInfo.OUTGOING | SyncInfo.CHANGE,
                        info.getKind());
                assertEquals(editFile, info.getLocal());
                assertNotNull(info.getRemote());
                assertNotNull(info.getBase());
                checkVariants(info.getBase(), info.getRemote());
                assertTrue(info instanceof PerforceSyncInfo);
                PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
                assertEquals(p4File, pInfo.getP4File());

                // Test refresh on folder
                PerforceSubscriber.getSubscriber().remove(editFile);
                PerforceSubscriber.getSubscriber().refresh(
                        new IResource[] { linkedFolder }, 0, null);
                info = PerforceSubscriber.getSubscriber().getSyncInfo(editFile);
                assertNotNull(info);
                assertEquals(SyncInfo.OUTGOING | SyncInfo.CHANGE,
                        info.getKind());
                assertEquals(editFile, info.getLocal());
                assertNotNull(info.getRemote());
                assertNotNull(info.getBase());
                checkVariants(info.getBase(), info.getRemote());
                assertTrue(info instanceof PerforceSyncInfo);
                pInfo = (PerforceSyncInfo) info;
                assertEquals(p4File, pInfo.getP4File());

                // Test refresh on file
                PerforceSubscriber.getSubscriber().remove(editFile);
                PerforceSubscriber.getSubscriber().refresh(
                        new IResource[] { editFile }, 0, null);
                info = PerforceSubscriber.getSubscriber().getSyncInfo(editFile);
                assertNotNull(info);
                assertEquals(SyncInfo.OUTGOING | SyncInfo.CHANGE,
                        info.getKind());
                assertEquals(editFile, info.getLocal());
                assertNotNull(info.getRemote());
                assertNotNull(info.getBase());
                checkVariants(info.getBase(), info.getRemote());
                assertTrue(info instanceof PerforceSyncInfo);
                pInfo = (PerforceSyncInfo) info;
                assertEquals(p4File, pInfo.getP4File());

            } catch (TeamException e) {
                assertFalse("Team exception thrown", true);
            }

        } finally {
            p4File.revert();
        }

    }

    /**
     * Test a linked file opened for edit showing up in the Synchronize view
     */
    public void testLinkedFileEdit() {
        String linkedPath = "//depot/p07.2/p4-eclipse/native/vstudio/p4api/p4api.sln";

        FileSpec spec = new FileSpec();
        spec.setDepotPath(linkedPath);
        IP4Connection connection = createConnection();
        IP4File depotFile = connection.getFile(spec);
        assertNotNull(depotFile);
        depotFile.refresh();
        assertNotNull(depotFile.getLocalPath());
        new P4Collection(new IP4Resource[] { depotFile }).sync(true, false,
                null,new NullProgressMonitor(), null);
        assertTrue(depotFile.isSynced());

        checkProject();
        IFile editFile = project.getFile("A_Linked_File.txt");
        assertFalse(editFile.exists());
        assertFalse(editFile.isLinked());
        IPath filePath = new Path(depotFile.getLocalPath());
        try {
            editFile.createLink(filePath, 0, null);
        } catch (CoreException e1) {
            assertTrue("Linking failed: " + e1.getMessage(), false);
        }
        assertTrue(editFile.exists());
        assertTrue(editFile.isLinked());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(editFile));
        edit.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(editFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        try {

            assertTrue(p4File.openedForEdit());

            try {
                // Test refresh on project
                PerforceSubscriber.getSubscriber().remove(editFile);
                PerforceSubscriber.getSubscriber().refresh(
                        new IResource[] { project }, 0, null);
                SyncInfo info = PerforceSubscriber.getSubscriber().getSyncInfo(
                        editFile);
                assertNotNull(info);
                assertEquals(SyncInfo.OUTGOING | SyncInfo.CHANGE,
                        info.getKind());
                assertEquals(editFile, info.getLocal());
                assertNotNull(info.getRemote());
                assertNotNull(info.getBase());
                checkVariants(info.getBase(), info.getRemote());
                assertTrue(info instanceof PerforceSyncInfo);
                PerforceSyncInfo pInfo = (PerforceSyncInfo) info;
                assertEquals(p4File, pInfo.getP4File());

                // Test refresh on file
                PerforceSubscriber.getSubscriber().remove(editFile);
                PerforceSubscriber.getSubscriber().refresh(
                        new IResource[] { editFile }, 0, null);
                info = PerforceSubscriber.getSubscriber().getSyncInfo(editFile);
                assertNotNull(info);
                assertEquals(SyncInfo.OUTGOING | SyncInfo.CHANGE,
                        info.getKind());
                assertEquals(editFile, info.getLocal());
                assertNotNull(info.getRemote());
                assertNotNull(info.getBase());
                checkVariants(info.getBase(), info.getRemote());
                assertTrue(info instanceof PerforceSyncInfo);
                pInfo = (PerforceSyncInfo) info;
                assertEquals(p4File, pInfo.getP4File());

            } catch (TeamException e) {
                assertFalse("Team exception thrown", true);
            }

        } finally {
            p4File.revert();
        }

    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/native/com.perforce.p4api.cnative";
    }

}
