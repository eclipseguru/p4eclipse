/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.p4java.actions.ServerInfoAction;
import com.perforce.team.ui.server.ServerInfoDialog;

import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerInfoActionTest extends ConnectionBasedTestCase {

    /**
     * Test server info action
     */
    public void testAction() {
        IP4Connection connection = createConnection();
        ServerInfoAction action = new ServerInfoAction();
        action.selectionChanged(null, new StructuredSelection(connection));
        ServerInfoDialog[] dialogs = action.showServerInfoDialogs(false);
        assertNotNull(dialogs);
        try {
            assertEquals(1, dialogs.length);
            assertNotNull(dialogs[0]);
        } finally {
            for (ServerInfoDialog dialog : dialogs) {
                dialog.close();
            }
        }

    }

}
