/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.dialogs.AuthenticationDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AuthDialogTest extends ConnectionBasedTestCase {

    /**
     * Test auth dialog
     */
    public void testDialog() {
        IP4Connection connection = createConnection();
        assertFalse(connection.getParameters().savePassword());
        AuthenticationDialog dialog = new AuthenticationDialog(
                Utils.getShell(), connection);
        dialog.setBlockOnOpen(false);
        try {
            dialog.open();
            assertFalse(dialog.savePassword());
            assertNotNull(dialog.getShell());
        } finally {
            dialog.close();
        }
    }

}
