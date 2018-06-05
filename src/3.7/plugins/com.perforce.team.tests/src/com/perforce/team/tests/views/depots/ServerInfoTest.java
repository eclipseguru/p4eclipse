/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.depots;

import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.server.ServerInfoDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerInfoTest extends ConnectionBasedTestCase {

    /**
     * Test server info dialog
     */
    public void testServerInfo() {
        ServerInfoDialog dialog = new ServerInfoDialog(Utils.getShell(),
                createConnection());
        dialog.setBlockOnOpen(false);
        dialog.open();
        assertNotNull(dialog.getAddress());
        assertTrue(dialog.getAddress().length() > 0);
        assertNotNull(dialog.getDate());
        assertTrue(dialog.getDate().length() > 0);
        assertNotNull(dialog.getVersion());
        assertTrue(dialog.getVersion().length() > 0);
        assertNotNull(dialog.getUptime());
        assertTrue(dialog.getVersion().length() > 0);
        assertNotNull(dialog.getRoot());
        assertTrue(dialog.getRoot().length() > 0);
        assertNotNull(dialog.getLicense());
        assertTrue(dialog.getLicense().length() > 0);

        dialog.close();
    }
}
