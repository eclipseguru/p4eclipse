/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.views.PendingView;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LargeSubmitTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project_large";
    }

    /**
     * Test submit of 1000 files in response to job033026
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

        IFile[] added = new IFile[1000];
        long stamp = System.currentTimeMillis();
        for (int i = 0; i < added.length; i++) {
            added[i] = project.getFile("largeAdd_" + stamp + "_" + i + ".txt");
            assertFalse(added[i].exists());
            try {
                Utils.fillFile(added[i]);
            } catch (Exception e) {
                assertFalse("Failed filling " + added[i].getName(), true);
            }
        }

        Tree tree = view.getViewer().getTree();
        assertNotNull(tree);

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(added));
        add.run(null);

        Utils.sleep(2);

        assertTrue(tree.getItemCount() > 0);

        IP4File[] p4Files = new IP4File[added.length];
        for (int j = 0; j < added.length; j++) {
            IFile addFile = added[j];
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    addFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
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
            p4Files[j] = p4File;
        }
        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);
        assertEquals(0, defaultList.getId());
        assertTrue(defaultList.isDefault());

        int id = defaultList.submit("unit test large submit", p4Files, new NullProgressMonitor());
        assertTrue(id > 0);

        for (IFile addFile : added) {
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    addFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            assertFalse(p4File.isOpened());
            assertEquals(1, p4File.getHeadRevision());
            assertNull(p4File.getAction());
            assertFalse(Arrays.asList(defaultList.members()).contains(p4File));
        }

        Utils.sleep(2);

        assertTrue(tree.getItemCount() > 0);

        for (IP4File p4File : p4Files) {
            for (TreeItem item : tree.getItems()) {
                Object data = item.getData();
                assertTrue(data instanceof IP4PendingChangelist);
                view.getViewer().expandToLevel(data, 9);
                TreeItem[] files = item.getItems();
                for (TreeItem file : files) {
                    assertFalse(p4File.equals(file.getData()));
                }
            }
        }

        IP4SubmittedChangelist list = connection.getSubmittedChangelistById(id);
        assertNotNull(list);
        assertEquals(id, list.getId());
        list.refresh();
        IP4Resource[] members = list.members();
        assertNotNull(members);
        assertEquals(added.length, members.length);
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
