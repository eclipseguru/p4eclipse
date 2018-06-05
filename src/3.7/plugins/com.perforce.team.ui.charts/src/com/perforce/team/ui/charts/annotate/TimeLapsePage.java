/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.annotate;

import com.perforce.team.ui.text.timelapse.ITextAnnotateModel;
import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;
import com.perforce.team.ui.timelapse.IAnnotateModel;
import com.perforce.team.ui.timelapse.ITimeLapseListener;
import com.perforce.team.ui.timelapse.TimeLapseEditor;
import com.perforce.team.ui.timelapse.IAnnotateModel.IModelListener;

import org.eclipse.ui.part.Page;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class TimeLapsePage extends Page implements IModelListener,
        ITimeLapseListener {

    /**
     * Editor
     */
    protected TextTimeLapseEditor editor = null;

    /**
     * Text annotate model
     */
    protected ITextAnnotateModel model = null;

    /**
     * Set the editor to bind this page to
     * 
     * @param editor
     */
    public void setEditor(TextTimeLapseEditor editor) {
        if (editor != null) {
            this.editor = editor;
            this.model = editor.getAnnotateModel();
            if (this.model != null) {
                this.model.addListener(this);
            }
            this.editor.addListener(this);
        }
    }

    /**
     * @see org.eclipse.ui.part.Page#dispose()
     */
    @Override
    public void dispose() {
        if (this.model != null) {
            this.model.removeListener(this);
        }
        if (this.editor != null) {
            this.editor.removeListener(this);
        }
        super.dispose();
    }

    /**
     * Model refreshed callback
     */
    protected abstract void modelRefreshed();

    /**
     * @see com.perforce.team.ui.timelapse.IAnnotateModel.IModelListener#loaded(com.perforce.team.ui.timelapse.IAnnotateModel)
     */
    public void loaded(IAnnotateModel model) {
        if (model != null) {
            IAnnotateModel current = this.model;
            if (current != null) {
                current.removeListener(this);
            }
            this.model = (ITextAnnotateModel) model;
            this.model.addListener(this);
            modelRefreshed();
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITimeLapseListener#loaded(com.perforce.team.ui.timelapse.TimeLapseEditor)
     */
    public void loaded(TimeLapseEditor editor) {
        if (editor instanceof TextTimeLapseEditor) {
            loaded(((TextTimeLapseEditor) editor).getAnnotateModel());
        }
    }
}
