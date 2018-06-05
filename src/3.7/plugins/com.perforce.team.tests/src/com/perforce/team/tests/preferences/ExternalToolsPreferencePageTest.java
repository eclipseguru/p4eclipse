/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.preferences;

import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.ExternalToolsPreferencePage;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.PreferenceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ExternalToolsPreferencePageTest extends ConnectionBasedTestCase {

    private String p4merge = null;
    private String p4v = null;

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#initParameters()
     */
    @Override
    protected void initParameters() {
        super.initParameters();
        assertNotNull(System.getProperty("p4merge"));
        p4merge = System.getProperty("p4merge");
        assertNotNull(System.getProperty("p4v"));
        p4v = System.getProperty("p4v");
    }

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .setValue(IPreferenceConstants.P4MERGE_PATH, "");
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .setValue(IPreferenceConstants.P4V_PATH, "");
    }

    /**
     * Test p4 merge setting
     */
    public void testP4MergeSetting() {
        PreferenceDialog dialog = P4UIUtils.openPreferencePage(
                ExternalToolsPreferencePage.ID, false);
        assertNotNull(dialog);
        try {
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof ExternalToolsPreferencePage);
            ExternalToolsPreferencePage ePage = (ExternalToolsPreferencePage) page;
            assertNotNull(ePage.getP4MergeText());
            assertEquals(0, ePage.getP4MergeText().length());
            assertTrue(ePage.isValid());
            assertNull(ePage.getErrorMessage());
            ePage.setP4MergeText("/bad_path/to_p4_merge.exe");
            assertFalse(ePage.isValid());
            assertNotNull(ePage.getErrorMessage());
            ePage.setP4MergeText(p4v);
            assertTrue(ePage.isValid());
            assertNull(ePage.getErrorMessage());
        } finally {
            dialog.close();
        }
    }

    /**
     * Test p4v setting
     */
    public void testP4VSetting() {
        PreferenceDialog dialog = P4UIUtils.openPreferencePage(
                ExternalToolsPreferencePage.ID, false);
        assertNotNull(dialog);
        try {
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof ExternalToolsPreferencePage);
            ExternalToolsPreferencePage ePage = (ExternalToolsPreferencePage) page;
            assertNotNull(ePage.getP4VText());
            assertEquals(0, ePage.getP4VText().length());
            assertTrue(ePage.isValid());
            assertNull(ePage.getErrorMessage());
            ePage.setP4VText("/bad_path/to_p4_v.exe");
            assertFalse(ePage.isValid());
            assertNotNull(ePage.getErrorMessage());
            ePage.setP4VText(p4v);
            assertTrue(ePage.isValid());
            assertNull(ePage.getErrorMessage());
        } finally {
            dialog.close();
        }
    }

    /**
     * Test saving the prefernces
     */
    public void testPrefSaving() {
        PreferenceDialog dialog = P4UIUtils.openPreferencePage(
                ExternalToolsPreferencePage.ID, false);
        assertNotNull(dialog);
        try {
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof ExternalToolsPreferencePage);
            ExternalToolsPreferencePage ePage = (ExternalToolsPreferencePage) page;
            ePage.setP4VText(p4v);
            ePage.setP4MergeText(p4merge);
            ePage.performOk();
            assertEquals(p4v, PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .getString(IPreferenceConstants.P4V_PATH));
            assertEquals(p4merge,
                    PerforceUIPlugin.getPlugin().getPreferenceStore()
                            .getString(IPreferenceConstants.P4MERGE_PATH));
        } finally {
            dialog.close();
        }
    }

}
