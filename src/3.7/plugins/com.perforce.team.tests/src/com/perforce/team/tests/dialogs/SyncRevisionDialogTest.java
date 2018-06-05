/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.dialogs.SyncRevisionDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SyncRevisionDialogTest extends P4TestCase {

    /**
     * Test sync revision dialog
     */
    public void testDialog() {
        SyncRevisionDialog dialog = new SyncRevisionDialog(Utils.getShell());
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertTrue(dialog.revisionSelected());
            assertFalse(dialog.otherSelected());
            assertFalse(dialog.latestSelected());
            assertFalse(dialog.forceSelected());
            dialog.setForceSelected(true);
            assertTrue(dialog.forceSelected());

            dialog.selectLatest();
            assertTrue(dialog.latestSelected());
            assertFalse(dialog.otherSelected());
            assertFalse(dialog.revisionSelected());
            assertEquals("", dialog.getCurrentRevisionSpec());

            dialog.selectRevision();
            assertTrue(dialog.revisionSelected());
            assertFalse(dialog.latestSelected());
            assertFalse(dialog.otherSelected());
            dialog.setRevisionText("3");
            assertEquals("#3", dialog.getCurrentRevisionSpec());

            dialog.selectOther();
            assertTrue(dialog.otherSelected());
            assertFalse(dialog.revisionSelected());
            assertFalse(dialog.latestSelected());
            dialog.setOtherText("label_1");
            assertEquals("@label_1", dialog.getCurrentRevisionSpec());

            assertNull(dialog.getRevSpec());
            assertFalse(dialog.forceSync());
        } finally {
            dialog.close();
        }
    }
}
