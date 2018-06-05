/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.IntegrateAction;
import com.perforce.team.ui.p4java.actions.MoveToAnotherChangelistAction;
import com.perforce.team.ui.p4java.actions.RevertAllAction;
import com.perforce.team.ui.p4java.actions.SubmitAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.p4java.dialogs.ChangeSpecDialog;
import com.perforce.team.ui.views.PendingView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LiveSubmitTest extends ProjectBasedTestCase {

    /**
     * Tests actual submit against a test p4 server
     */
    public void testSubmit() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        PendingView view = PendingView.showView();
        assertNotNull(view);
        view.showOtherChanges(false);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        Tree tree = view.getViewer().getTree();
        assertNotNull(tree);

        IFile addFile = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
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

        Utils.sleep(.1);

        assertTrue(tree.getItemCount() > 0);
        boolean found = false;
        for (TreeItem item : tree.getItems()) {
            Object data = item.getData();
            assertTrue(data instanceof IP4PendingChangelist);
            view.getViewer().expandToLevel(data, 9);
            TreeItem[] files = item.getItems();
            for (TreeItem file : files) {
                if (p4File.equals(file.getData())) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);

        assertTrue(p4File.openedForAdd());

        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);

        int id = defaultList.submit("unit test submit",
                new IP4File[] { p4File }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(p4File.isOpened());
        assertEquals(1, p4File.getHeadRevision());
        assertNull(p4File.getAction());
        assertFalse(Arrays.asList(defaultList.members()).contains(p4File));

        Utils.sleep(.1);

        assertTrue(tree.getItemCount() > 0);
        view.getViewer().expandAll();
        Utils.waitForFamily(P4Runner.FAMILY_P4_RUNNER); // wait until all p4 jobs finished.
        for (TreeItem item : tree.getItems()) {
            Object data = item.getData();
            assertTrue(data instanceof IP4PendingChangelist);
//            view.getViewer().expandToLevel(data, 9);
            TreeItem[] files = item.getItems();
            for (TreeItem file : files) {
                assertFalse(p4File.equals(file.getData()));
            }
        }
    }

    /**
     * Test submitting a non-default changelist
     */
    public void testSubmitNonDefault() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);
        connection.getPendingChangelists(false);

        PendingView view = PendingView.showView();
        assertNotNull(view);
        view.showOtherChanges(false);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        Tree tree = view.getViewer().getTree();
        assertNotNull(tree);

        IFile addFile = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
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

        Action wrap = Utils.getDisabledAction();
        MoveToAnotherChangelistAction move = new MoveToAnotherChangelistAction();
        move.setAsync(false);
        move.selectionChanged(wrap, new StructuredSelection(addFile));
        assertTrue(wrap.isEnabled());

        IP4PendingChangelist newList = p4File.getConnection().createChangelist(
                "test submit non-default changelist: " + getName(), null);

        assertNotNull(connection.getPendingChangelist(newList.getId()));

        Utils.sleep(.1);

        assertTrue(tree.getItemCount() > 1);
        boolean found = false;
        for (TreeItem item : tree.getItems()) {
            Object data = item.getData();
            assertTrue(data instanceof IP4PendingChangelist);
            IP4PendingChangelist pending = (IP4PendingChangelist) data;
            if (newList.getId() == pending.getId()) {
                found = true;
            }
        }
        assertTrue(found);

        assertNotNull(newList);
        assertTrue(newList.getId() > 0);

        assertNotNull(newList.members());
        assertEquals(0, newList.members().length);

        move.move(newList);

        assertTrue(Arrays.asList(newList.members()).contains(p4File));

        int id = newList.submit("test submit non-default changelist: ",
                new IP4File[] { p4File }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(Arrays.asList(newList.members()).contains(p4File));

        assertFalse(p4File.isOpened());
        assertEquals(1, p4File.getHeadRevision());
        assertNull(p4File.getAction());

        Utils.sleep(.1);

        IP4PendingChangelist[] cached = connection
                .getCachedPendingChangelists();
        for (IP4PendingChangelist list : cached) {
            assertFalse(list.getId() == newList.getId());
        }

        assertNull(connection.getPendingChangelist(newList.getId()));

        assertTrue(tree.getItemCount() > 0);
        for (TreeItem item : tree.getItems()) {
            Object data = item.getData();
            assertTrue(data instanceof IP4PendingChangelist);
            IP4PendingChangelist pending = (IP4PendingChangelist) data;
            assertFalse(newList.getId() == pending.getId());
        }

    }

    /**
     * Tests submitting with the reopen flag set to true
     */
    public void testReopen() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        IFile addFile = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
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

        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);

        int id = defaultList.submit(true, "unit test submit",
                new IP4File[] { p4File }, null, new NullProgressMonitor());
        assertTrue(id > 0);

        assertTrue(p4File.isOpened());
        assertTrue(p4File.openedForEdit());
        assertEquals(1, p4File.getHeadRevision());
        assertNotNull(p4File.getAction());
        assertTrue(Arrays.asList(defaultList.members()).contains(p4File));
    }

    /**
     * Tests submitted with jobs being fixed by the submitted changelist
     */
    public void testJobFix() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        IFile addFile = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
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

        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);

        createJob(addFile.getName(), p4File.getClient().getServer());

        IP4Job[] jobs = connection.getJobs();
        assertNotNull(jobs);
        assertTrue(jobs.length > 0);
        IP4Job job = jobs[0];
        assertEquals(addFile.getName(), job.getField("Description").toString()
                .trim());

        int id = defaultList.submit("unit test submit",
                new IP4File[] { p4File }, new IP4Job[] { job }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(p4File.isOpened());
        assertEquals(1, p4File.getHeadRevision());
        assertNull(p4File.getAction());

        jobs = connection.getJobs();
        assertNotNull(jobs);
        assertTrue(jobs.length > 0);
        job = jobs[0];
        assertEquals(addFile.getName(), job.getField("Description").toString()
                .trim());
        assertEquals("closed", job.getField("Status").toString().trim());
    }

    /**
     * Test failure submission
     */
    public void testFailure() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        PendingView view = PendingView.showView();
        assertNotNull(view);
        view.showOtherChanges(false);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        Utils.sleep(.1);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        Tree tree = view.getViewer().getTree();
        assertNotNull(tree);

        IFile addFile = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
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

        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);

        int id = defaultList.submit("unit test submit",
                new IP4File[] { p4File }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(p4File.isOpened());
        assertEquals(1, p4File.getHeadRevision());
        assertNull(p4File.getAction());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(addFile));
        edit.run(null);

        id = defaultList.submit("unit test submit2", new IP4File[] { p4File }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(p4File.isOpened());
        assertEquals(2, p4File.getHeadRevision());
        assertNull(p4File.getAction());

        SyncRevisionAction getOldRevision = new SyncRevisionAction();
        getOldRevision.setAsync(false);
        getOldRevision.selectionChanged(null, new StructuredSelection(addFile));
        getOldRevision.runAction("#1");

        assertEquals(1, p4File.getHaveRevision());

        edit.run(null);

        assertNotNull(p4File.getChangelist());
        assertTrue(defaultList.equals(p4File.getChangelist()));
        assertFalse(p4File.isUnresolved());

        Utils.sleep(1);
        int previous = tree.getItemCount();
        assertEquals(1, previous);

        IP4PendingChangelist[] before = defaultList.getConnection()
                .getCachedPendingChangelists();
        assertNotNull(before);
        id = defaultList.submit("unit test submit failed",
                new IP4File[] { p4File }, new NullProgressMonitor());
        assertEquals(-1, id);

        IP4PendingChangelist[] after = defaultList.getConnection()
                .getCachedPendingChangelists();
        assertNotNull(after);
        assertEquals(before.length + 1, after.length);

        boolean found = false;
        for (IP4PendingChangelist list : after) {
            if (list.needsRefresh()) {
                list.refresh();
            }
            if (Arrays.asList(list.getFiles()).contains(p4File)) {
                assertFalse("File exists in multiple pending changelists",
                        found);
                found = true;
            }
        }
        assertTrue("File not found in pending changelist cache", found);

        Utils.sleep(1);

        assertTrue(tree.getItemCount() > previous);
        for (TreeItem item : tree.getItems()) {
            Object data = item.getData();
            assertTrue(data instanceof IP4PendingChangelist);
        }

        assertTrue(p4File.openedForEdit());
        assertEquals(1, p4File.getHaveRevision());

        assertTrue(p4File.isUnresolved());

        IP4Changelist newChangelist = p4File.getChangelist();
        assertNotNull(newChangelist);
        assertFalse(defaultList.equals(newChangelist));
        RevertAllAction revert = new RevertAllAction();
        revert.setAsync(false);
        revert.selectionChanged(null, new StructuredSelection(newChangelist));
        revert.runAction(false);
        newChangelist.delete();
        assertNull(newChangelist.getChangelist());
    }

    /**
     * Test the change spec dialog with default changelist
     */
    public void testDialogDefault() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);
        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);

        IP4Job[] jobs = connection.getJobs();
        assertNotNull(jobs);
        if (jobs.length == 0) {
            createJob("test dialog default", connection.getServer());
        }
        jobs = connection.getJobs();
        assertNotNull(jobs);
        assertTrue(jobs.length > 0);

        ChangeSpecDialog dialog = new ChangeSpecDialog(defaultList, null,
                Utils.getShell(), true);
        dialog.setBlockOnOpen(false);
        dialog.open();
        dialog.addJobs(new IP4Job[] { jobs[0] });
        dialog.close();
        IP4Job[] checkedJobs = dialog.getCheckedJobs();
        assertNotNull(checkedJobs);
        assertEquals(1, checkedJobs.length);
        assertEquals(jobs[0], checkedJobs[0]);
        IP4File[] checkedFiles = dialog.getCheckedFiles();
        assertNotNull(checkedFiles);
        assertEquals(0, checkedFiles.length);
    }

    /**
     * Test the dialog being opened with job initially to be shown
     */
    public void testDialogWithInitialJob() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);
        IP4PendingChangelist newList = null;
        IP4Job job = null;
        try {
            newList = connection.createChangelist("test dialog: " + getName(),
                    null);
            assertNotNull(newList);

            IP4Job[] jobs = connection.getJobs();
            assertNotNull(jobs);
            if (jobs.length == 0) {
                createJob("test dialog new", connection.getServer());
            }
            jobs = connection.getJobs();
            assertNotNull(jobs);
            assertTrue(jobs.length > 0);
            job = jobs[0];
            newList.fix(job);

            ChangeSpecDialog dialog = new ChangeSpecDialog(newList, null,
                    Utils.getShell(), true);
            dialog.setBlockOnOpen(false);
            dialog.open();
            dialog.close();
            IP4Job[] checkedJobs = dialog.getCheckedJobs();
            assertNotNull(checkedJobs);
            assertEquals(1, checkedJobs.length);
            assertEquals(jobs[0], checkedJobs[0]);
            IP4File[] checkedFiles = dialog.getCheckedFiles();
            assertNotNull(checkedFiles);
            assertEquals(0, checkedFiles.length);
        } finally {
            if (newList != null) {
                if (job != null) {
                    newList.unfix(job);
                }
                newList.delete();
                assertNull(newList.getChangelist());
            }
        }
    }

    /**
     * Test the change spec dialog with new changelist
     */
    public void testDialogNew() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);
        IP4PendingChangelist newList = null;
        try {
            newList = connection.createChangelist("test dialog: " + getName(),
                    null);
            assertNotNull(newList);

            IP4Job[] jobs = connection.getJobs();
            assertNotNull(jobs);
            if (jobs.length == 0) {
                createJob("test dialog new", connection.getServer());
            }
            jobs = connection.getJobs();
            assertNotNull(jobs);
            assertTrue(jobs.length > 0);

            ChangeSpecDialog dialog = new ChangeSpecDialog(newList, null,
                    Utils.getShell(), true);
            dialog.setBlockOnOpen(false);
            dialog.open();
            dialog.addJobs(new IP4Job[] { jobs[0] });
            dialog.close();
            IP4Job[] checkedJobs = dialog.getCheckedJobs();
            assertNotNull(checkedJobs);
            assertEquals(1, checkedJobs.length);
            assertEquals(jobs[0], checkedJobs[0]);
            IP4File[] checkedFiles = dialog.getCheckedFiles();
            assertNotNull(checkedFiles);
            assertEquals(0, checkedFiles.length);
        } finally {
            if (newList != null) {
                newList.delete();
                assertNull(newList.getChangelist());
            }
        }
    }

    private void createJob(String description, IServer server) {
        Map<String, Object> inMap = new HashMap<String, Object>();
        inMap.put("Job", "new");
        inMap.put("Status", "open");
        inMap.put("User", parameters.getUser());
        inMap.put("Description", description);
        try {
            ((IOptionsServer)server).execMapCmdList("job", new String[] { "-i" }, inMap);
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown", true);
        }
    }

    /**
     * Test submitting an integrated file
     */
    public void testIntegrationSubmit() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        IFile addFile = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
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

        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);

        int id = defaultList.submit("unit test submit",
                new IP4File[] { p4File }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(p4File.isOpened());
        assertEquals(1, p4File.getHeadRevision());

        IFile integFile = project.getFile("integrate"
                + System.currentTimeMillis() + ".txt");
        assertFalse(integFile.exists());

        IntegrateAction integ = new IntegrateAction();
        P4IntegrationOptions options = P4IntegrationOptions.createInstance(p4File.getConnection().getServer());
        options.setDontCopyToClient(true);
        integ.setAsync(false);
        P4FileIntegration integration = new P4FileIntegration();
        integration.setSource(p4File.getLocalPath());
        integration.setTarget(integFile.getLocation().makeAbsolute()
                .toOSString());

        integ.integrate(connection, integration, 0, options);

        resource = P4Workspace.getWorkspace().getResource(integFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4Integ = (IP4File) resource;

        assertTrue(p4Integ.isOpened());

        defaultList.submit("unit test integ submit", new IP4File[] { p4Integ }, new NullProgressMonitor());

        assertFalse(p4Integ.isOpened());
        assertEquals(1, p4Integ.getHaveRevision());
        assertEquals(1, p4Integ.getHeadRevision());
    }

    /**
     * Test that submit is disabled for an empty default changelist
     */
    public void testEmptyDefaultEnablement() {
        Action wrap = Utils.getDisabledAction();
        SubmitAction submit = new SubmitAction();
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);
        IP4PendingChangelist list = connection.getPendingChangelist(0);
        assertNotNull(list);
        assertEquals(0, list.members().length);
        submit.selectionChanged(wrap, new StructuredSelection(list));
        assertFalse(wrap.isEnabled());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project1";
    }

}
