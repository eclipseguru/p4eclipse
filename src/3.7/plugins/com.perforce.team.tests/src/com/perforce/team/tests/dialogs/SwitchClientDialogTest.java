/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.dialogs.SwitchClientConfirmDialog;

public class SwitchClientDialogTest extends ConnectionBasedTestCase {

    /**
     * Test noprompt
     */
    public void testDialog1() {
        SwitchClientConfirmDialog dialog = new SwitchClientConfirmDialog(Utils.getShell());
        dialog.setBlockOnOpen(false);
        try {
            dialog.open();
            dialog.setAutoSync(true);
            dialog.setNoPrompt(true);
            dialog.okPressed();
            assertTrue(dialog.isAutoSync());
            assertEquals(IPerforceUIConstants.ALWAYS,dialog.getPreferenceStore().getString(IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION));
            
        } finally {
            dialog.close();
        }
        
    }

    /**
     * test prompt
     */
    public void testDialog2() {
        SwitchClientConfirmDialog dialog = new SwitchClientConfirmDialog(Utils.getShell());
        dialog.setBlockOnOpen(false);
        try {
            dialog.open();
            dialog.getPreferenceStore().setValue(IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION,IPerforceUIConstants.PROMPT);
            dialog.setAutoSync(true);
            dialog.setNoPrompt(false);
            dialog.okPressed();
            assertTrue(dialog.isAutoSync());
            assertEquals(IPerforceUIConstants.PROMPT,dialog.getPreferenceStore().getString(IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION));
        } finally {
            dialog.close();
        }
        
    }
}
