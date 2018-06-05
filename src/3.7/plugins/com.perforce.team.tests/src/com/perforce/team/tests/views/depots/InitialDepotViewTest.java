/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.depots;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.actions.NewServerAction;
import com.perforce.team.ui.p4java.actions.RemoveServerAction;
import com.perforce.team.ui.views.DepotView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class InitialDepotViewTest extends ConnectionBasedTestCase {

    private void clearConnections() {
        clearConnections(true);
    }

    private void clearConnections(boolean dispatch) {
        P4Workspace.getWorkspace().clear();
        if (dispatch) {
            Utils.sleep(.1);
        }
        for (IWorkbenchWindow window : PlatformUI.getWorkbench()
                .getWorkbenchWindows()) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                page.hideView(DepotView.getView());
            }
        }
    }

    /**
     * Tests an empty view
     */
    public void testEmpty() {
        clearConnections();
        DepotView view = DepotView.showView();
        refreshDepotView(view);
        assertNotNull(view);
        assertNotNull(view.getViewer());
        assertNotNull(view.getViewer().getTree());
        assertEquals(0, view.getViewer().getTree().getItemCount());
        assertFalse(view.getShowDeletedFiles());
        assertFalse(view.getShowFilterClient());
    }

    /**
     * Tests adding a depot
     */
    public void testAdd() {
        clearConnections();
        DepotView view = DepotView.showView();
        assertNotNull(view);
        refreshDepotView(view);
        NewServerAction action = new NewServerAction();
        IP4Connection connection = createConnection();
        action.add(connection.getParameters());
        view.getViewer().expandAll();
        int count = view.getViewer().getTree().getItemCount();
        int seconds = 0;
        while (count < 1 && seconds < 60 * 10) {
            Utils.sleep(.1);
            seconds++;
            count = view.getViewer().getTree().getItemCount();
        }
        assertNotNull(view.getViewer());
        assertNotNull(view.getViewer().getTree());
        assertEquals(1, view.getViewer().getTree().getItemCount());
        TreeItem item = view.getViewer().getTree().getItem(0);
        assertNotNull(item);
        assertNotNull(item.getData());
        assertEquals(connection, item.getData());
    }

    /**
     * Tests removing a depot
     */
    public void testRemove() {
        clearConnections();
        DepotView view = DepotView.showView();
        assertNotNull(view);
    	view.getViewer().setInput(P4ConnectionManager.getManager().getConnections());
    	refreshDepotView(view);
        view.getViewer().expandAll();
        
        // wait for view update.
        int count = view.getViewer().getTree().getItemCount();
        int seconds = 0;
        while (count >0 && seconds < 60 * 10) {
            Utils.sleep(.1);
            seconds++;
            count = view.getViewer().getTree().getItemCount();
        }
        assertEquals(0, view.getViewer().getTree().getItemCount());
        
        NewServerAction action = new NewServerAction();
        IP4Connection connection = createConnection();
        action.add(connection.getParameters());
        count = view.getViewer().getTree().getItemCount();
        seconds = 0;
        while (count < 1 && seconds < 60 * 10) {
            Utils.sleep(.1);
            seconds++;
            count = view.getViewer().getTree().getItemCount();
        }
        assertEquals(1, count);
        TreeItem item = view.getViewer().getTree().getItem(0);
        assertNotNull(item);
        RemoveServerAction remove = new RemoveServerAction();
        remove.setAsync(false);
        remove.selectionChanged(null, new StructuredSelection(item.getData()));
        remove.run(null);
        count = view.getViewer().getTree().getItemCount();
        seconds = 0;
        while (count != 0 && seconds < 60 * 10) {
            Utils.sleep(.1);
            seconds++;
            count = view.getViewer().getTree().getItemCount();
        }
        assertEquals(0, view.getViewer().getTree().getItemCount());
    }

    /**
     * Tests refreshing the depot view
     */
    public void testRefresh() {
        clearConnections();
        DepotView view = DepotView.showView();
        assertNotNull(view);
        refreshDepotView(view);
        assertEquals(0, view.getViewer().getTree().getItemCount());
        NewServerAction action = new NewServerAction();
        IP4Connection connection = createConnection();
        action.add(connection.getParameters());
        assertEquals(0, view.getViewer().getTree().getItemCount());
        refreshDepotView(view);
        view.getViewer().expandToLevel(connection, 0); // lazy tree need explicitly expand.
        Utils.sleep(2); // wait for tree expanding
        assertEquals(1, view.getViewer().getTree().getItemCount());
        TreeItem item = view.getViewer().getTree().getItem(0);
        assertNotNull(item);
        assertNotNull(item.getData());
        assertTrue(item.getData() instanceof IP4Connection);
    }

	private void refreshDepotView(DepotView view) {
		view.getViewer().setInput(P4ConnectionManager.getManager().getConnections());
		view.getViewer().refresh(true);
        view.refresh();
        Utils.waitForFamily(P4Runner.FAMILY_P4_RUNNER); // wait until async update of the depot tree finished.
	}

}
