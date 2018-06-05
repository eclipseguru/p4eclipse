/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.preferences;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.preferences.AdvancedPreferencePage;

import org.eclipse.jface.preference.PreferenceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AdvancedPreferencePageTest extends P4TestCase {

    /**
     * Test advanced pref page
     */
    public void testAdvancedPage() {
        PreferenceDialog dialog = P4UIUtils.openPreferencePage(
                AdvancedPreferencePage.ID, false);
        assertNotNull(dialog);
        try {
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof AdvancedPreferencePage);
            AdvancedPreferencePage aPage = (AdvancedPreferencePage) page;
            assertTrue(aPage.isValid());
            assertNull(aPage.getErrorMessage());
        } finally {
            dialog.close();
        }
    }

}
