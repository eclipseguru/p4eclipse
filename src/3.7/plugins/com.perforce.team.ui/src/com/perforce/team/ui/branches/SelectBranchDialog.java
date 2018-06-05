/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.dialogs.PerforceDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class SelectBranchDialog extends PerforceDialog {

    private IP4Branch selected = null;
    private IP4Connection connection;
    private BranchesViewer branchesViewer;

    /**
     * Create a dialog to select a branch
     * 
     * @param parent
     * @param connection
     */
    public SelectBranchDialog(Shell parent, IP4Connection connection) {
        super(parent, Messages.SelectBranchDialog_DialogTitle);
        setModalResizeStyle();
        this.connection = connection;
    }

    /**
     * Get selected branch
     * 
     * @return - branch
     */
    public IP4Branch getSelected() {
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
        branchesViewer = new BranchesViewer();
        branchesViewer.createControl(c, this.connection, false, false);
        this.branchesViewer.getViewer().addDoubleClickListener(
                new IDoubleClickListener() {

                    public void doubleClick(DoubleClickEvent event) {
                        updateSelection();
                        close();
                    }
                });
        return c;
    }

    private void updateSelection() {
        IStructuredSelection selection = (IStructuredSelection) this.branchesViewer
                .getViewer().getSelection();
        if (selection.getFirstElement() instanceof IP4Branch) {
            this.selected = (IP4Branch) selection.getFirstElement();
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
