/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class UserRuler extends MinimalRuler {

    /**
     * User ruler
     * 
     * @param model
     */
    public UserRuler(ITextAnnotateModel model) {
        super(model);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#computeIndentations()
     */
    @Override
    protected void computeIndentations() {
        super.computeIndentations();
        // Remove all indentation except the first since we want to left-align
        // the author text
        for (int i = 1; i < fIndentation.length; i++) {
            fIndentation[i] = 0;
        }
    }

}
