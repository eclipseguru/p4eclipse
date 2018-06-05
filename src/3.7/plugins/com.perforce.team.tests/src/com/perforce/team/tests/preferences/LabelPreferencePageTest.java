/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.preferences;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.preferences.decorators.LabelPreviewPreferencePage;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelPreferencePageTest extends P4TestCase {

    private void setDefaults() {
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        store.setToDefault(IPerforceUIConstants.PREF_IGNORED_TEXT);
        store.setToDefault(IPerforceUIConstants.PREF_FILE_OPEN_ICON);
        store.setToDefault(IPerforceUIConstants.PREF_FILE_SYNC_ICON);
        store.setToDefault(IPerforceUIConstants.PREF_FILE_SYNC2_ICON);
        store.setToDefault(IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON);
        store.setToDefault(IPerforceUIConstants.PREF_FILE_LOCK_ICON);
        store.setToDefault(IPerforceUIConstants.PREF_FILE_OTHER_ICON);
        store.setToDefault(IPerforceUIConstants.PREF_RETRIEVE_NUM_JOBS);
        store.setToDefault(IPerforceUIConstants.PREF_RETRIEVE_NUM_CHANGES);
        store.setToDefault(IPerforceUIConstants.PREF_PROJECT_ICON);
        store.setToDefault(IPreferenceConstants.FILE_DECORATION_TEXT);
        store.setToDefault(IPreferenceConstants.CONNECTION_DECORATION_TEXT);
        store.setToDefault(IPreferenceConstants.PROJECT_DECORATION_TEXT);
        store.setToDefault(IPreferenceConstants.IGNORED_DECORATION);
        store.setToDefault(IPreferenceConstants.OUTGOING_CHANGE_DECORATION);
    }

    /**
     * Test label preview preference page
     */
    public void testPreferencePage() {
        setDefaults();
        PreferenceDialog dialog = P4UIUtils.openPreferencePage(
                LabelPreviewPreferencePage.ID, false);
        assertNotNull(dialog);
        try {
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof LabelPreviewPreferencePage);
            LabelPreviewPreferencePage labelPage = (LabelPreviewPreferencePage) page;

            assertNotNull(labelPage.getProjectIconText());
            assertTrue(labelPage.getProjectIconText().length() > 0);

            assertNotNull(labelPage.getConnectionText());
            assertTrue(labelPage.getConnectionText().length() > 0);

            assertNotNull(labelPage.getProjectText());
            assertTrue(labelPage.getProjectText().length() > 0);

            assertNotNull(labelPage.getIgnoredIconText());
            assertTrue(labelPage.getIgnoredIconText().length() > 0);

            assertNotNull(labelPage.getIgnoredText());
            assertTrue(labelPage.getIgnoredText().length() > 0);

            assertNotNull(labelPage.getFileText());
            assertTrue(labelPage.getFileText().length() > 0);

            assertNotNull(labelPage.getSyncIconText());
            assertTrue(labelPage.getSyncIconText().length() > 0);

            assertNotNull(labelPage.getNotSyncIconText());
            assertTrue(labelPage.getNotSyncIconText().length() > 0);

            assertNotNull(labelPage.getLockedIconText());
            assertTrue(labelPage.getLockedIconText().length() > 0);

            assertNotNull(labelPage.getUnresolvedIconText());
            assertTrue(labelPage.getUnresolvedIconText().length() > 0);

            assertNotNull(labelPage.getOpenedElsewhereIconText());
            assertTrue(labelPage.getOpenedElsewhereIconText().length() > 0);

            assertNotNull(labelPage.getOpenIconText());
            assertTrue(labelPage.getOpenIconText().length() > 0);

            assertNotNull(labelPage.getOutgoingText());
            assertTrue(labelPage.getOutgoingText().length() > 0);

            assertFalse(labelPage.getChangelistInSyncSelection());
            assertFalse(labelPage.getIgnoredTextSelection());
        } finally {
            dialog.close();
        }
    }

}
