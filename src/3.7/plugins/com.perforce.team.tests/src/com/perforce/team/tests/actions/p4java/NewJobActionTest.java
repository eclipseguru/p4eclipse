/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.NewJobAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class NewJobActionTest extends ConnectionBasedTestCase {

    /**
     * Test new job action
     */
    public void testEnablement() {
        IP4Connection connection = createConnection();
        assertFalse(connection.isOffline());
        Action wrap = Utils.getDisabledAction();
        NewJobAction action = new NewJobAction();
        action.selectionChanged(wrap, new StructuredSelection(connection));
        assertTrue(wrap.isEnabled());
        connection.setOffline(true);
        action.selectionChanged(wrap, new StructuredSelection(connection));
        assertFalse(wrap.isEnabled());
    }

}
