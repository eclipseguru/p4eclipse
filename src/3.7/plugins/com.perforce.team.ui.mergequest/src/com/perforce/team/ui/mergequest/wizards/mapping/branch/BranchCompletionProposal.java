/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.ui.editor.BaseCompletionProposal;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchCompletionProposal extends BaseCompletionProposal {

    private IP4Branch branch;
    private Runnable applyCallback;
    private SimpleDateFormat pattern;

    /**
     * @param branch
     * @param replacementOffset
     * @param replacementLength
     * @param cursorPosition
     * @param image
     * @param callback
     */
    public BranchCompletionProposal(IP4Branch branch, int replacementOffset,
            int replacementLength, int cursorPosition, Image image,
            Runnable callback) {
        this(branch, replacementOffset, replacementLength, cursorPosition,
                image);
        this.applyCallback = callback;
        this.pattern = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$
    }

    /**
     * @param branch
     * @param replacementOffset
     * @param replacementLength
     * @param cursorPosition
     * @param image
     */
    public BranchCompletionProposal(IP4Branch branch, int replacementOffset,
            int replacementLength, int cursorPosition, Image image) {
        super(branch.getName(), replacementOffset, replacementLength,
                cursorPosition, image, branch.getName());
        this.branch = branch;
    }

    /**
     * @see com.perforce.team.ui.editor.BaseCompletionProposal#apply(org.eclipse.jface.text.IDocument)
     */
    @Override
    public void apply(IDocument document) {
        super.apply(document);
        if (this.applyCallback != null) {
            this.applyCallback.run();
        }
    }

    /**
     * @see com.perforce.team.ui.editor.BaseCompletionProposal#getStyledDisplayString()
     */
    @Override
    public StyledString getStyledDisplayString() {
        StyledString display = new StyledString(branch.getName());
        String owner = this.branch.getOwner();
        if (owner != null) {
            display.append(": ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
            display.append(owner, StyledString.QUALIFIER_STYLER);
        }
        Date access = this.branch.getAccessTime();
        if (access != null) {
            display.append(' ');
            display.append(this.pattern.format(access),
                    StyledString.QUALIFIER_STYLER);
        }
        return display;
    }
}
