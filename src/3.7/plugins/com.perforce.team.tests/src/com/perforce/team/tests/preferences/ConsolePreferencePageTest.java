/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.preferences;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.dialogs.ConsolePreferencesDialog;

import org.eclipse.jface.preference.PreferenceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConsolePreferencePageTest extends P4TestCase {

    /**
     * Test console pref page
     */
    public void testConsolePage() {
        PreferenceDialog dialog = P4UIUtils.openPreferencePage(
                ConsolePreferencesDialog.ID, false);
        assertNotNull(dialog);
        try {
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof ConsolePreferencesDialog);
            ConsolePreferencesDialog cPage = (ConsolePreferencesDialog) page;
            assertTrue(cPage.isValid());
            assertNull(cPage.getErrorMessage());
        } finally {
            dialog.close();
        }
    }

}
