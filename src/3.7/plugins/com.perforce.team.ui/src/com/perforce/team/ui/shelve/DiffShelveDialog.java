/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DiffShelveDialog extends P4StatusDialog {

    private FileShelfWidget widget;

    private IP4File file;
    private IP4ShelveFile[] shelves;
    private IP4ShelveFile selected;
    private boolean revert = false;

    /**
     * @param parent
     * @param file
     * @param shelves
     */
    public DiffShelveDialog(Shell parent, IP4File file, IP4ShelveFile[] shelves) {
        super(parent);
        setTitle(MessageFormat.format(
                Messages.DiffShelveDialog_CompareWithShelvedVersion,
                file.getName()));
        setModalResizeStyle();
        this.file = file;
        this.shelves = shelves;
    }

    /**
     * @see com.perforce.team.ui.dialogs.P4StatusDialog#getSectionName()
     */
    @Override
    protected String getSectionName() {
        return FileShelfWidget.SECTION_NAME;
    }

    /**
     * Revert file before unshelving?
     * 
     * @return - true to revert
     */
    public boolean revert() {
        return revert;
    }

    /**
     * Get selected shelve file
     * 
     * @return - shelve file
     */
    public IP4ShelveFile getSelected() {
        return this.selected;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        this.selected = widget.getSelectedFile();
        this.revert = widget.isRevert();
        super.okPressed();
    }

    private void validate() {
        if (widget.getViewer().getSelection().isEmpty()) {
            setErrorMessage(Messages.DiffShelveDialog_MustSelectShelvedFile);
        } else {
            setErrorMessage(null);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        widget = new FileShelfWidget(this.shelves, file, MessageFormat.format(
                Messages.DiffShelveDialog_ComparingFile, file.getRemotePath()));
        widget.createControl(c, false);
        widget.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        widget.getViewer().addSelectionChangedListener(
                new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        validate();
                    }
                });
        validate();

        return c;
    }

}
