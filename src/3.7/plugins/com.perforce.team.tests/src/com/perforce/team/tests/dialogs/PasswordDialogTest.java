/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.dialogs.PasswordDialog;

import org.eclipse.swt.SWT;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PasswordDialogTest extends ConnectionBasedTestCase {

    /**
     * Test password dialog
     */
    public void testDialog() {
        IP4Connection connection = createConnection();
        PasswordDialog dialog = null;
        try {
            dialog = new PasswordDialog(Utils.getShell(), connection);
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertNotNull(dialog.getShell().getText());
            assertNull(dialog.getPassword());
            assertNotNull(dialog.getPasswordText());
            String pw = "password123";
            dialog.getPasswordText().setText(pw);
            assertTrue((dialog.getPasswordText().getStyle() & SWT.PASSWORD) != 0);
            dialog.updatePassword();
            assertEquals(pw, dialog.getPassword());
        } finally {
            if (dialog != null) {
                dialog.close();
            }
        }
    }

}
