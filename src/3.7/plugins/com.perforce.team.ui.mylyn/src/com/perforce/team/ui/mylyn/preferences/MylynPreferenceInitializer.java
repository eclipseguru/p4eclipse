/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.preferences;

import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MylynPreferenceInitializer extends AbstractPreferenceInitializer {

    /**
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = PerforceUiMylynPlugin.getDefault()
                .getPreferenceStore();

        store.setDefault(IPreferenceConstants.GROUP_FIELDS_BY_TYPE, true);
        store.setDefault(IPreferenceConstants.USE_TASK_EDITOR, true);
        store.setDefault(IPreferenceConstants.USE_MYLYN_TEAM_COMMENT, false);
    }

}
