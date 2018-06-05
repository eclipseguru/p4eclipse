/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.labels;

import org.eclipse.ui.PartInitException;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.PerforceProjectView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelsView extends PerforceProjectView {

    /**
     * ID of view
     */
    public static final String VIEW_ID = "com.perforce.team.ui.LabelsView"; //$NON-NLS-1$

    /**
     * Get the labels view
     * 
     * @return - Labels view
     */
    public static LabelsView getView() {
        return (LabelsView) PerforceUIPlugin.getActivePage().findView(VIEW_ID);
    }

    /**
     * Shows the labels view
     * 
     * @return - shown view
     */
    public static LabelsView showView() {
        try {
            return (LabelsView) PerforceUIPlugin.getActivePage().showView(
                    VIEW_ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return null;
    }


    @Override
    protected LabelsViewControl createViewControl(IPerforceView view) {
        return new LabelsViewControl(view);
    }
    
    @Override
    public LabelsViewControl getPerforceViewControl() {
        return (LabelsViewControl) super.getPerforceViewControl();
    }

    /**
     * Is the labels view loading?
     * 
     * @return - true if loading
     */
    public boolean isLoading() {
        return getPerforceViewControl().isLoading();
    }

    /**
     * Refreshes the labels view
     */
    public void refresh() {
        getPerforceViewControl().refresh();
    }

    /**
     * Refresh the retrieve count
     */
    public void refreshRetrieveCount() {
        getPerforceViewControl().refreshRetrieveCount();
    }

    /**
     * Shows the next amount of labels set to be retrieved
     */
    public void showMore() {
        getPerforceViewControl().showMore();
    }

    /**
     * Show/hide details section
     * 
     * @param show
     */
    public void showDetails(boolean show) {
        getPerforceViewControl().showDetails(show);
    }

    /**
     * Get label details widget
     * 
     * @return - label details
     */
    public LabelWidget getLabelDetails() {
        return getPerforceViewControl().getLabelDetails();
    }

}