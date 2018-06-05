/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class LowerUserRuler extends UserRuler {

    /**
     * @param model
     */
    public LowerUserRuler(ITextAnnotateModel model) {
        super(model);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#getLineText(int)
     */
    @Override
    protected String getLineText(int line) {
        String text = null;
        if (line > -1 && line < this.lines.length) {
            text = this.model.getAuthor(this.lines[line].lower);
        }
        return text;
    }
}
