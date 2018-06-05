package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 - 2005 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PartInitException;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.submitted.SubmittedChangelistTable;

/**
 * Submitted changelist view
 */
public class SubmittedView extends PerforceProjectView{
    @Override
    public SubmittedViewControl getPerforceViewControl() {
        return (SubmittedViewControl) super.getPerforceViewControl();
    }


    /**
     * The ID for this view
     */
    public static final String VIEW_ID = "com.perforce.team.ui.SubmittedChangelistView"; //$NON-NLS-1$

    /**
     * Shows the submitted changelist view
     * 
     * @return - shown view
     */
    public static SubmittedView showView() {
        try {
            return (SubmittedView) PerforceUIPlugin.getActivePage().showView(
                    VIEW_ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return null;
    }

    /**
     * Get the tree viewer
     * 
     * @return - tree viewer
     */
    public TreeViewer getViewer() {
    	if(getPerforceViewControl()!=null)
    		return getPerforceViewControl().getViewer();
    	return null;
    }

    /**
     * Gets the text in the change detail field
     * 
     * @return - change details text field value
     */
    public String getChangeDetails() {
        return getPerforceViewControl().getChangeDetails();
    }

    /**
     * Gets the text in the date detail field
     * 
     * @return - date details text field value
     */
    public String getDateDetails() {
        return getPerforceViewControl().getDateDetails();
    }

    /**
     * Gets the text in the cleint detail field
     * 
     * @return - client details text field value
     */
    public String getClientDetails() {
        return getPerforceViewControl().getClientDetails();
    }

    /**
     * Gets the text in the user detail field
     * 
     * @return - user details text field value
     */
    public String getUserDetail() {
        return getPerforceViewControl().getUserDetail();
    }

    /**
     * Gets the text in the files description field
     * 
     * @return - description details text field value
     */
    public String getDescriptionDetail() {
        return getPerforceViewControl().getDescriptionDetail();
    }


    /**
     * Shows the changelists for a p4 resource
     * 
     * @param resource
     */
    public void showChangelists(IP4Resource resource) {
        getPerforceViewControl().showChangelists(resource);
    }

    /**
     * Schedule a showing of the changelists associated with the specified
     * resource. This method schedules a UI job with this view's scheduling
     * rule.
     * 
     * @param resource
     */
    public void scheduleShowChangelists(final IP4Resource resource) {
        getPerforceViewControl().scheduleShowChangelists(resource);
    }

    /**
     * Get the changelists currently displayed
     * 
     * @return - array of submitted changelists
     */
    public IP4SubmittedChangelist[] getChangelists() {
    	if(getPerforceViewControl()!=null)
    		return getPerforceViewControl().getChangelists();
    	return null;
    }

    /**
     * Show display details for selected submitted changelist
     * 
     * @param show
     */
    public void showDisplayDetails(boolean show) {
        getPerforceViewControl().showDisplayDetails(show);
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
     * Shows the next amount of submitted changelists
     */
    public void showMore() {
        getPerforceViewControl().showMore();
    }

    /**
     * Get the underlying submitted changelist table widget
     * 
     * @return - submitted changelist table
     */
    public SubmittedChangelistTable getChangelistTable() {
    	if(getPerforceViewControl()!=null)
    		return getPerforceViewControl().getChangelistTable();
    	return null;
    }

    @Override
    protected SubmittedViewControl createViewControl(IPerforceView view) {
        return new SubmittedViewControl(view);
    }
}
