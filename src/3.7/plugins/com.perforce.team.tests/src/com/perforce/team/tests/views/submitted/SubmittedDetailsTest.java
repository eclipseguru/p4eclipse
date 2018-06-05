/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.submitted;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.views.SubmittedView;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedDetailsTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        IClient client = createConnection().getClient();
        addDepotFile(client, "//depot/SubmittedDetailsTest/test.txt");
        addDepotFile(client, "//depot/SubmittedDetailsTest/test.txt");
    }

    /**
     * Tests the details component of the submitted changelist view
     */
    public void testDetails() {
        SubmittedView view = SubmittedView.showView();
        assertNotNull(view);
        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(parameters);
        view.showChangelists(connection);

        Utils.sleep(.1);

        // Wait for changelists to load since this is now async
        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        assertNotNull(view.getChangelists());
        assertTrue(view.getChangelists().length > 0);
        assertNotNull(view.getChangelists()[0]);
        if (view.getChangelists()[0].needsRefresh()) {
            view.getChangelists()[0].refresh();
        }

        view.showDisplayDetails(true);
        assertNotNull(view.getChangeDetails());
        assertTrue(view.getChangeDetails().length() > 0);
        assertNotNull(view.getDateDetails());
        assertTrue(view.getDateDetails().length() > 0);
        assertNotNull(view.getClientDetails());
        assertTrue(view.getClientDetails().length() > 0);
        assertNotNull(view.getDescriptionDetail());
        assertTrue(view.getDescriptionDetail().length() > 0);
        assertNotNull(view.getUserDetail());
        assertTrue(view.getUserDetail().length() > 0);

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree table = viewer.getTree();
        assertNotNull(table);
        assertTrue(table.getItemCount() > 1);
        StructuredSelection selection = new StructuredSelection(new Object[] {
                new Object(), new Object() });
        SelectionChangedEvent event = new SelectionChangedEvent(viewer,
                selection);
        view.getChangelistTable().selectionChanged(event);
        
        assertEquals(0,view.getChangeDetails().length());
        assertEquals(0,view.getDateDetails().length());
        assertEquals(0,view.getClientDetails().length());
        assertEquals(0,view.getDescriptionDetail().length());
        assertEquals(0,view.getUserDetail().length());
    }
}
