/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.TextUtils;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel.Line;
import com.perforce.team.ui.timelapse.IAnnotateModel;
import com.perforce.team.ui.timelapse.IAnnotateModel.IModelListener;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class MinimalRuler extends TextRuler implements IModelListener {

    /**
     * Current revision
     */
    protected IP4Revision revision;

    /**
     * Annotate model
     */
    protected ITextAnnotateModel model;

    /**
     * Lines of current revision
     */
    protected Line[] lines;

    /**
     * Last changelist id
     */
    protected int latest = -1;

    /**
     * @param model
     * 
     */
    public MinimalRuler(ITextAnnotateModel model) {
        this.model = model;
        this.model.addListener(this);
        loaded(this.model);
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel.IModelListener#loaded(com.perforce.team.ui.timelapse.IAnnotateModel)
     */
    public void loaded(IAnnotateModel model) {
        this.latest = model.getRevisionId(model.getLatest());
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#handleDispose()
     */
    @Override
    protected void handleDispose() {
        super.handleDispose();
        this.model.removeListener(this);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#computeMaxNumber()
     */
    @Override
    protected int computeMaxNumber() {
        int max = 0;
        for (int i = 0; i < this.lines.length; i++) {
            String text = getLineText(i);
            if (text != null) {
                max = Math.max(max, text.length());
            }
        }
        return max;
    }

    /**
     * Generate revisions
     */
    protected void generateRevisions() {
        if (model != null && this.revision != null) {
            this.lines = model.getLines(this.revision);
        }
    }

    /**
     * 
     * @param revision
     */
    public void setRevision(IP4Revision revision) {
        if (revision != this.revision) {
            this.revision = revision;
            generateRevisions();
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextRuler#paintLines(int, int,
     *      int, int, org.eclipse.swt.graphics.GC,
     *      org.eclipse.swt.widgets.Display)
     */
    @Override
    protected void paintLines(int startLine, int endLine, int width, int y,
            GC gc, Display display) {
        if (this.lines == null) {
            return;
        }
        ITextViewer viewer = getViewer();
        StyledText text = getTextWidget();
        Line lineModel = null;
        for (int line = startLine; line < endLine; line++) {
            boolean draw = false;
            int widgetLine = TextUtils.modelLineToWidgetLine(viewer, line);
            if (widgetLine == -1) {
                continue;
            }

            int lineHeight = text.getLineHeight(text
                    .getOffsetAtLine(widgetLine));

            paintBackground(line, gc, y, width, lineHeight);
            Line current = null;
            if (line < this.lines.length) {
                current = this.lines[line];
                if (lineModel == null) {
                    draw = true;
                } else {
                    if (current.lower != lineModel.lower
                            || current.upper != lineModel.upper) {
                        draw = true;
                    }
                }
            }
            if (y + lineHeight >= 0) {
                if (draw) {
                    paintLine(line, y, lineHeight, gc, display);
                }
                lineModel = current;
            }
            y += lineHeight;
        }
    }

}
