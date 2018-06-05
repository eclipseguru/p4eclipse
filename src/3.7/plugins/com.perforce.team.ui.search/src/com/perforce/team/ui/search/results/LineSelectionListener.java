/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LineSelectionListener implements IPartListener {

    private int lineNumber = -1;

    /**
     * Create a line selection listener
     * 
     * @param lineNumber
     */
    public LineSelectionListener(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
        if (part instanceof ITextEditor && this.lineNumber >= 0) {
            ITextEditor editor = (ITextEditor) part;
            IDocumentProvider provider = editor.getDocumentProvider();
            if (provider != null) {
                IDocument document = provider.getDocument(editor
                        .getEditorInput());
                if (document != null) {
                    try {
                        int offset = document.getLineOffset(lineNumber);
                        int length = document.getLineLength(lineNumber);
                        if (length > 1) {
                            length--;
                        }
                        if (offset >= 0) {
                            editor.selectAndReveal(offset, length);
                        }
                    } catch (BadLocationException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    /**
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {

    }

    /**
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {

    }

    /**
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {

    }

    /**
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {

    }

}
