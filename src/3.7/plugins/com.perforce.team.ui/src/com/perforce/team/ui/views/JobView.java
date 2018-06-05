package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PartInitException;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.JobsDialog;

/**
 * P4 Jobs view
 */
public class JobView extends PerforceProjectView{

    /**
     * ID of view
     */
    public static final String VIEW_ID = "com.perforce.team.ui.JobView"; //$NON-NLS-1$

    /**
     * Get the jobs view
     * 
     * @return - Job view
     */
    public static JobView getView() {
        return (JobView) PerforceUIPlugin.getActivePage().findView(VIEW_ID);
    }

    /**
     * Shows the job view
     * 
     * @return - shown view
     */
    public static JobView showView() {
        try {
            return (JobView) PerforceUIPlugin.getActivePage().showView(VIEW_ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return null;
    }

    @Override
    protected JobViewControl createViewControl(IPerforceView view) {
        return new JobViewControl(view);
    }
    
    @Override
    public JobViewControl getPerforceViewControl() {
        return (JobViewControl) super.getPerforceViewControl();
    }

    /**
     * Gets the table control used by the jobs view
     * 
     * @return - table control or null if job list failed creation
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
     * Gets the underlying jobs dialog
     * 
     * @return - jobs dialog
     */
    public JobsDialog getJobsDialog() {
        return getPerforceViewControl().getJobsDialog();
    }

    /**
     * Is the jobs view loading?
     * 
     * @return - true if loading
     */
    public boolean isLoading() {
        return getPerforceViewControl().isLoading();
    }

    /**
     * Refreshes the job view
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
     * Shows the need amount of jobs set to be retrieved
     */
    public void showMore() {
        getPerforceViewControl().showMore();
    }

}
