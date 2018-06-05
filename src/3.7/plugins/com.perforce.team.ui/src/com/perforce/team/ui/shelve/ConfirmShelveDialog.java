/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.changelists.DescriptionViewer;
import com.perforce.team.ui.dialogs.FileListViewer;
import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ConfirmShelveDialog extends P4StatusDialog {

    /**
     * NEW_SHELVED_CHANGELIST_DESCRIPTION
     */
    public static final String NEW_SHELVED_CHANGELIST_DESCRIPTION = Messages.ConfirmShelveDialog_NewShelvedChangelistDescription;

    private IP4PendingChangelist list;
    private IP4File[] files;

    private FileListViewer viewer;
    private DescriptionViewer descriptionViewer;
    private IP4File[] selectedFiles;
    private String description;
    private Label countLabel;

    /**
     * @param parent
     * @param list
     * @param files
     */
    public ConfirmShelveDialog(Shell parent, IP4PendingChangelist list,
            IP4File[] files) {
        super(parent);
        String changeString = list.isDefault()
                ? Messages.ConfirmShelveDialog_DefaultChangelist
                : MessageFormat.format(
                        Messages.ConfirmShelveDialog_ChangelistNum,
                        list.getId());
        setTitle(MessageFormat.format(
                Messages.ConfirmShelveDialog_ShelveFilesFromChangelist,
                files.length, changeString));
        setModalResizeStyle();
        this.list = list;
        this.files = files;
    }

    /**
     * Get changelist to shelve files into
     * 
     * @return - changelist id
     */
    public int getChangelist() {
        return this.list.isDefault() ? IP4PendingChangelist.NEW : this.list
                .getId();
    }

    /**
     * Get the selected files
     * 
     * @return - selected files
     */
    public IP4File[] getSelectedFiles() {
        return selectedFiles;
    }

    private void updateCount() {
        int count = viewer.getCheckedElements().length;
        int max = viewer.getTable().getItemCount();
        countLabel.setText(MessageFormat.format(
                Messages.ConfirmShelveDialog_FilesSelected, count, max));
    }

    /**
     * Get entered description
     * 
     * @return - string description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(dialogArea, SWT.NONE);
        GridLayout cLayout = new GridLayout(2, false);
        cLayout.marginHeight = 0;
        cLayout.marginWidth = 0;
        composite.setLayout(cLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        countLabel = new Label(composite, SWT.LEFT);
        GridData clData = new GridData(SWT.FILL, SWT.FILL, true, false);
        clData.verticalIndent = 5;
        clData.horizontalSpan = 2;
        countLabel.setLayoutData(clData);

        viewer = new FileListViewer(composite, this.files, this.files, false);
        ((GridData) viewer.getTable().getLayoutData()).horizontalSpan = 2;

        viewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                updateCount();
                validate();
            }
        });

        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText(Messages.ConfirmShelveDialog_Description);

        description = NEW_SHELVED_CHANGELIST_DESCRIPTION;
        descriptionViewer = new DescriptionViewer(list.getConnection());
        descriptionViewer.createControl(composite, description);
        descriptionViewer.getDocument().addDocumentListener(
                new IDocumentListener() {

                    public void documentChanged(DocumentEvent event) {
                        description = descriptionViewer.getDocument().get();
                        validate();
                    }

                    public void documentAboutToBeChanged(DocumentEvent event) {
                        // Ignored
                    }
                });
        StyledText styledText = descriptionViewer.getViewer().getTextWidget();
        ((GridData) styledText.getLayoutData()).heightHint = P4UIUtils
                .computePixelHeight(styledText.getFont(), 5);
        descriptionViewer.setFocus();

        validate();

        return dialogArea;
    }

    private void validate() {
        String errorMessage = null;
        if (viewer.getCheckedElements().length == 0) {
            errorMessage = Messages.ConfirmShelveDialog_MustSelectAtLeastOneFile;
        }

        if (errorMessage == null && description.length() == 0) {
            errorMessage = Messages.ConfirmShelveDialog_EnterChangelistDescription;
        }

        if (errorMessage != null) {
            setErrorMessage(errorMessage);
        } else {
            setInfoMessage(Messages.ConfirmShelveDialog_SelectedFilesWillBeReopenedBeforeShelving);
        }
    }

    /**
     * Update the store variables representing the current selection of
     * changelist and files
     */
    public void updateSelection() {
        Object[] elements = viewer.getCheckedElements();
        selectedFiles = new IP4File[elements.length];
        System.arraycopy(elements, 0, selectedFiles, 0, selectedFiles.length);
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
