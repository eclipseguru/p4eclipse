/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.labels;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelFilesDialog extends P4StatusDialog {

    private LabelFilesWidget newLabelWidget;
    private P4Collection collection;

    /**
     * @param parent
     * @param collection
     */
    public LabelFilesDialog(Shell parent, P4Collection collection) {
        super(parent, Messages.LabelFilesDialog_LabelFiles);
        setModalResizeStyle();
        setStatusLineAboveButtons(true);
        this.collection = collection;
    }

    /**
     * Delete files from label?
     * 
     * @return - true to delete, false to add
     */
    public boolean deleteFromLabel() {
        if (this.newLabelWidget != null) {
            return this.newLabelWidget.deleteFromLabel();
        }
        return false;
    }

    /**
     * Get selected label
     * 
     * @return - label name
     */
    public String getSelectedLabel() {
        if (this.newLabelWidget != null) {
            return this.newLabelWidget.getSelectedLabel();
        }
        return null;
    }

    /**
     * Get selected revision
     * 
     * @return - revision
     */
    public String getRevision() {
        if (this.newLabelWidget != null) {
            return this.newLabelWidget.getRevision();
        }
        return null;
    }

    /**
     * Get label files widget
     * 
     * @return - label files widget
     */
    public LabelFilesWidget getLabelFilesWidget() {
        return this.newLabelWidget;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        newLabelWidget = new LabelFilesWidget(collection.members(),
                collection.getType());
        newLabelWidget.setErrorDisplay(this);
        newLabelWidget.createControl(c);
        return c;
    }
}
