/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.preferences;

import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /**
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = PerforceUiFolderPlugin.getDefault()
                .getPreferenceStore();

        store.setDefault(IPreferenceConstants.SHOW_CONTENT, true);
        store.setDefault(IPreferenceConstants.SHOW_UNIQUE, true);
        store.setDefault(IPreferenceConstants.SHOW_IDENTICAL, false);

        store.setDefault(IPreferenceConstants.COMPARE_DISPLAY_MODE,
                Type.COMPRESSED.toString());

        store.setDefault(IPreferenceConstants.UNIQUE_COLOR,
                StringConverter.asString(new RGB(255, 230, 188)));

        store.setDefault(IPreferenceConstants.CONTENT_COLOR,
                StringConverter.asString(new RGB(231, 230, 244)));

        store.setDefault(IPreferenceConstants.DIFF_UNIQUE_COLOR,
                StringConverter.asString(new RGB(255, 230, 188)));

        store.setDefault(IPreferenceConstants.DIFF_CONTENT_COLOR,
                StringConverter.asString(new RGB(231, 230, 244)));

        store.setDefault(IPreferenceConstants.COMPARE_SELECT_MODE, true);
    }
}
