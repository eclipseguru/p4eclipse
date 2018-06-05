/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BulkJobErrorDialog extends MessageDialog {

    private String errors = null;

    /**
     * @param parentShell
     * @param errors
     */
    public BulkJobErrorDialog(Shell parentShell, String errors) {
        super(parentShell, Messages.BulkJobPage_ErrorUpdatingJobs, null,
                Messages.BulkJobPage_UpdateErrors, MessageDialog.ERROR,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
        this.errors = errors;
    }

    /**
     * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createCustomArea(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        daData.heightHint = 300;
        displayArea.setLayoutData(daData);
        SourceViewer viewer = new SourceViewer(displayArea, null, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
        viewer.getTextWidget().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        if (this.errors == null) {
            this.errors = ""; //$NON-NLS-1$
        }
        viewer.setDocument(new Document(this.errors));
        return displayArea;
    }

}
