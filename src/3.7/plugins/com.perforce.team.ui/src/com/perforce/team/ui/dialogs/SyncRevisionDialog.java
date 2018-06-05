package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Sync revision dialog
 */
public class SyncRevisionDialog extends PerforceDialog {

    // Dialog controls
    private Button latestRadio;
    private Button revisionRadio;
    private Button otherRadio;
    private Text revisionText;
    private Text otherText;
    private Button forceSyncButton;
    private Button previewSyncButton;

    // Revision specification i.e #3 or @300
    private String revSpec;
    private boolean force = false;
    private boolean preview = false;

    private String initalText;
    private boolean isOther = false;

    /**
     * 
     * @param parent
     * @param initial
     * @param isOther
     */
    public SyncRevisionDialog(Shell parent, String initial, boolean isOther) {
        super(parent,
                Messages.SyncRevisionDialog_SyncFilesToRevLabelChangeOrDate);
        this.initalText = initial;
        this.isOther = isOther;
    }

    /**
     * 
     * @param parent
     */
    public SyncRevisionDialog(Shell parent) {
        this(parent, null, false);
    }

    /**
     * Get revision spec entered
     * 
     * @return - string rev spec
     */
    public String getRevSpec() {
        return revSpec;
    }

    /**
     * Return true if force sync was selected
     * 
     * @return - true if force sync selected
     */
    public boolean forceSync() {
        return force;
    }

    /**
     * Should a sync preview be done?
     * 
     * @return -true if preview selected, false otherwise
     */
    public boolean preview() {
        return this.preview;
    }

    /**
     * True if the force button is currently selected
     * 
     * @return - true if selected
     */
    public boolean forceSelected() {
        return this.forceSyncButton.getSelection();
    }

    /**
     * Set the force selected button
     * 
     * @param force
     */
    public void setForceSelected(boolean force) {
        this.forceSyncButton.setSelection(force);
    }

    private void select(boolean latest, boolean revision, boolean other) {
        latestRadio.setSelection(latest);
        revisionRadio.setSelection(revision);
        revisionText.setEnabled(revision);
        otherRadio.setSelection(other);
        otherRadio.setEnabled(other);
    }

    /**
     * Select latest revision option
     */
    public void selectLatest() {
        select(true, false, false);
    }

    /**
     * Is latest revision selected?
     * 
     * @return - true if latest revision selected
     */
    public boolean latestSelected() {
        return latestRadio.getSelection();
    }

    /**
     * Select specify revision option
     */
    public void selectRevision() {
        select(false, true, false);
    }

    /**
     * Is specific revision selected?
     * 
     * @return - true if specific revision selected
     */
    public boolean revisionSelected() {
        return revisionRadio.getSelection();
    }

    /**
     * Selec other revision option
     */
    public void selectOther() {
        select(false, false, true);
    }

    /**
     * Is other revision selected?
     * 
     * @return - true if other revision selected
     */
    public boolean otherSelected() {
        return otherRadio.getSelection();
    }

    /**
     * Set specific revision text
     * 
     * @param revision
     */
    public void setRevisionText(String revision) {
        if (revision != null) {
            revisionText.setText(revision);
        }
    }

    /**
     * Set other revision text
     * 
     * @param other
     */
    public void setOtherText(String other) {
        if (other != null) {
            otherText.setText(other);
        }
    }

    /**
     * Get string spec representing the current spec entered in the dialog
     * 
     * @return - current UI rev spec
     */
    public String getCurrentRevisionSpec() {
        String spec = null;
        if (revisionRadio.getSelection() && revisionText.getText().length() > 0) {
            spec = revisionText.getText();
            if (!spec.startsWith("#")) { //$NON-NLS-1$
                spec = "#" + spec; //$NON-NLS-1$
            }
        } else if (otherRadio.getSelection()
                && otherText.getText().length() > 0) {
            spec = otherText.getText();
            if (!spec.startsWith("@")) { //$NON-NLS-1$
                spec = "@" + spec; //$NON-NLS-1$
            }
        } else {
            spec = ""; //$NON-NLS-1$
        }
        return spec;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        this.revSpec = getCurrentRevisionSpec();
        this.force = forceSelected();
        this.preview = this.previewSyncButton.getSelection();
        super.okPressed();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = createComposite(dialogArea, 2, GridData.FILL_BOTH);

        GridData twoData = new GridData(SWT.FILL, SWT.FILL, true, false);
        twoData.horizontalSpan = 2;

        latestRadio = createRadio(composite,
                Messages.SyncRevisionDialog_GetLatestRevision);
        latestRadio.setLayoutData(twoData);
        revisionRadio = createRadio(composite,
                Messages.SyncRevisionDialog_RevisionNumber);
        revisionText = createTextField(composite, 30, false);
        otherRadio = createRadio(composite, Messages.SyncRevisionDialog_Other);
        otherText = createTextField(composite);
        createBlank(composite);
        createLabel(composite, Messages.SyncRevisionDialog_LabelChangeOrDate);

        SelectionAdapter radioAdapter = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (otherRadio.getSelection()) {
                    otherText.setEnabled(true);
                    otherText.setFocus();
                } else {
                    otherText.setEnabled(false);
                }
                if (revisionRadio.getSelection()) {
                    revisionText.setEnabled(true);
                    revisionText.setFocus();
                } else {
                    revisionText.setEnabled(false);
                }
            }
        };

        revisionRadio.addSelectionListener(radioAdapter);
        otherRadio.addSelectionListener(radioAdapter);

        if (this.initalText != null) {
            if (this.isOther) {
                otherRadio.setSelection(true);
                otherText.setText(this.initalText);
            } else {
                revisionRadio.setSelection(true);
                revisionText.setText(this.initalText);
            }
        } else {
            revisionRadio.setSelection(true);
        }
        radioAdapter.widgetSelected(null);

        forceSyncButton = new Button(composite, SWT.CHECK);
        forceSyncButton.setText(Messages.SyncRevisionDialog_ForceOperation);
        forceSyncButton.setLayoutData(twoData);

        previewSyncButton = new Button(composite, SWT.CHECK);
        previewSyncButton.setText(Messages.SyncRevisionDialog_PreviewSync);
        previewSyncButton.setLayoutData(twoData);

        return dialogArea;
    }
}
