/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import org.eclipse.jface.text.IDocument;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class LineNumberRuler extends TextRuler {

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#computeMaxNumber()
     */
    @Override
    protected int computeMaxNumber() {
        IDocument document = getViewer().getDocument();
        return document != null ? computeNumberOfDigits(document
                .getNumberOfLines()) : 2;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#getLineText(int)
     */
    @Override
    protected String getLineText(int line) {
        return Integer.toString(line + 1);
    }

}
