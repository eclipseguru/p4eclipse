package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * View showing pending changeslists and open files.
 */
public class PendingView extends PerforceProjectView {

    /**
     * The ID for this view
     */
    public static final String VIEW_ID = "com.perforce.team.ui.PendingChangelistView"; //$NON-NLS-1$

    /**
     * Get the pending view
     * 
     * @return - pending view
     */
    public static PendingView getView() {
        return (PendingView) PerforceUIPlugin.getActivePage().findView(VIEW_ID);
    }

    /**
     * Shows the pending view
     * 
     * @return - shown view
     */
    public static PendingView showView() {
        try {
            return (PendingView) PerforceUIPlugin.getActivePage().showView(
                    VIEW_ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return null;
    }


    private IMemento memento;
    
	private IContextActivation fContextActivation;

    /**
     * Initialize this view
     * 
     * @param site
     * @param memento
     * @throws PartInitException
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento=memento;
    }
    
    @Override
    public PendingViewControl getPerforceViewControl() {
        return (PendingViewControl) super.getPerforceViewControl();
    }

    /**
     * Save the state of this view
     * 
     * @param memento
     */
    @Override
    public void saveState(IMemento memento) {
        getPerforceViewControl().saveState(memento);
    }

    protected IPerforceViewControl createViewControl(IPerforceView view){
        PendingViewControl control = new PendingViewControl(view);
        control.setMemento(this.memento);

        IContextService ctxService = (IContextService) getSite().getService(
				IContextService.class);
        if (ctxService != null) {
			fContextActivation = ctxService
					.activateContext(PerforceUIPlugin.CONTEXT_PENDING_VIEW);
		}
		
        return control;        
    }
    
    /**
     * Refresh this view
     */
    public void refresh() {
        getPerforceViewControl().refresh();
    }

    /**
     * Gets the underlying tree viewer showing the pending changelists
     * 
     * @return - tree viewer
     */
    public TreeViewer getViewer() {
        return getPerforceViewControl().getViewer();
    }

    /**
     * Show other changes
     * 
     * @param show
     *            - true to show other changes, false to not
     */
    public void showOtherChanges(boolean show) {
        getPerforceViewControl().showOtherChanges(show);
    }


    /**
     * Is this view currently loading pending changelists?
     * 
     * @return true is loading the pending changelists, false otherwise
     */
    public boolean isLoading() {
        return getPerforceViewControl().isLoading();
    }

    @Override
    public void dispose() {
    	if (fContextActivation != null) {
    		IContextService ctxService = (IContextService) getSite()
    				.getService(IContextService.class);
    		if (ctxService != null) {
    			ctxService.deactivateContext(fContextActivation);
    		}
    	}
    	super.dispose();
    }
}
