/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.ui.editor.BaseCompletionProposal;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchCompletionProposal extends BaseCompletionProposal {

    private Branch branch;

    /**
     * @param branch
     * @param replacementOffset
     * @param replacementLength
     * @param cursorPosition
     * @param image
     */
    public BranchCompletionProposal(Branch branch, int replacementOffset,
            int replacementLength, int cursorPosition, Image image) {
        super(branch.getName(), replacementOffset, replacementLength,
                cursorPosition, image, branch.getName());
        this.branch = branch;
    }

    /**
     * @see com.perforce.team.ui.editor.BaseCompletionProposal#getStyledDisplayString()
     */
    @Override
    public StyledString getStyledDisplayString() {
        StyledString display = new StyledString(branch.getName());
        return display;
    }

}