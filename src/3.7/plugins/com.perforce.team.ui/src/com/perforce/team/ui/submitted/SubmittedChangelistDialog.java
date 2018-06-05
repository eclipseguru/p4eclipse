/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.dialogs.PerforceDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedChangelistDialog extends PerforceDialog {

    private Composite displayArea;
    private SubmittedChangelistTable table;

    private IP4Connection connection = null;

    private IP4SubmittedChangelist[] selected = new IP4SubmittedChangelist[0];

    private boolean single = false;

    /**
     * @param parent
     * @param connection
     */
    public SubmittedChangelistDialog(Shell parent, IP4Connection connection) {
        super(parent,
                Messages.SubmittedChangelistDialog_SelectSubmittedChangelist);
        setModalResizeStyle();
        this.connection = connection;
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
        displayArea = new Composite(c, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        daData.heightHint = 500;
        daData.widthHint = 600;
        displayArea.setLayoutData(daData);
        table = new SubmittedChangelistTable(null, null, null, false);
        table.createPartControl(displayArea, single ? SWT.SINGLE : SWT.MULTI);
        table.showDisplayDetails(true);
        table.showChangelists(connection);
        table.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        return c;
    }

    /**
     * Get table widget
     * 
     * @return - submitted changelist tab
     */
    public SubmittedChangelistTable getTable() {
        return this.table;
    }

    /**
     * Update selected changelists
     */
    public void updateSelectedChangelists() {
        selected = table.getSelectedChangelists();
    }

    /**
     * Get the selected submitted changelists
     * 
     * @return - array of submitted changelists
     */
    public IP4SubmittedChangelist[] getSelected() {
        return this.selected;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        updateSelectedChangelists();
        super.okPressed();
    }

    /**
     * Set whether single or multi selection will be used
     * 
     * @param single
     */
    public void setSingle(boolean single) {
        this.single = single;
    }

}
