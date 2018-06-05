/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IContentAssistProvider {

    /**
     * Get proposals for a specified ui and model context
     * 
     * @param modelContext
     * @param uiContext
     * 
     * @param context
     * @return - array of proposals
     */
    IProposal[] getProposals(Object modelContext, Object uiContext);

}
