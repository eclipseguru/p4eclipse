/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LoadingProposal implements ICompletionProposal,
        ICompletionProposalExtension4 {

    private SharedResources resources;

    /**
     * Create new loading proposal
     * 
     * @param resources
     */
    public LoadingProposal(SharedResources resources) {
        this.resources = resources;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
     */
    public void apply(IDocument document) {
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
        return null;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation() {
        return null;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
     */
    public String getDisplayString() {
        return Messages.LoadingProposal_Loading;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
     */
    public Image getImage() {
        return this.resources.getImage(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_LOADING));
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
     */
    public Point getSelection(IDocument document) {
        return null;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension4#isAutoInsertable()
     */
    public boolean isAutoInsertable() {
        return false;
    }

}
