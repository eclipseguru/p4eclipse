/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.submitted.SubmittedChangelistDialog;
import com.perforce.team.ui.submitted.SubmittedChangelistTable;

import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedDialogTest extends ConnectionBasedTestCase {

    /**
     * Test the submitted changelist table dialog
     */
    public void testDialog() {
        SubmittedChangelistDialog dialog = null;
        try {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_RETRIEVE_NUM_CHANGES, 10);
            IP4Connection connection = createConnection();
            dialog = new SubmittedChangelistDialog(Utils.getShell(), connection);
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertNotNull(dialog.getSelected());
            assertEquals(0, dialog.getSelected().length);
            SubmittedChangelistTable table = dialog.getTable();
            assertNotNull(table);
            while (table.isLoading()) {
                Utils.sleep(.1);
            }
            assertNotNull(table.getViewer());
            assertNotNull(table.getChangelists());
            assertTrue(table.getChangelists().length >= 2);
            IP4SubmittedChangelist[] lists = new IP4SubmittedChangelist[] {
                    table.getChangelists()[0], table.getChangelists()[1] };
            table.getViewer().setSelection(new StructuredSelection(lists));
            dialog.updateSelectedChangelists();
            assertNotNull(dialog.getSelected());
            assertEquals(2, dialog.getSelected().length);

            IP4SubmittedChangelist select1 = dialog.getSelected()[0];
            assertNotNull(select1);
            IP4SubmittedChangelist select2 = dialog.getSelected()[1];
            assertNotNull(select2);
            if (select1.getId() == lists[0].getId()) {
                assertEquals(lists[0], select1);
                assertEquals(lists[1], select2);
            } else {
                assertEquals(lists[1], select1);
                assertEquals(lists[0], select2);
            }
        } finally {
            if (dialog != null) {
                dialog.close();
            }
        }
    }
}
