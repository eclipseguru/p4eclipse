package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog box to display list of jobs from which the user may select some
 */
public class JobFixDialog extends PerforceDialog implements
        IJobDoubleClickListener {

    // Connection
    private IP4Connection con;

    // Jobs selected by user
    private IP4Job[] selectedJobs;

    // Job list pane
    JobsDialog jobPane;

    /**
     * Constructor.
     * 
     * @param parent
     *            parent window
     * @param con
     * @param title
     *            non-null
     */
    public JobFixDialog(Shell parent, IP4Connection con, String title) {
        super(parent, title);
        this.con = con;
        jobPane = new JobsDialog();
        jobPane.setAutoSelectFirstEntry(true);
        jobPane.setDoubleListener(this);
        setModalResizeStyle();
    }

    /**
     * Constructor.
     * 
     * @param parent
     *            parent window
     * @param con
     */
    public JobFixDialog(Shell parent, IP4Connection con) {
        this(parent, con, Messages.JobFixDialog_SelectJobsFixedByChangelist);
    }

    /**
     * Get the jobs selected by user.
     * 
     * @return jobs selected by user
     */
    public IP4Job[] getSelectedJobs() {
        return selectedJobs;
    }

    /**
     * OK button pressed
     */
    @Override
    protected void okPressed() {
        selectedJobs = jobPane.getSelectedJobs();
        super.okPressed();
    }

    /**
     * Create dialog box controls
     * 
     * @param parent
     * @return - main control
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        ((GridData) dialogArea.getLayoutData()).widthHint = 700;
        ((GridData) dialogArea.getLayoutData()).heightHint = 500;
        jobPane.createControl(dialogArea, con, true, true);
        jobPane.setKeywordFocus();
        return dialogArea;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // Do not create OK button as the default. This is so that the enter
        // gets sent to
        // the filter combo
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                false);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Returns true if jobs are currently loading
     * 
     * @return - true if loading
     */
    public boolean isLoading() {
        if (jobPane != null) {
            return jobPane.isLoading();
        }
        return false;
    }

    /**
     * Get underlying table view
     * 
     * @return - table view
     */
    public TableViewer getViewer() {
        if (jobPane != null) {
            return jobPane.getViewer();
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.dialogs.IJobDoubleClickListener#doubleClick()
     */
    public void doubleClick() {
        okPressed();
    }
}
