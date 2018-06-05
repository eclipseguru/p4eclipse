/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseCompletionProposal implements ICompletionProposal,
        ICompletionProposalExtension6 {

    private String display;
    private String replacement;
    private int offset;
    private int length;
    private int cursorPosition;
    private Image image;

    /**
     * Creates a new completion proposal based on the provided information. The
     * replacement string is considered being the display string too. All
     * remaining fields are set to <code>null</code>.
     * 
     * @param replacementString
     *            the actual string to be inserted into the document
     * @param replacementOffset
     *            the offset of the text to be replaced
     * @param replacementLength
     *            the length of the text to be replaced
     * @param cursorPosition
     *            the position of the cursor following the insert relative to
     *            replacementOffset
     */
    public BaseCompletionProposal(String replacementString,
            int replacementOffset, int replacementLength, int cursorPosition) {
        this(replacementString, replacementOffset, replacementLength,
                cursorPosition, null, null);
    }

    /**
     * Creates a new completion proposal. All fields are initialized based on
     * the provided information.
     * 
     * @param replacementString
     *            the actual string to be inserted into the document
     * @param replacementOffset
     *            the offset of the text to be replaced
     * @param replacementLength
     *            the length of the text to be replaced
     * @param cursorPosition
     *            the position of the cursor following the insert relative to
     *            replacementOffset
     * @param image
     *            the image to display for this proposal
     * @param displayString
     *            the string to be displayed for the proposal
     */
    public BaseCompletionProposal(String replacementString,
            int replacementOffset, int replacementLength, int cursorPosition,
            Image image, String displayString) {
        Assert.isNotNull(replacementString);
        Assert.isTrue(replacementOffset >= 0);
        Assert.isTrue(replacementLength >= 0);
        Assert.isTrue(cursorPosition >= 0);

        this.replacement = replacementString;
        this.offset = replacementOffset;
        this.length = replacementLength;
        this.cursorPosition = cursorPosition;
        this.image = image;
        this.display = displayString;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
     */
    public void apply(IDocument document) {
        try {
            document.replace(this.offset, this.length, this.replacement);
        } catch (BadLocationException x) {
            // Ignore exception
        }
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
        return this.display;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
     */
    public Image getImage() {
        return this.image;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
     */
    public Point getSelection(IDocument document) {
        return new Point(this.offset + this.cursorPosition, 0);
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension6#getStyledDisplayString()
     */
    public StyledString getStyledDisplayString() {
        return new StyledString(getDisplayString());
    }

}
