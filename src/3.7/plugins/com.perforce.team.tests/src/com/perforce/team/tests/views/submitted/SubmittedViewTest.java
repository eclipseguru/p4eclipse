/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.submitted;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.views.SubmittedView;

import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedViewTest extends ConnectionBasedTestCase {

    /**
     * Basic submitted view test
     */
    public void testView() {
        SubmittedView view = SubmittedView.showView();
        assertNotNull(view);
        IP4Connection connection = createConnection();
        StructuredSelection selection = new StructuredSelection(connection);
        view.getPerforceViewControl().changeConnection(selection);
        assertNotNull(view.getChangelistTable());
        assertNotNull(view.getViewer());
    }

    /**
     * Test empty submitted view
     */
    public void testEmpty() {
        SubmittedView view = new SubmittedView();
        assertNull(view.getViewer());
        assertNull(view.getChangelistTable());
        assertNull(view.getChangelists());
    }
}
