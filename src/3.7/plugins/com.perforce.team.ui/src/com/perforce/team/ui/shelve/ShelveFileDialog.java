/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.dialogs.FileListViewer;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.shelve.UpdateShelveDialog.Option;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelveFileDialog extends P4StatusDialog {

    private FileListViewer viewer;
    private IP4PendingChangelist list;
    private IP4Resource[] allFiles;
    private IP4Resource[] checkedFiles;
    private IP4Resource[] selectedFiles;
    private Option option;
    private Label countLabel;

    /**
     * Creates a shelve file dialog
     * 
     * @param parent
     * @param list
     * @param resources
     * @param checked
     */
    public ShelveFileDialog(Shell parent, IP4PendingChangelist list,
            IP4Resource[] resources, IP4Resource[] checked) {
        this(parent, list, resources, checked, Option.UPDATE);
    }

    /**
     * Creates a shelve file dialog
     * 
     * @param parent
     * @param list
     * @param resources
     * @param checked
     * @param option
     */
    public ShelveFileDialog(Shell parent, IP4PendingChangelist list,
            IP4Resource[] resources, IP4Resource[] checked, Option option) {
        super(parent);
        setModalResizeStyle();
        if (option == Option.DELETE) {
            setTitle(MessageFormat.format(
                    Messages.ShelveFileDialog_DeleteShelvedFiles, list.getId()));
        } else {
            setTitle(MessageFormat.format(
                    Messages.ShelveFileDialog_ShelveFiles, list.getId()));
        }
        this.allFiles = getFiles(resources);
        this.list = list;
        if (checked != null) {
            this.checkedFiles = checked;
        } else {
            this.checkedFiles = this.allFiles;
        }
        this.option = option;
        setModalResizeStyle();
    }

    /**
     * Get the selected files
     * 
     * @return - selected files
     */
    public IP4Resource[] getSelectedFiles() {
        return selectedFiles;
    }

    private void updateCount() {
        int count = viewer.getCheckedElements().length;
        int max = viewer.getTable().getItemCount();
        countLabel.setText(MessageFormat.format(
                Messages.ShelveFileDialog_FilesNumSelected, count, max));

        if (count == 0) {
            setErrorMessage(Messages.ShelveFileDialog_MustSelectAtLeastOneFile,
                    null);
        } else {
            setErrorMessage(null);
        }
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

        viewer = new FileListViewer(composite, allFiles, checkedFiles, false);
        ((GridData) viewer.getTable().getLayoutData()).horizontalSpan = 2;

        updateCount();
        viewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                updateCount();
            }
        });

        return dialogArea;
    }

    /**
     * Update the store variables representing the current selection of
     * changelist and files
     */
    public void updateSelection() {
        Object[] elements = viewer.getCheckedElements();
        selectedFiles = new IP4Resource[elements.length];
        for (int i = 0; i < elements.length; i++) {
            selectedFiles[i] = (IP4Resource) elements[i];
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.StatusDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        ((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
        String okLabel = null;
        if (this.option == Option.DELETE) {
            okLabel = Messages.ShelveFileDialog_Delete;
        } else {
            okLabel = Messages.ShelveFileDialog_Shelve;
        }
        okStatusButton = createButton(parent, IDialogConstants.OK_ID, okLabel,
                true);
        Button advanced = createButton(parent, IDialogConstants.DETAILS_ID,
                Messages.ShelveFileDialog_Advanced, false);
        advanced.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelection();
                cancelPressed();
                P4Runner.schedule(new P4Runnable() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        ShelveChangelistAction action = new ShelveChangelistAction();
                        action.updateShelveNumbered(list, selectedFiles, option);
                    }

                    @Override
                    public String getTitle() {
                        return MessageFormat
                                .format(Messages.ShelveFileDialog_UpdatingShelvedChangelist,
                                        list.getId());
                    }

                });
            }

        });
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
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
