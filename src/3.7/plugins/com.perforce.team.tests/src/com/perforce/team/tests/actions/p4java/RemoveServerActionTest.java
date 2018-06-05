/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.p4java.actions.RemoveServerAction;

import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RemoveServerActionTest extends ConnectionBasedTestCase {

    /**
     * Test the remove server action
     */
    public void testAction() {
        IP4Connection connect = P4Workspace.getWorkspace().getConnection(
                parameters);
        assertNotNull(connect);
        assertTrue(P4Workspace.getWorkspace().containsConnection(parameters));
        RemoveServerAction remove = new RemoveServerAction();
        remove.setAsync(false);
        remove.selectionChanged(null, new StructuredSelection(connect));
        remove.run(null);
        assertFalse(P4Workspace.getWorkspace().containsConnection(parameters));
    }

}
