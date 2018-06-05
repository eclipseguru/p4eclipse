/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.pending;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.EditChangelistAction;
import com.perforce.team.ui.p4java.actions.RevertAction;
import com.perforce.team.ui.p4java.actions.RevertAllAction;
import com.perforce.team.ui.p4java.actions.SubmitAction;
import com.perforce.team.ui.p4java.actions.UnfixJobAction;
import com.perforce.team.ui.views.PendingView;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SharedWorkspaceTest extends ProjectBasedTestCase {

    private ConnectionParameters parameters2 = null;

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        createJob();
    }

    private IP4Connection createConnection2() {
        IP4Connection connection = new P4Connection(parameters2);
        connection.login(parameters2.getPassword());
        connection.connect();
        assertTrue(connection.isConnected());
        assertNotNull(connection.getClient());
        return connection;
    }

    private IP4PendingChangelist fixOtherJob() {
        IP4Connection other2 = createConnection2();
        IP4Job[] jobs = other2.getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        IP4PendingChangelist newList = other2.createChangelist(
                "test SharedWorkspaceTest.fixOtherJob", null);
        new P4Collection(jobs).fix(newList);
        return newList;
    }

    private void deleteOtherChangelist(IP4PendingChangelist otherList) {
        if (otherList == null) {
            return;
        }
        otherList.unfix(otherList.getJobs());
        otherList.delete();
        assertNull(otherList.getChangelist());
    }

    private IP4File addOtherFile() {
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
        IP4Connection other = createConnection2();
        IP4File otherFile = new P4File(other, addFile.getLocation()
                .makeAbsolute().toOSString());
        otherFile.refresh();
        otherFile.add(0);
        otherFile.refresh();
        assertTrue(otherFile.openedForAdd());
        return otherFile;
    }

    private void revertOtherFile(IP4File file) {
        if (file == null) {
            return;
        }
        file.revert();
        file.refresh();
        assertFalse(file.isOpened());
    }

    /**
     * Test other job actions (should all be disabled)
     */
    public void testOtherJobActions() {
        IP4PendingChangelist newList = null;
        try {
            newList = fixOtherJob();

            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(project);

            IP4PendingChangelist[] lists = connection
                    .getPendingChangelists(false);

            assertNotNull(lists);
            assertTrue(lists.length >= 2);

            IP4PendingChangelist otherList = null;

            for (IP4PendingChangelist list : lists) {
                if (!list.isOnClient() && list.isReadOnly()
                        && !list.isDefault() && list.getId() == newList.getId()
                        && list.getClientName().equals(parameters2.getClient())
                        && list.getUserName().equals(parameters2.getUser())) {
                    otherList = list;
                    break;
                }
            }

            assertNotNull(otherList);
            otherList.refresh();

            IP4Job[] jobs = otherList.getJobs();
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            assertFalse(jobs[0].isReadOnly());

            Action wrap = Utils.getDisabledAction();
            UnfixJobAction unfix = new UnfixJobAction();
            unfix.setAsync(false);
            unfix.selectionChanged(wrap, new StructuredSelection(jobs[0]));
            assertTrue(wrap.isEnabled());

        } finally {
            deleteOtherChangelist(newList);
        }
    }

    /**
     * Test other file actions (should all be disabled)
     */
    public void testOtherFileActions() {
        IP4File added = null;
        try {
            added = addOtherFile();
            assertNotNull(added);

            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(project);

            IP4PendingChangelist[] lists = connection
                    .getPendingChangelists(false);

            assertNotNull(lists);
            assertTrue(lists.length >= 2);

            IP4PendingChangelist otherList = null;

            for (IP4PendingChangelist list : lists) {
                if (!list.isOnClient() && list.isReadOnly() && list.isDefault()
                        && list.getClientName().equals(parameters2.getClient())
                        && list.getUserName().equals(parameters2.getUser())) {
                    otherList = list;
                    break;
                }
            }

            assertNotNull(otherList);

            IP4Resource[] members = otherList.members();
            assertNotNull(members);
            assertEquals(1, members.length);
            assertTrue(members[0] instanceof IP4File);

            IP4File otherFile = (IP4File) members[0];
            assertTrue(otherFile.isReadOnly());
            assertTrue(otherFile.openedForAdd());

            Action wrap = Utils.getDisabledAction();
            RevertAction revert = new RevertAction();
            revert.setAsync(false);
            revert.selectionChanged(wrap, new StructuredSelection(otherFile));
            assertFalse(wrap.isEnabled());

        } finally {
            if (added != null) {
                revertOtherFile(added);
            }
        }
    }

    private void verifyOtherImage(Image image) {
        assertNotNull(image);
        Image expected = Utils.enlargeImage(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CHG_OTHER)
                .createImage());
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(x + "," + y + " pixel difference", expected
                        .getImageData().getPixel(x, y), image.getImageData()
                        .getPixel(x, y));
            }
        }
    }

    private void verifyOtherText(String text) {
        assertNotNull(text);
        assertTrue(text.contains(parameters2.getUser()));
        assertTrue(text.contains(parameters2.getClient()));
    }

    /**
     * Test other changelist appearance
     */
    public void testOtherChangelistAppearance() {
        IP4PendingChangelist numberedOther = null;
        IP4File defaultOther = null;
        try {
            numberedOther = fixOtherJob();
            defaultOther = addOtherFile();
            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(project);
            PendingView view = PendingView.showView();

            view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
            while (view.isLoading()) {
                Utils.sleep(.1);
            }

            view.showOtherChanges(false);
            while (view.isLoading()) {
                Utils.sleep(.1);
            }

            view.refresh();
            while (view.isLoading()) {
                Utils.sleep(.1);
            }

            TreeViewer viewer = view.getViewer();
            assertNotNull(viewer);
            Tree tree = viewer.getTree();
            assertNotNull(tree);

            assertTrue(tree.getItemCount() >= 3);

            IP4PendingChangelist foundNumberedOther = null;
            IP4PendingChangelist foundDefaultOther = null;

            for (TreeItem item : tree.getItems()) {
                if (item.getData() instanceof IP4PendingChangelist) {
                    IP4PendingChangelist list = (IP4PendingChangelist) item
                            .getData();
                    if (!list.isOnClient() && list.isReadOnly()) {
                        if (list.getUserName().equals(parameters2.getUser())
                                && list.getClientName().equals(
                                        parameters2.getClient())) {
                            if (list.isDefault()) {
                                foundDefaultOther = list;
                                verifyOtherImage(item.getImage());
                                verifyOtherText(item.getText());
                            } else if (numberedOther.getId() == list.getId()) {
                                foundNumberedOther = list;
                                verifyOtherImage(item.getImage());
                                verifyOtherText(item.getText());
                            }
                        }
                    }
                }
            }

            assertNotNull(foundDefaultOther);
            assertNotNull(foundNumberedOther);

        } finally {
            try {
                deleteOtherChangelist(numberedOther);
            } finally {
                revertOtherFile(defaultOther);
            }
        }
    }

    /**
     * Test other changelist actions (should all be disabled)
     */
    public void testOtherChangelistActions() {
        IP4File added = null;
        try {
            added = addOtherFile();
            assertNotNull(added);

            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(project);

            IP4PendingChangelist[] lists = connection
                    .getPendingChangelists(false);

            assertNotNull(lists);
            assertTrue(lists.length >= 2);

            IP4PendingChangelist otherList = null;

            for (IP4PendingChangelist list : lists) {
                if (!list.isOnClient() && list.isReadOnly() && list.isDefault()
                        && list.getClientName().equals(parameters2.getClient())
                        && list.getUserName().equals(parameters2.getUser())) {
                    otherList = list;
                    break;
                }
            }

            assertNotNull(otherList);
            assertTrue(otherList.isReadOnly());

            Action wrap = Utils.getDisabledAction();
            EditChangelistAction edit = new EditChangelistAction();
            edit.setAsync(false);
            edit.selectionChanged(wrap, new StructuredSelection(otherList));
            assertFalse(wrap.isEnabled());

            wrap = Utils.getDisabledAction();
            SubmitAction submit = new SubmitAction();
            submit.setAsync(false);
            submit.selectionChanged(wrap, new StructuredSelection(otherList));
            assertFalse(wrap.isEnabled());

            wrap = Utils.getDisabledAction();
            RevertAllAction revert = new RevertAllAction();
            revert.setAsync(false);
            revert.selectionChanged(wrap, new StructuredSelection(otherList));
            assertFalse(wrap.isEnabled());

        } finally {
            if (added != null) {
                revertOtherFile(added);
            }
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project_shared";
    }

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#initParameters()
     */
    @Override
    protected void initParameters() {
        super.initParameters();

        // params 2
        parameters2 = new ConnectionParameters();
        parameters2.setClient(parameters.getClient());
        parameters2.setUser(parameters.getUser() + "a");
        parameters2.setPort(parameters.getPort());
        parameters2.setPassword(parameters.getPassword());
    }
}
