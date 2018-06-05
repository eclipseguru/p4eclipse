/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.actions.NewServerAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class NewServerActionTest extends ConnectionBasedTestCase {

    /**
     * Test new server action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        NewServerAction newServer = new NewServerAction();
        newServer.selectionChanged(wrap, StructuredSelection.EMPTY);
        assertFalse(wrap.isEnabled());
        IP4Connection connection = createConnection();
        newServer.selectionChanged(wrap, new StructuredSelection(connection));
        assertTrue(wrap.isEnabled());
    }

}
