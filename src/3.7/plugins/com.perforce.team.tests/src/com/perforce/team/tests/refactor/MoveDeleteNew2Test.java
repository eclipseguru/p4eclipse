/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.refactor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.P4JavaSysFileCommandsHelper;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.ImportProjectAction;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveProjectOperation;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class MoveDeleteNew2Test extends ProjectBasedTestCase {

    private IProject otherProject = null;

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_REFACTOR_USE_MOVE, true);
        // Reconnect to each so that p4jserver is updated
        for (IP4Connection connection : P4Workspace.getWorkspace()
                .getConnections()) {
            connection.refreshServer();
            connection.connect();
        }

        IClient client = createConnection().getClient();

        for (int i = 0; i < 5; i++) {
            addFile(client,
                    project.getFile(new Path("images/test" + i + ".gif")));
        }
        addFile(client, project.getFile(new Path("META-INF/MANIFEST.MF")));
        addFile(client, project.getFile("p4eclipse.properties"));
        addFile(client, project.getFile("plugin.xml"));
        addFile(client, project.getFile(new Path("src/com/perforce/test.txt")));
        addDepotFile(client,
                "//depot/p08.1/p4-eclipse/com.perforce.team.ui/src/com/perforce/test.txt");
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        try {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_USE_MOVE, false);
            if (otherProject != null) {
                try {
                    otherProject.refreshLocal(IResource.DEPTH_INFINITE, null);
                    otherProject.accept(new IResourceVisitor() {

                        public boolean visit(IResource resource)
                                throws CoreException {
                            ResourceAttributes attrs = resource
                                    .getResourceAttributes();
                            if (attrs != null) {
                                attrs.setReadOnly(false);
                                try {
                                    resource.setResourceAttributes(attrs);
                                } catch (CoreException e) {
                                }
                            }
                            return true;
                        }
                    });
                    otherProject.delete(true, true, null);
                } catch (CoreException e) {
                    handle(e);
                }
                assertFalse(otherProject.exists());
            }
        } finally {
            super.tearDown();
        }
    }

    /**
     * Test moving a folder to an existing folder
     */
    public void testMoveFolderExisting() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("images");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        int fromSize = fromFolder.members().length;

        IFolder folderTo = project.getFolder("META-INF");
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        IP4Resource[] fromFiles = fromFolder.members();
        assertNotNull(fromFiles);
        assertTrue(fromFiles.length > 0);

        int deleteSize = check(FileAction.MOVE_DELETE, fromFolder);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        newFolder.refresh();
        IP4Resource[] toFiles = newFolder.members();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.MOVE_ADD, newFolder);
        assertEquals(fromSize, toSize);
    }

    /**
     * Test moving a folder to a new folder
     */
    public void testMoveFolderNew() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("images");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        int fromSize = fromFolder.members().length;

        IFolder folderTo = project.getFolder("images2");
        assertFalse(folderTo.exists());
        try {
            folderTo.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        IP4Resource[] fromFiles = fromFolder.members();
        assertNotNull(fromFiles);
        assertTrue(fromFiles.length > 0);

        int deleteSize = check(FileAction.MOVE_DELETE, fromFolder);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        newFolder.refresh();
        IP4Resource[] toFiles = newFolder.members();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.MOVE_ADD, newFolder);
        assertEquals(fromSize, toSize);
    }

    /**
     * Tests moving a file
     */
    public void testMoveFile() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IFolder toFolder = project.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            e.printStackTrace();
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        IFile toFile = toFolder.getFile(fromFile.getName());
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);
    }

    /**
     * Test moving an un-added file
     */
    public void testMoveFileUnadded() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse2.properties");
        assertFalse(fromFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            fromFile.create(fileUrl.openStream(), true, null);
            assertTrue(fromFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        IFolder toFolder = project.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertNull(p4File.getP4JFile());

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        assertNull(p4File.getP4JFile());
        assertFalse(p4File.isOpened());

        try {
            fromFile.refreshLocal(IResource.DEPTH_ONE,
                    new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertFalse(fromFile.exists());

        IFile toFile = toFolder.getFile(fromFile.getName());
        try {
            toFile.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        assertNull(p4File2.getP4JFile());
        assertFalse(p4File2.isOpened());
    }

    /**
     * Test moving a folder with files under source control and a file not under
     * source control
     */
    public void testMoveMixedFolder() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("images");

        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        int fromSize = fromFolder.members().length;

        IFile unadded = folderFrom.getFile("unadded_file.txt");
        try {
            Utils.fillFile(unadded);
        } catch (Exception e) {
            handle(e);
        }
        assertTrue(unadded.exists());

        IFolder folderTo = project.getFolder("META-INF");
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        IP4Resource[] fromFiles = fromFolder.members();
        assertNotNull(fromFiles);
        assertTrue(fromFiles.length > 0);

        int deleteSize = check(FileAction.MOVE_DELETE, fromFolder);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        newFolder.refresh();
        IP4Resource[] toFiles = newFolder.members();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.MOVE_ADD, newFolder);
        assertEquals(fromSize, toSize);

        try {
            unadded.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertFalse(unadded.exists());

        IFolder moved = folderTo.getFolder(folderFrom.getName());
        try {
            moved.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertTrue(moved.exists());
        unadded = moved.getFile(unadded.getName());
        assertTrue(unadded.exists());
    }

    /**
     * Test moving a folder that contains a folder that contains an unadded file
     */
    public void testMoveNestedFolderWithUnadded() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("images");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        int fromSize = fromFolder.members().length;

        IFolder unaddedFolder = folderFrom.getFolder("unadded_folder");
        assertFalse(unaddedFolder.exists());
        try {
            unaddedFolder.create(true, true, new NullProgressMonitor());
        } catch (CoreException e1) {
            handle(e1);
        }
        assertTrue(unaddedFolder.exists());

        IFile unadded = unaddedFolder.getFile("unadded_file.txt");
        try {
            Utils.fillFile(unadded);
        } catch (Exception e) {
            handle(e);
        }
        assertTrue(unadded.exists());

        IFolder folderTo = project.getFolder("META-INF");
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        IP4Resource[] fromFiles = fromFolder.members();
        assertNotNull(fromFiles);
        assertTrue(fromFiles.length > 0);

        int deleteSize = check(FileAction.MOVE_DELETE, fromFolder);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        newFolder.refresh();
        IP4Resource[] toFiles = newFolder.members();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.MOVE_ADD, newFolder);
        assertEquals(fromSize, toSize);

        try {
            unaddedFolder.refreshLocal(IResource.DEPTH_ONE,
                    new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertFalse(unaddedFolder.exists());

        try {
            unadded.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertFalse(unadded.exists());

        IFolder moved = folderTo.getFolder(folderFrom.getName()).getFolder(
                unaddedFolder.getName());
        try {
            moved.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertTrue(moved.exists());
        unadded = moved.getFile(unadded.getName());
        assertTrue(unadded.exists());
    }

    /**
     * Test moving a folder that doesn't contain any files under source control
     */
    public void testMoveUnaddedFolder() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("unadded_folder");
        assertFalse(folderFrom.exists());
        try {
            folderFrom.create(true, true, new NullProgressMonitor());
        } catch (CoreException e1) {
            handle(e1);
        }
        assertTrue(folderFrom.exists());

        IFile unadded = folderFrom.getFile("unadded_file.txt");
        try {
            Utils.fillFile(unadded);
        } catch (Exception e) {
            handle(e);
        }
        assertTrue(unadded.exists());

        IFolder folderTo = project.getFolder("META-INF");
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        try {
            folderFrom.refreshLocal(IResource.DEPTH_ONE,
                    new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertFalse(folderFrom.exists());

        try {
            unadded.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertFalse(unadded.exists());

        IFolder moved = folderTo.getFolder(folderFrom.getName());
        try {
            moved.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertTrue(moved.exists());
        unadded = moved.getFile(unadded.getName());
        assertTrue(unadded.exists());
    }

    /**
     * Tests moving a folder that contains at least one sub-folder to an
     * existing folder
     */
    public void testMoveNestedFoldersExisting() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("src");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        IP4File[] allFiles = fromFolder.getAllLocalFiles();
        int fromSize = allFiles.length;
        assertTrue(fromSize > 0);

        IFolder folderTo = project.getFolder("images");
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        int deleteSize = check(FileAction.MOVE_DELETE, allFiles);
        assertTrue(deleteSize > 0);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        IP4File[] toFiles = newFolder.getAllLocalFiles();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.MOVE_ADD, toFiles);
        assertEquals(fromSize, toSize);
    }

    /**
     * Tests moving a folder that contains at least one sub-folder to a new
     * folder
     */
    public void testMoveNestedFoldersNew() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("src");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        IP4File[] allFiles = fromFolder.getAllLocalFiles();
        int fromSize = allFiles.length;
        assertTrue(fromSize > 0);

        IFolder folderTo = project.getFolder("src2");
        assertFalse(folderTo.exists());
        try {
            folderTo.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        int deleteSize = check(FileAction.MOVE_DELETE, allFiles);
        assertTrue(deleteSize > 0);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        IP4File[] toFiles = newFolder.getAllLocalFiles();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.MOVE_ADD, toFiles);
        assertEquals(fromSize, toSize);
    }

    /**
     * Tests deleting a file
     */
    public void testDeleteFileOff() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                false);
        try {
            IFile file = project.getFile("plugin.xml");
            assertTrue(file.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            assertFalse(p4File.isOpened());

            try {
                assertTrue(new P4JavaSysFileCommandsHelper().setWritable(file
                        .getLocation().makeAbsolute().toOSString(), true));
                file.delete(true, null);
            } catch (CoreException e) {
                handle(e);
            }

            assertFalse(file.exists());
            p4File.refresh();
            assertFalse(p4File.isOpened());
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, false);
        }
    }

    /**
     * Tests deleting a file
     */
    public void testDeleteFile() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        try {
            IFile file = project.getFile("plugin.xml");
            assertTrue(file.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;

            try {
                assertTrue(new P4JavaSysFileCommandsHelper().setWritable(file
                        .getLocation().makeAbsolute().toOSString(), true));
                file.delete(true, null);
            } catch (CoreException e) {
                handle(e);
            }

            check(FileAction.DELETE, p4File);
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, false);
        }
    }

    /**
     * Tests deleting a folder and underlying p4 open for deletes occurring
     */
    public void testDeleteFolder() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        try {
            IFolder folder = project.getFolder("images");
            assertTrue(folder.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    folder);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4Folder);
            IP4Folder p4Folder = (IP4Folder) resource;
            int fromSize = p4Folder.members().length;

            try {
                folder.delete(true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }

            IP4Resource[] fromFiles = p4Folder.members();
            assertNotNull(fromFiles);
            assertTrue(fromFiles.length > 0);

            int deleteSize = check(FileAction.DELETE, p4Folder);
            assertEquals(fromSize, deleteSize);
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, false);
        }
    }

    /**
     * Tests deleting a folder with refactoring support off
     */
    public void testDeleteFolderOff() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                false);
        try {
            IFolder folder = project.getFolder("images");
            assertTrue(folder.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    folder);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4Folder);
            IP4Folder p4Folder = (IP4Folder) resource;
            int fromSize = p4Folder.members().length;

            try {
                final P4JavaSysFileCommandsHelper helper = new P4JavaSysFileCommandsHelper();
                folder.accept(new IResourceVisitor() {

                    public boolean visit(IResource resource)
                            throws CoreException {
                        helper.setWritable(resource.getLocation()
                                .makeAbsolute().toOSString(), true);
                        return true;
                    }
                }, IResource.DEPTH_INFINITE, 0);
                folder.delete(true, null);
            } catch (CoreException e) {
                handle(e);
            }

            assertFalse(folder.exists());

            IP4Resource[] fromFiles = p4Folder.members();
            assertNotNull(fromFiles);
            assertTrue(fromFiles.length > 0);

            int unopenedSize = checkNotOpened(p4Folder);
            assertEquals(fromSize, unopenedSize);
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, false);
        }
    }

    private int checkNotOpened(IP4Resource resource) {
        int checked = 0;
        if (resource instanceof IP4Folder) {
            for (IP4Resource child : ((IP4Folder) resource).members()) {
                checked += checkNotOpened(child);
            }
        } else if (resource instanceof IP4File) {
            assertFalse(((IP4File) resource).isOpened());
            checked++;
        }
        return checked;
    }

    /**
     * Tests deleting a project that will do an open for delete
     * 
     * @throws Exception
     *             - exception
     */
    public void testDeleteProject() throws Exception {
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, true);
        try {
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    project);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4Folder);
            IP4Folder p4Folder = (IP4Folder) resource;
            IP4File[] allFiles = p4Folder.getAllLocalFiles();
            int fromSize = allFiles.length;

            try {
                project.delete(true, true, null);
            } catch (CoreException e) {
                assertFalse("Core exception thrown", true);
            }

            IP4Resource[] fromFiles = p4Folder.members();
            assertNotNull(fromFiles);
            assertTrue(fromFiles.length > 0);

            int deleteSize = check(FileAction.DELETE, allFiles);
            assertEquals(fromSize, deleteSize);
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, false);
            if (!project.isAccessible()) {
                project = null;
            }
        }
    }

    /**
     * Tests moving an existing file across projects
     */
    public void testMoveExistingFileAcrossProjects() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IProject project2 = createOtherProject("//depot/p08.1/p4-eclipse/com.perforce.team.ui");
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IFolder toFolder = project2.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        IFile toFile = toFolder.getFile(fromFile.getName());
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);
    }

    /**
     * Tests moving a new file across projects
     */
    public void testMoveNewFileAcrossProjects() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IProject project2 = createOtherProject("//depot/p08.1/p4-eclipse/com.perforce.team.ui");

        IFile fromFile = project.getFile("p4eclipse2.properties");
        assertFalse(fromFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            fromFile.create(fileUrl.openStream(), true, null);
            assertTrue(fromFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        IFolder toFolder = project2.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertNull(p4File.getP4JFile());

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        assertNull(p4File.getP4JFile());
        assertFalse(p4File.isOpened());

        IFile toFile = toFolder.getFile(fromFile.getName());
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        assertNull(p4File2.getP4JFile());
        assertFalse(p4File2.isOpened());
    }

    /**
     * Tests moving a folder across projects
     */
    public void testMoveFolderAcrossProjects() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IProject project2 = createOtherProject("//depot/p08.1/p4-eclipse/com.perforce.team.ui");

        IFolder folderFrom = project.getFolder("images");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        int fromSize = fromFolder.members().length;

        IFolder folderTo = project2.getFolder("META-INF");
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        IP4Resource[] fromFiles = fromFolder.members();
        assertNotNull(fromFiles);
        assertTrue(fromFiles.length > 0);

        int deleteSize = check(FileAction.MOVE_DELETE, fromFolder);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        newFolder.refresh();
        IP4Resource[] toFiles = newFolder.members();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.MOVE_ADD, newFolder);
        assertEquals(fromSize, toSize);

    }

    private IProject createOtherProject(String path) {
        IProject project = null;
        ImportProjectAction checkout = new ImportProjectAction();
        IP4Connection connection = new P4Connection(parameters);
        connection.setOffline(false);
        connection.login(parameters.getPassword());
        connection.connect();
        assertFalse(connection.isOffline());
        assertTrue(connection.isConnected());
        IP4Folder folder = new P4Folder(connection, null, path);
        assertNotNull(folder.getClient());
        assertNotNull(folder.getRemotePath());
        folder.updateLocation();
        assertNotNull(folder.getLocalPath());
        StructuredSelection selection = new StructuredSelection(folder);
        Action wrapAction = new Action() {
        };
        wrapAction.setEnabled(false);
        checkout.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());

        checkout.runAction(new NullProgressMonitor(), false);

        String name = folder.getName();

        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.forceSync(new NullProgressMonitor());

        project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        assertNotNull(project);
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e1) {
            assertFalse(true);
        }
        assertTrue(project.exists());
        assertTrue(project.isAccessible());
        assertTrue(project.isOpen());
        this.otherProject = project;
        try {
            addFile(this.otherProject.getFile(new Path("META-INF/MANIFEST.MF")));
        } catch (Exception e) {
            handle(e);
        }
        return project;
    }

    /**
     * Tests renaming an unopened file
     */
    public void testRenameUnopenedExistingFile() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("plugin.xml");
        assertTrue(fromFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());

        try {
            fromFile.move(project.getFile("plugin2.xml").getFullPath(), true,
                    null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        IFile toFile = project.getFile("plugin2.xml");
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);
    }

    /**
     * Tests renaming an opened existing file
     */
    public void testRenameOpenedExistingFile() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("plugin.xml");
        assertTrue(fromFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());

        try {
            fromFile.move(project.getFile("plugin2.xml").getFullPath(), true,
                    null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        IFile toFile = project.getFile("plugin2.xml");
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);
    }

    /**
     * Test deleting a file that is opened for add
     */
    public void testDeleteAddedFile() {
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, true);
        try {
            IFile file = project.getFile("plugin2345.xml");
            assertFalse(file.exists());
            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/Test.txt");
            assertNotNull(fileUrl);
            try {
                fileUrl = FileLocator.toFileURL(fileUrl);
            } catch (IOException e) {
                assertFalse("IO exception thrown", true);
            }
            try {
                file.create(fileUrl.openStream(), true, null);
                assertTrue(file.exists());
            } catch (IOException e) {
                assertFalse("IO exception thrown", true);
            } catch (CoreException e) {
                assertFalse("Core exception thrown", true);
            }

            AddAction add = new AddAction();
            add.setAsync(false);
            add.selectionChanged(null, new StructuredSelection(file));
            add.run(null);

            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;

            assertTrue(p4File.openedForAdd());

            try {
                file.delete(true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }
            assertFalse(file.exists());
            // remove this since this depends on time delay
            // assertFalse(p4File.isOpened());
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, false);
        }
    }

    /**
     * Test moving a project, not supported but should still succeed
     */
    public void testMoveProject() {
        IP4File p4ProjectFile = null;
        try {
            IFile projectFile = this.project.getFile(".project");
            assertTrue(projectFile.exists());

            AddAction add = new AddAction();
            add.setAsync(false);
            add.selectionChanged(null, new StructuredSelection(projectFile));
            add.run(null);

            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    projectFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            p4ProjectFile = (IP4File) resource;

            assertTrue(p4ProjectFile.openedForAdd());

            int id = p4ProjectFile
                    .getConnection()
                    .getPendingChangelist(0)
                    .submit("unit test submit", new IP4File[] { p4ProjectFile },new NullProgressMonitor());
            assertTrue(id > 0);

            assertFalse(p4ProjectFile.isOpened());
            assertTrue(projectFile.isReadOnly());
            EditAction edit = new EditAction();
            edit.setAsync(false);
            edit.selectionChanged(null, new StructuredSelection(projectFile));
            edit.run(null);

            assertTrue(p4ProjectFile.isOpened());
            IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                    .makeAbsolute();
            try {
                MoveProjectOperation operation = new MoveProjectOperation(
                        project, path.toFile().toURI(), "test");
                PlatformUI.getWorkbench().getOperationSupport()
                        .getOperationHistory()
                        .execute(operation, new NullProgressMonitor(), null);
                assertNotNull(P4ConnectionManager.getManager().getConnection(
                        project));
                assertEquals(path.append(project.getName()),
                        project.getLocation());
            } catch (ExecutionException e) {
                assertFalse("Execution exception thrown:" + e.getMessage(),
                        true);
            }
        } finally {
            if (p4ProjectFile != null) {
                p4ProjectFile.revert();
                p4ProjectFile.refresh();
                assertFalse(p4ProjectFile.isOpened());
            }
        }
    }

    /**
     * Test moving a file and then moving back to itself
     */
    public void testMoveBack() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IFolder toFolder = project.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        IPath fromLocation = fromFile.getFullPath();
        assertNotNull(fromLocation);

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        IFile toFile = toFolder.getFile(fromFile.getName());
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);

        try {
            toFile.move(fromLocation, true, null);
        } catch (CoreException e) {
            e.printStackTrace();
            assertFalse("Core exception thrown moving back", true);
        }

        check(FileAction.EDIT, p4File);
        assertFalse(p4File2.isOpened());
    }

    /**
     * Test renaming a file then renaming it back to itself
     */
    public void testRenameBack() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        IPath fromLocation = fromFile.getFullPath();
        assertNotNull(fromLocation);

        IFile toFile = project.getFile("test_rename"
                + System.currentTimeMillis() + ".properties");
        assertFalse(toFile.exists());

        try {
            fromFile.move(toFile.getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);

        assertFalse(fromFile.exists());
        try {
            toFile.move(fromLocation, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception renaming moving back", true);
        }

        assertTrue(fromFile.exists());
        check(FileAction.EDIT, p4File);
        assertFalse(p4File2.isOpened());
    }

    /**
     * Test moving the same file twice, would fail with 'classic move'
     */
    public void testDoubleMove() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IFolder toFolder = project.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        IPath fromLocation = fromFile.getFullPath();
        assertNotNull(fromLocation);

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        IFile toFile = toFolder.getFile(fromFile.getName());
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);

        IFolder toFolder2 = project.getFolder("src/com");
        assertTrue(toFolder2.exists());

        try {
            toFile.move(toFolder2.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            e.printStackTrace();
            assertFalse("Core exception thrown moving back", true);
        }
        assertFalse(toFile.exists());

        IFile toFile2 = toFolder2.getFile(fromFile.getName());
        assertTrue(toFile2.exists());

        IP4Resource resource3 = P4Workspace.getWorkspace().getResource(toFile2);
        assertNotNull(resource3);
        assertTrue(resource3 instanceof IP4File);
        IP4File p4File3 = (IP4File) resource3;

        check(FileAction.MOVE_ADD, p4File3);
        assertFalse(p4File2.isOpened());
        check(FileAction.MOVE_DELETE, p4File);
    }

    /**
     * Test renaming the same file twice, would fail with 'classic move'
     */
    public void testDoubleRename() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        IPath fromLocation = fromFile.getFullPath();
        assertNotNull(fromLocation);

        IFile toFile = project.getFile("test_rename"
                + System.currentTimeMillis() + ".properties");
        assertFalse(toFile.exists());

        try {
            fromFile.move(toFile.getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);

        assertFalse(fromFile.exists());

        IFile toFile2 = project.getFile("test2_rename"
                + System.currentTimeMillis() + ".properties");
        assertFalse(toFile2.exists());

        try {
            toFile.move(toFile2.getFullPath(), true, null);
        } catch (CoreException e) {
            e.printStackTrace();
            assertFalse("Core exception thrown moving back", true);
        }
        assertFalse(toFile.exists());
        assertTrue(toFile2.exists());

        IP4Resource resource3 = P4Workspace.getWorkspace().getResource(toFile2);
        assertNotNull(resource3);
        assertTrue(resource3 instanceof IP4File);
        IP4File p4File3 = (IP4File) resource3;

        check(FileAction.MOVE_ADD, p4File3);
        assertFalse(p4File2.isOpened());
        check(FileAction.MOVE_DELETE, p4File);
    }

    private void check(FileAction action, IP4File file) {
        assertSame(action, file.getAction());
        assertTrue(file.isOpened());
        List<IP4Resource> pendinglist = Arrays.asList(
        		file.getConnection().getPendingChangelist(0).members());
        if(action==FileAction.MOVE_DELETE || action==FileAction.MOVE_ADD){
	        if(!pendinglist.contains(file)){
	        	// see P4Resource.hashCode() to why. Simply, the localPath is used for hash code, which result moved file not in the list.
	        	// The following code will check the MOVE_* files in the pending change list.
	        	boolean match=false;
	        	for(IP4Resource pending: pendinglist){
	        		if(pending.getRemotePath().equals(file.getMovedFile())){
	        			match=true;
	        			break;
	        		}
	        	}
	        	assertTrue(match);
	        }
        }else{
        	assertTrue(pendinglist.contains(file)); 
        }
        switch (action) {
        case DELETE:
        case MOVE_DELETE:
            assertTrue(file.openedForDelete());
            break;
        case BRANCH:
        case ADD:
        case MOVE_ADD:
            assertTrue(file.openedForAdd());
            break;
        case INTEGRATE:
        case EDIT:
            assertTrue(file.openedForEdit());
        default:
            break;
        }
    }

    private int check(FileAction action, IP4File[] files) {
        int counted = 0;
        for (IP4File file : files) {
            check(action, file);
            counted++;
        }
        return counted;
    }

    private int check(FileAction action, IP4Folder folder) {
        int counted = 0;
        for (IP4Resource resource : folder.members()) {
            if (resource instanceof IP4Folder) {
                counted += check(action, (IP4Folder) resource);
            } else {
                assertTrue(resource instanceof IP4File);
                check(action, (IP4File) resource);
                counted++;
            }
        }
        return counted;
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.core";
    }

}
