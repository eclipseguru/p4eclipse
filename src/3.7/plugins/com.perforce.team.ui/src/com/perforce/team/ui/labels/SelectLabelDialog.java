/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.labels;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.ui.dialogs.PerforceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SelectLabelDialog extends PerforceDialog {

    private IP4Label selected = null;
    private IP4Connection connection;
    private LabelsViewer labelsViewer;

    /**
     * @param parent
     * @param connection
     */
    public SelectLabelDialog(Shell parent, IP4Connection connection) {
        super(parent, Messages.SelectLabelDialog_SelectLabel);
        setModalResizeStyle();
        this.connection = connection;
    }

    /**
     * Get selected label
     * 
     * @return - label
     */
    public IP4Label getSelected() {
        return this.selected;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                false);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        labelsViewer = new LabelsViewer();
        labelsViewer.createControl(c, this.connection, false, false);
        this.labelsViewer.getViewer().addDoubleClickListener(
                new IDoubleClickListener() {

                    public void doubleClick(DoubleClickEvent event) {
                        updateSelection();
                        close();
                    }
                });
        return c;
    }

    private void updateSelection() {
        IStructuredSelection selection = (IStructuredSelection) this.labelsViewer
                .getViewer().getSelection();
        if (selection.getFirstElement() instanceof IP4Label) {
            this.selected = (IP4Label) selection.getFirstElement();
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        updateSelection();
        super.okPressed();
    }

}
