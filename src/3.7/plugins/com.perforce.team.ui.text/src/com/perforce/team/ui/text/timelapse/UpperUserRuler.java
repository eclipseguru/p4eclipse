/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel.Line;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UpperUserRuler extends UserRuler {

    /**
     * @param model
     */
    public UpperUserRuler(ITextAnnotateModel model) {
        super(model);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#getLineText(int)
     */
    @Override
    protected String getLineText(int line) {
        String text = null;
        if (line > -1 && line < this.lines.length) {
            Line currentLine = this.lines[line];
            int key = model.getRevisionId(revision);
            if (latest == key || latest == currentLine.upper) {
                text = ""; //$NON-NLS-1$
            } else {
                IP4Revision upperRev = model.getRevisionById(currentLine.upper);
                upperRev = model.getNext(upperRev);
                if (upperRev != null) {
                    text = upperRev.getAuthor();
                }
            }
        }
        return text;
    }

}
