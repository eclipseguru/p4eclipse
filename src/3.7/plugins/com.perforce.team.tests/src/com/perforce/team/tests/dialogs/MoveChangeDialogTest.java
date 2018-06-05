/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.dialogs.MoveChangeDialog;

import org.eclipse.swt.widgets.Table;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MoveChangeDialogTest extends ConnectionBasedTestCase {

    /**
     * Test move change dialog
     */
    public void testDialog() {
        IP4Connection connection = createConnection();
        MoveChangeDialog dialog = null;
        IP4PendingChangelist newList = null;
        try {
            IP4PendingChangelist defaultList = connection
                    .getPendingChangelist(0);
            assertNotNull(defaultList);
            newList = connection.createChangelist("test move change dialog: "
                    + getName(), null);

            assertNotNull(newList);
            dialog = new MoveChangeDialog(Utils.getShell(),
                    new IP4PendingChangelist[] { defaultList, newList });
            dialog.setBlockOnOpen(false);
            dialog.open();
            Table table = dialog.getChangesList();
            assertNotNull(table);
            // Dialog now contains New option
            assertEquals(3, table.getItemCount());
            // New option is position 0 so 1 is the new default
            assertEquals(1, table.getSelectionIndex());
            assertEquals(defaultList.getId(), dialog.getSelectedChange());

        } finally {
            if (dialog != null) {
                dialog.close();
            }
            if (newList != null) {
                newList.delete();
                assertNull(newList.getStatus());
                assertNull(newList.getChangelist());
            }
        }
    }

}
