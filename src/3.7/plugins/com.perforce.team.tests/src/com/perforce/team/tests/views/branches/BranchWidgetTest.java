/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.branches;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.branches.BranchWidget;
import com.perforce.team.ui.branches.BranchesView;

import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchWidgetTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createBranch();
    }

    /**
     * Test widget that displays entire Branches model
     */
    public void testWidget() {
        BranchesView view = BranchesView.showView();
        assertNotNull(view);
        view.showDetails(true);
        IP4Connection connection = createConnection();
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        BranchWidget widget = view.getBranchDetails();
        assertNotNull(widget);
        IP4Branch[] branches = connection.getBranches(1);
        assertNotNull(branches);
        assertEquals(1, branches.length);
        assertNotNull(branches[0]);
        assertTrue(branches[0].needsRefresh());
        branches[0].refresh();
        assertFalse(branches[0].needsRefresh());
        widget.update(branches[0]);

        checkField(branches[0].getName(), widget.getBranchName());
        checkField(branches[0].getOwner(), widget.getOwner());
        checkField(P4UIUtils.formatLabelDate(branches[0].getAccessTime()),
                widget.getAccess());
        checkField(P4UIUtils.formatLabelDate(branches[0].getUpdateTime()),
                widget.getUpdate());
        checkField(branches[0].getDescription(), widget.getDescription());
        assertNotNull(widget.getView());
        assertEquals(branches[0].isLocked(), widget.isLocked());
    }

    private void checkField(String expected, String actual) {
        assertNotNull(actual);
        if (expected != null) {
            assertTrue(actual.length() > 0);
            assertEquals(expected, actual);
        } else {
            assertEquals(0, actual.length());
        }
    }
}
