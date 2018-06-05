/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.preferences;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.dialogs.GeneralPreferencesDialog;

import org.eclipse.jface.preference.PreferenceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GeneralPreferencePageTest extends P4TestCase {

    /**
     * Test general pref page
     */
    public void testPage() {
        PreferenceDialog dialog = P4UIUtils.openPreferencePage(
                GeneralPreferencesDialog.ID, false);
        assertNotNull(dialog);
        try {
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof GeneralPreferencesDialog);
        } finally {
            dialog.close();
        }
    }

}
