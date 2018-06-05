/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.win32;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.RemoveAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.core.synchronize.SyncInfo;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CaseTest extends ProjectBasedTestCase {

    /**
     * Test converting a windows path
     * 
     * @throws Exception
     */
    public void testConvertPath() throws Exception {
        if (P4CoreUtils.isWindows()) {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, false);
            String name1 = "sYNc.txt";
            IFile file1 = this.project.getFile(name1);
            assertFalse(file1.exists());
            Utils.fillFile(file1);
            assertTrue(file1.exists());
            assertEquals(
                    file1.getLocation().makeAbsolute().toOSString(),
                    P4Connection.convertPath(file1.getLocation().makeAbsolute()
                            .toOSString()));
            String baseCase = this.project.getLocation().append("sync.txt")
                    .makeAbsolute().toOSString();
            assertEquals(baseCase, P4Connection.convertPath(baseCase));
        }
    }

    /**
     * Test sync view outgoing
     * 
     * @throws Exception
     */
    public void testSyncViewOutgoing() throws Exception {
        if (P4CoreUtils.isWindows()) {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, false);
            String folderName = Long.toString(System.currentTimeMillis());
            String name1 = "sYNc.txt";
            IFolder folder = this.project.getFolder(folderName);
            assertFalse(folder.exists());
            folder.create(true, true, null);
            assertTrue(folder.exists());
            IFile file1 = folder.getFile(name1);
            assertFalse(file1.exists());
            Utils.fillFile(file1);
            assertTrue(file1.exists());
            AddAction add = new AddAction();
            add.setAsync(false);
            add.selectionChanged(null, new StructuredSelection(file1));
            add.run(null);

            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(project);
            assertNotNull(connection);

            IP4Resource resource1 = P4Workspace.getWorkspace().getResource(
                    file1);
            assertNotNull(resource1);
            assertTrue(resource1 instanceof IP4File);
            IP4File p4File1 = (IP4File) resource1;
            assertTrue(p4File1.openedForAdd());

            connection.getPendingChangelist(0).submit(
                    "win32 case outgoing sync test", new IP4File[] { p4File1 },new NullProgressMonitor());
            assertFalse(p4File1.openedForAdd());
            assertEquals(1, p4File1.getHeadRevision());
            assertEquals(1, p4File1.getHaveRevision());

            file1.delete(true, null);
            assertFalse(file1.exists());

            String name2 = "SynC.txt";
            IFile file2 = folder.getFile(name2);
            assertFalse(file2.exists());
            Utils.fillFile(file2);
            assertTrue(file2.exists());

            add.selectionChanged(null, new StructuredSelection(file2));
            add.run(null);

            IP4Resource resource2 = P4Workspace.getWorkspace().getResource(
                    file2);
            assertNotNull(resource2);
            assertTrue(resource2 instanceof IP4File);
            IP4File p4File2 = (IP4File) resource2;
            assertTrue(p4File2.openedForAdd());

            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { folder }, IResource.DEPTH_INFINITE, null);
            SyncInfo info1 = PerforceSubscriber.getSubscriber().getSyncInfo(
                    file1);
            assertNull(info1);
            SyncInfo info2 = PerforceSubscriber.getSubscriber().getSyncInfo(
                    file2);
            assertNotNull(info2);
        }
    }

    /**
     * Test sync view incoming
     * 
     * @throws Exception
     */
    public void testSyncViewIncoming() throws Exception {
        if (P4CoreUtils.isWindows()) {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, false);
            String folderName = Long.toString(System.currentTimeMillis());
            String name1 = "SYNc.txt";
            IFolder folder = this.project.getFolder(folderName);
            assertFalse(folder.exists());
            folder.create(true, true, null);
            assertTrue(folder.exists());
            IFile file1 = folder.getFile(name1);
            assertFalse(file1.exists());
            Utils.fillFile(file1);
            assertTrue(file1.exists());
            AddAction add = new AddAction();
            add.setAsync(false);
            add.selectionChanged(null, new StructuredSelection(file1));
            add.run(null);

            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(project);
            assertNotNull(connection);

            IP4Resource resource1 = P4Workspace.getWorkspace().getResource(
                    file1);
            assertNotNull(resource1);
            assertTrue(resource1 instanceof IP4File);
            IP4File p4File1 = (IP4File) resource1;
            assertTrue(p4File1.openedForAdd());

            connection.getPendingChangelist(0).submit(
                    "win32 case incoming sync test", new IP4File[] { p4File1 },new NullProgressMonitor());
            assertFalse(p4File1.openedForAdd());
            assertEquals(1, p4File1.getHeadRevision());
            assertEquals(1, p4File1.getHaveRevision());

            RemoveAction remove = new RemoveAction();
            remove.setAsync(false);
            RemoveAction.setNeedConfirm(false);
            remove.selectionChanged(null, new StructuredSelection(file1));
            remove.run(null);
            assertEquals(1, p4File1.getHeadRevision());
            assertEquals(0, p4File1.getHaveRevision());

            file1.delete(true, null);
            assertFalse(file1.exists());

            String name2 = "synC.txt";
            IFile file2 = folder.getFile(name2);
            assertFalse(file2.exists());
            Utils.fillFile(file2);
            assertTrue(file2.exists());

            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { folder }, IResource.DEPTH_INFINITE, null);
            SyncInfo info1 = PerforceSubscriber.getSubscriber().getSyncInfo(
                    file1);
            assertNotNull(info1);
            SyncInfo info2 = PerforceSubscriber.getSubscriber().getSyncInfo(
                    file2);
            assertNull(info2);
        }
    }

    /**
     * Test renaming a file and only changing case
     * 
     * @throws Exception
     */
    public void testFileRename() throws Exception {
        if (P4CoreUtils.isWindows()) {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, true);
            String folderName = Long.toString(System.currentTimeMillis());
            String name1 = "reNAME.txt";
            IFolder folder = this.project.getFolder(folderName);
            assertFalse(folder.exists());
            folder.create(true, true, null);
            assertTrue(folder.exists());
            IFile file1 = folder.getFile(name1);
            assertFalse(file1.exists());
            Utils.fillFile(file1);
            assertTrue(file1.exists());
            AddAction add = new AddAction();
            add.setAsync(false);
            add.selectionChanged(null, new StructuredSelection(file1));
            add.run(null);

            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(project);
            assertNotNull(connection);

            IP4Resource resource1 = P4Workspace.getWorkspace().getResource(
                    file1);
            assertNotNull(resource1);
            assertTrue(resource1 instanceof IP4File);
            IP4File p4File1 = (IP4File) resource1;
            assertTrue(p4File1.openedForAdd());

            connection.getPendingChangelist(0).submit(
                    "win32 case file model test", new IP4File[] { p4File1 },new NullProgressMonitor());
            assertFalse(p4File1.openedForAdd());
            assertEquals(1, p4File1.getHeadRevision());
            assertEquals(1, p4File1.getHaveRevision());

            String name2 = "RENaMe.txt";
            IFile file2 = folder.getFile(name2);
            assertFalse(file2.exists());
            file1.move(folder.getFullPath().append("RENaMe.txt"), true, null);
            assertTrue(file2.exists());

            IP4Resource resource2 = P4Workspace.getWorkspace().getResource(
                    file2);
            assertNotNull(resource2);
            assertTrue(resource2 instanceof IP4File);
            IP4File p4File2 = (IP4File) resource2;
            assertTrue(p4File2.openedForAdd());
            assertEquals(0, p4File2.getHeadRevision());
            assertEquals(0, p4File2.getHaveRevision());
        }
    }

    /**
     * Test basic file model when files that only differ in case exist in the
     * same folder
     * 
     * @throws Exception
     */
    public void testFileModel() throws Exception {
        if (P4CoreUtils.isWindows()) {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_SUPPORT, false);
            String folderName = Long.toString(System.currentTimeMillis());
            String name1 = "FILE.txt";
            IFolder folder = this.project.getFolder(folderName);
            assertFalse(folder.exists());
            folder.create(true, true, null);
            assertTrue(folder.exists());
            IFile file1 = folder.getFile(name1);
            assertFalse(file1.exists());
            Utils.fillFile(file1);
            assertTrue(file1.exists());
            AddAction add = new AddAction();
            add.setAsync(false);
            add.selectionChanged(null, new StructuredSelection(file1));
            add.run(null);

            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(project);
            assertNotNull(connection);

            IP4Resource resource1 = P4Workspace.getWorkspace().getResource(
                    file1);
            assertNotNull(resource1);
            assertTrue(resource1 instanceof IP4File);
            IP4File p4File1 = (IP4File) resource1;
            assertTrue(p4File1.openedForAdd());

            connection.getPendingChangelist(0).submit(
                    "win32 case file model test", new IP4File[] { p4File1 },new NullProgressMonitor());
            assertFalse(p4File1.openedForAdd());
            assertEquals(1, p4File1.getHeadRevision());
            assertEquals(1, p4File1.getHaveRevision());

            file1.delete(true, null);

            String name2 = "FiLe.txt";
            IFile file2 = folder.getFile(name2);
            assertFalse(file2.exists());
            Utils.fillFile(file2);
            assertTrue(file2.exists());

            resource1 = connection
                    .getResource(file1.getLocation().toOSString());
            assertNotNull(resource1);
            assertTrue(resource1 instanceof IP4File);
            p4File1 = (IP4File) resource1;
            assertFalse(p4File1.openedForAdd());
            assertEquals(1, p4File1.getHeadRevision());
            assertEquals(1, p4File1.getHaveRevision());

            IP4Resource resource2 = P4Workspace.getWorkspace().getResource(
                    file2);
            assertNotNull(resource2);
            assertTrue(resource2 instanceof IP4File);
            IP4File p4File2 = (IP4File) resource2;
            assertFalse(p4File2.openedForAdd());
            assertEquals(0, p4File2.getHeadRevision());
            assertEquals(0, p4File2.getHaveRevision());
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project_win32";
    }

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#initParameters()
     */
    @Override
    protected void initParameters() {
        assertNotNull(System.getProperty("p4.client.live"));
        assertNotNull(System.getProperty("p4.user.live"));
        assertNotNull(System.getProperty("p4.password.live"));
        assertNotNull(System.getProperty("p4.port.live"));
        parameters = new ConnectionParameters();
        parameters.setClient(System.getProperty("p4.client.live"));
        parameters.setUser(System.getProperty("p4.user.live"));
        parameters.setPort(System.getProperty("p4.port.live"));
        parameters.setPassword(System.getProperty("p4.password.live"));
    }

}
