/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel.Line;
import com.perforce.team.ui.timelapse.IAnnotateModel.Type;

import org.eclipse.jface.text.source.LineNumberRulerColumn;

/**
 * 
 * Drawing taken from {@link LineNumberRulerColumn}
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class RevisionRuler extends MinimalRuler {

    /**
     * Ruler display type
     */
    private Type type = Type.REVISION;

    /**
     * @param model
     * 
     */
    public RevisionRuler(ITextAnnotateModel model) {
        super(model);
    }

    /**
     * Set the type
     * 
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#getLineText(int)
     */
    @Override
    protected String getLineText(int line) {
        String text = null;
        if (line > -1 && line < this.lines.length) {
            text = createDisplayString(this.lines[line], this.revision, line,
                    this.type);
        }
        return text;
    }

    /**
     * Create display string
     * 
     * @param revision
     * @return - display string
     */
    protected String createDisplayString(int revision) {
        return Integer.toString(revision);
    }

    /**
     * Create display string
     * 
     * @param line
     * @param revision
     * @param lineNumber
     * @param type
     * @return - display string
     */
    protected abstract String createDisplayString(Line line,
            IP4Revision revision, int lineNumber, Type type);

}
