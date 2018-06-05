/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseContentAssistProcessor implements
        IContentAssistProcessor {

    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage() {
        return null;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer,
     *      int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer,
            int offset) {
        return null;
    }

    /**
     * Get prefix
     * 
     * @param document
     * @param offset
     * @return prefix
     */
    protected String getPrefix(IDocument document, int offset) {
        StringBuilder builder = new StringBuilder();
        try {
            offset--;
            char c = document.getChar(offset);
            while (!Character.isWhitespace(c)) {
                builder.insert(0, c);
                offset--;
                c = document.getChar(offset);
            }
        } catch (BadLocationException e) {
        }
        return builder.toString();
    }

}
