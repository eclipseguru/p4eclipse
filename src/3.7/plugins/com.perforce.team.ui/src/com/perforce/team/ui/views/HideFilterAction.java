/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.views;

import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class HideFilterAction extends Action {

    private IPreferenceStore store;
    private String pref;
    private IFilterView view;

    /**
     * Create a hide filters action
     * 
     * @param preference
     * @param view
     */
    public HideFilterAction(String preference, IFilterView view) {
        this(preference, view, PerforceUIPlugin.getPlugin()
                .getPreferenceStore());
    }

    /**
     * Create a hide filters action
     * 
     * @param preference
     * @param view
     * @param store
     */
    public HideFilterAction(String preference, IFilterView view,
            IPreferenceStore store) {
        super(Messages.HideFilterAction_HideFilters, IAction.AS_CHECK_BOX);
        this.pref = preference;
        this.view = view;
        this.store = store;
        setChecked(this.store.getBoolean(this.pref));
        if (isChecked()) {
            this.view.showFilters(false);
        }
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        view.showFilters(!isChecked());
        store.setValue(pref, isChecked());
    }

}
