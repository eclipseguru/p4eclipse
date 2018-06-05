/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PartInitException;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.PerforceProjectView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class BranchesView extends PerforceProjectView {

    /**
     * ID of view
     */
    public static final String VIEW_ID = "com.perforce.team.ui.BranchesView"; //$NON-NLS-1$

    /**
     * Get the branches view
     * 
     * @return - Branches view
     */
    public static BranchesView getView() {
        return (BranchesView) PerforceUIPlugin.getActivePage()
                .findView(VIEW_ID);
    }

    /**
     * Shows the branches view
     * 
     * @return - shown view
     */
    public static BranchesView showView() {
        try {
            return (BranchesView) PerforceUIPlugin.getActivePage().showView(
                    VIEW_ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return null;
    }

    @Override
    protected BranchesViewControl createViewControl(IPerforceView view) {
        return new BranchesViewControl(view);
    }
    
    @Override
    public BranchesViewControl getPerforceViewControl() {
        return (BranchesViewControl) super.getPerforceViewControl();
    }

    /**
     * Gets the table control used by the branches view
     * 
     * @return - table control or null if branch list failed creation
     */
    public Table getTableControl() {
        return getPerforceViewControl().getTableControl();
    }

    /**
     * Gets the table viewer
     * 
     * @return - table viewer
     */
    public TableViewer getTableViewer() {
        return getPerforceViewControl().getTableViewer();
    }

    /**
     * Is the branches view loading?
     * 
     * @return - true if loading
     */
    public boolean isLoading() {
        return getPerforceViewControl().isLoading();
    }

    /**
     * Refreshes the branches view
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
     * Shows the next amount of branches set to be retrieved
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
     * Get branch details widget
     * 
     * @return - branch details
     */
    public BranchWidget getBranchDetails() {
        return getPerforceViewControl().getBranchDetails();
    }

}