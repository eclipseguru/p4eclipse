/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.preferences;

import com.perforce.team.ui.charts.P4ChartUiPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /**
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = P4ChartUiPlugin.getDefault()
                .getPreferenceStore();

        store.setDefault(IPreferenceConstants.EXPAND_HISTOGRAM, true);
    }

}
