/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import org.eclipse.ui.PartInitException;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.PerforceProjectView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelveView extends PerforceProjectView {

    /**
     * VIEW_ID
     */
    public static final String VIEW_ID = "com.perforce.team.ui.ShelveView"; //$NON-NLS-1$

    /**
     * Get the shelve view
     * 
     * @return - Shelve view
     */
    public static ShelveView getView() {
        return (ShelveView) PerforceUIPlugin.getActivePage()
                .findView(VIEW_ID);
    }

    /**
     * Shows the shelve view
     * 
     * @return - shown view
     */
    public static ShelveView showView() {
        try {
            return (ShelveView) PerforceUIPlugin.getActivePage().showView(
                    VIEW_ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return null;
    }
    
    @Override
    protected ShelveViewControl createViewControl(IPerforceView view) {
        return new ShelveViewControl(view);
    }
    
    @Override
    public ShelveViewControl getPerforceViewControl() {
        return (ShelveViewControl) super.getPerforceViewControl();
    }
    
    /**
     * Is this view loading?
     * 
     * @return - true if loading, false otherwise
     */
    public boolean isLoading() {
        return getPerforceViewControl().isLoading();
    }

    /**
     * Get the underlying shelve table widget
     * 
     * @return - shelve table
     */
    public ShelveTable getTable() {
        return getPerforceViewControl().getTabel();
    }

}
