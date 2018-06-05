/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.preferences;

import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
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
        IPreferenceStore store = P4BranchGraphPlugin.getDefault()
                .getPreferenceStore();

        store.setDefault("org.eclipse.gef.pdock", PositionConstants.WEST); //$NON-NLS-1$
        store.setDefault("org.eclipse.gef.pstate", //$NON-NLS-1$
                FlyoutPaletteComposite.STATE_PINNED_OPEN);
        store.setDefault("org.eclipse.gef.psize", 175); //$NON-NLS-1$
        store.setDefault(IPreferenceConstants.SHOW_TOOLTIPS, true);
    }

}
