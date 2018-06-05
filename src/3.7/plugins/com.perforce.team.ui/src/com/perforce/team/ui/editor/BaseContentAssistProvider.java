/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class BaseContentAssistProvider implements IContentAssistProvider {

    /**
     * EMPTY proposals array
     */
    public static final IProposal[] EMPTY = new IProposal[0];

    /**
     * @see com.perforce.team.ui.editor.IContentAssistProvider#getProposals(java.lang.Object,
     *      java.lang.Object)
     */
    public IProposal[] getProposals(Object modelContext, Object uiContext) {
        return EMPTY;
    }

}
