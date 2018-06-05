/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.pending;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.views.PendingView;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingViewTest extends ConnectionBasedTestCase {

    /**
     * Test the basic API of the pending view
     */
    public void testBasic() {
        PendingView view = PendingView.showView();
        assertNotNull(view);
        assertEquals(view, PendingView.getView());
        assertNotNull(view.getViewer());
    }

    /**
     * Test default view
     */
    public void testDefault() {
        IP4Connection connection = createConnection();
        PendingView view = PendingView.showView();

        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        view.showOtherChanges(false);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);

        assertEquals(1, tree.getItemCount());
        TreeItem defaultItem = tree.getItem(0);
        assertNotNull(defaultItem);
        assertTrue(defaultItem.getText().length() > 0);
        assertNotNull(defaultItem.getData());
        assertTrue(defaultItem.getData() instanceof IP4PendingChangelist);
        IP4PendingChangelist list = (IP4PendingChangelist) defaultItem
                .getData();
        assertTrue(list.isDefault());
        assertFalse(list.isReadOnly());
        assertTrue(list.isOnClient());
        assertEquals(0, list.getId());
    }

    /**
     * Test refresh
     */
    public void testRefresh() {
        IP4Connection connection = createConnection();
        PendingView view = PendingView.showView();

        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        view.showOtherChanges(false);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);

        assertEquals(1, tree.getItemCount());

        IP4PendingChangelist newList = connection.createChangelist(
                "test refresh: " + getName(), null);
        assertNotNull(newList);
        try {
            assertTrue(newList.getId() > 0);

            view.refresh();

            while (view.isLoading()) {
                Utils.sleep(.1);
            }

            assertEquals(2, tree.getItemCount());

            TreeItem newItem = tree.getItem(1);
            assertNotNull(newItem);
            assertNotNull(newItem.getData());
            assertTrue(newItem.getData() instanceof IP4PendingChangelist);
            IP4PendingChangelist list = (IP4PendingChangelist) newItem
                    .getData();
            assertFalse(list.isDefault());
            assertFalse(list.isReadOnly());
            assertTrue(list.isOnClient());
            assertTrue(list.getId() > 0);
        } finally {
            if (newList != null) {
                newList.delete();
                assertNull(newList.getChangelist());
            }
        }
    }

    private void createClient2() {
        ConnectionParameters params2 = new ConnectionParameters();
        params2.setPort(parameters.getPort());
        params2.setUser(parameters.getUser() + "a");
        params2.setClient(parameters.getClient() + "2");
        params2.setPassword(parameters.getPassword());
        try {
            IServer server = getServer(params2);
            IClient client = createClient(getServer(params2), params2);
            server.setCurrentClient(client);
            IChangelist cl = new Changelist();
            cl.setId(IChangelist.UNKNOWN);
            cl.setClientId(client.getName());
            cl.setDescription("test");
            cl.setUsername(client.getServer().getUserName());
            cl = client.createChangelist(cl);
            assertNotNull(cl);
        } catch (Exception e) {
            handle(e);
        }
    }

    /**
     * Test show other changelists
     */
    public void testShowOther() {
        IP4Connection connection = createConnection();
        PendingView view = PendingView.showView();
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        view.showOtherChanges(false);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);

        assertEquals(1, tree.getItemCount());

        TreeItem newItem = tree.getItem(0);
        assertNotNull(newItem);
        assertNotNull(newItem.getData());
        assertTrue(newItem.getData() instanceof IP4PendingChangelist);
        IP4PendingChangelist list = (IP4PendingChangelist) newItem.getData();
        assertTrue(list.isDefault());
        assertFalse(list.isReadOnly());
        assertTrue(list.isOnClient());
        assertEquals(0, list.getId());

        createClient2();

        view.showOtherChanges(true);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        assertTrue(tree.getItemCount() > 1);

        boolean foundOther = false;
        boolean foundDefault = false;
        for (TreeItem item : tree.getItems()) {
            assertNotNull(item);
            assertNotNull(item.getData());
            assertTrue(item.getData() instanceof IP4PendingChangelist);
            list = (IP4PendingChangelist) item.getData();
            if (list.isDefault()) {
                if (list.isOnClient()) {
                    foundDefault = true;
                }
                assertEquals(0, list.getId());
            } else {
                assertTrue(list.getId() > 0);
            }
            if (!list.isOnClient()) {
                foundOther = true;
            }
        }
        assertTrue(foundOther);
        assertTrue(foundDefault);
    }
}
