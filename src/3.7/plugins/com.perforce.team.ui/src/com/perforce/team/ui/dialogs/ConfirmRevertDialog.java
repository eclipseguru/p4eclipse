/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.dialogs;

import java.text.MessageFormat;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConfirmRevertDialog extends P4StatusDialog {

    /**
     * DELETE_EMPTIES
     */
    public static final String DELETE_EMPTIES = "com.perforce.team.ui.dialogs.DELETE_EMPTIES"; //$NON-NLS-1$

    /**
     * DELETE_SHELVES
     */
    public static final String DELETE_SHELVES = "com.perforce.team.ui.dialogs.DELETE_SHELVES"; //$NON-NLS-1$

    /**
     * Opens the confirm revert dialog
     * 
     * @param shell
     * @param files
     * @return - string of selected paths
     */
    public static IP4File[] openQuestion(Shell shell, IP4File[] files) {
        ConfirmRevertDialog dialog = openQuestion(shell, files, true);
        int rc = dialog.open();
        if (rc == OK) {
            return dialog.getSelected();
        }
        return new IP4File[0];
    }

    /**
     * Creates a new confirm revert dialog
     * 
     * @param shell
     * @param files
     * @param block
     * @param offerToDeleteEmpties
     * @return - created dialog
     */
    public static ConfirmRevertDialog openQuestion(Shell shell,
            IP4File[] files, boolean block, boolean offerToDeleteEmpties) {
        ConfirmRevertDialog dialog = new ConfirmRevertDialog(shell, files,
                offerToDeleteEmpties);
        dialog.setBlockOnOpen(block);
        return dialog;
    }

    /**
     * Creates a new confirm revert dialog
     * 
     * @param shell
     * @param files
     * @param block
     * @return - created dialog
     */
    public static ConfirmRevertDialog openQuestion(Shell shell,
            IP4File[] files, boolean block) {
        return openQuestion(shell, files, block, false);
    }

    private Composite displayArea;
    private Label header;
    private Label countLabel;
    private CheckboxTableViewer fileViewer;
    private Button selectAll;
    private Button deselectAll;
    private Button deleteEmptyChangelists;
    private Button deleteShelvedFiles;
    private IP4File[] files = null;
    private IP4File[] selected;

    private boolean showDeleteEmpties = false;
    private boolean showDeleteShelves = false;
    private boolean deleteEmpties = false;
    private boolean deleteShelves = false;

    /**
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    @Override
    public boolean close() {
        Object[] checked = fileViewer.getCheckedElements();
        selected = new IP4File[checked.length];
        for (int i = 0; i < checked.length; i++) {
            selected[i] = (IP4File) checked[i];
        }
        return super.close();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (deleteEmptyChangelists != null) {
            deleteEmpties = deleteEmptyChangelists.getSelection();
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(DELETE_EMPTIES, deleteEmpties);
        }
        if (deleteShelvedFiles != null) {
            deleteShelves = deleteShelvedFiles.getSelection();
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(DELETE_SHELVES, deleteShelves);
        }
        super.okPressed();
    }

    /**
     * Delete empty changelists after revert?
     * 
     * @return - true to delete, false otherwise
     */
    public boolean deleteEmptyChangelists() {
        return this.deleteEmpties;
    }

    /**
     * Delete shelved files after revert?
     * 
     * @return - true to delete, false otherwise
     */
    public boolean deleteShelvedFiles() {
        return this.deleteShelves;
    }

    /**
     * @param parent
     * @param files
     */
    public ConfirmRevertDialog(Shell parent, IP4File[] files) {
        this(parent, files, false);
    }

    /**
     * @param parent
     * @param files
     * @param showDeleteEmpties
     */
    public ConfirmRevertDialog(Shell parent, IP4File[] files,
            boolean showDeleteEmpties) {
        super(parent);
        if (files != null) {
            this.files = files;
            if (this.files.length > 0) {
                this.showDeleteShelves = this.files[0].getConnection()
                        .isShelvingSupported();
            }
        } else {
            this.files = new IP4File[0];
        }
        setTitle(MessageFormat.format(Messages.ConfirmRevertDialog_RevertFiles,
                this.files.length));
        this.showDeleteEmpties = showDeleteEmpties;
        setModalResizeStyle();
    }

    /**
     * @return the selected
     */
    public IP4File[] getSelected() {
        return selected;
    }

    private void updateCount() {
        int count = fileViewer.getCheckedElements().length;
        int max = fileViewer.getTable().getItemCount();
        countLabel.setText(MessageFormat.format(
                Messages.ConfirmRevertDialog_FilesSelected, count, max));

        if (count == 0) {
            setErrorMessage(
                    Messages.ConfirmRevertDialog_MustSelectAtLeastOneFile, null);
        } else {
            setInfoMessage(MessageFormat.format(
                    Messages.ConfirmRevertDialog_RevertingWillOverwrite, max));
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogParent = (Composite) super.createDialogArea(parent);
        displayArea = new Composite(dialogParent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        displayArea.setLayout(layout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);

        header = new Label(displayArea, SWT.LEFT);
        header.setText(MessageFormat.format(
                Messages.ConfirmRevertDialog_RevertSelected, this.files==null?0:this.files.length));

        countLabel = new Label(displayArea, SWT.LEFT);
        GridData clData = new GridData(SWT.FILL, SWT.FILL, true, false);
        clData.verticalIndent = 5;
        countLabel.setLayoutData(clData);

        fileViewer = CheckboxTableViewer.newCheckList(displayArea, SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.BORDER);
        fileViewer.setContentProvider(new ArrayContentProvider());
        fileViewer.setLabelProvider(new PerforceLabelProvider() {

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof IP4File) {
                    String path = ((IP4File) element)
                            .getActionPath(IP4Resource.Type.REMOTE);
                    String decorated = decorator.getLabelDecorator()
                            .decorateText(path, element);
                    if (decorated != null) {
                        path = decorated;
                    }
                    return path;
                }
                return super.getColumnText(element, columnIndex);
            }

        });
        fileViewer.setSorter(new ViewerSorter());
        if (this.files != null) {
            fileViewer.setInput(this.files);
        }
        fileViewer.setAllChecked(true);
        updateCount();
        P4UIUtils.trackMovedFiles(fileViewer);
        fileViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                updateCount();
            }
        });
        GridData fvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fvData.heightHint = 100;
        fvData.widthHint = 400;
        fileViewer.getTable().setLayoutData(fvData);
        Composite buttons = new Composite(displayArea, SWT.NONE);
        GridLayout bLayout = new GridLayout(2, false);
        buttons.setLayout(bLayout);
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        selectAll = new Button(buttons, SWT.PUSH);
        selectAll.setText(Messages.ConfirmRevertDialog_SelectAll);
        selectAll.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fileViewer.setAllChecked(true);
                updateCount();
            }

        });
        deselectAll = new Button(buttons, SWT.PUSH);
        deselectAll.setText(Messages.ConfirmRevertDialog_DeselectAll);
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fileViewer.setAllChecked(false);
                updateCount();
            }

        });
        if (showDeleteEmpties) {
            if (showDeleteShelves) {
                deleteShelvedFiles = new Button(displayArea, SWT.CHECK);
                deleteShelvedFiles
                        .setText(Messages.ConfirmRevertDialog_DeleteShelvedAfterRevert);
                deleteShelvedFiles.setSelection(PerforceUIPlugin.getPlugin()
                        .getPreferenceStore().getBoolean(DELETE_SHELVES));
            }
            deleteEmptyChangelists = new Button(displayArea, SWT.CHECK);
            deleteEmptyChangelists
                    .setText(Messages.ConfirmRevertDialog_DeleteEmptyPendingAfterRevert);
            deleteEmptyChangelists.setSelection(PerforceUIPlugin.getPlugin()
                    .getPreferenceStore().getBoolean(DELETE_EMPTIES));
            SelectionListener listener = new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (deleteShelvedFiles != null) {
                        deleteShelvedFiles.setEnabled(!deleteEmptyChangelists
                                .getSelection());
                        if (deleteEmptyChangelists.getSelection()) {
                            deleteShelvedFiles.setSelection(true);
                        }
                    }
                }

            };
            deleteEmptyChangelists.addSelectionListener(listener);
            listener.widgetSelected(null);
        }
        return dialogParent;
    }
}
