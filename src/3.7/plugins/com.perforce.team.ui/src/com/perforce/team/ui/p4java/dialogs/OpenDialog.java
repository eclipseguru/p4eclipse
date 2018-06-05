/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import java.text.MessageFormat;

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
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.changelists.PendingCombo;
import com.perforce.team.ui.dialogs.FileListViewer;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class OpenDialog extends P4StatusDialog {

    private FileListViewer viewer;
    private IP4Connection connection;
    private IP4Resource[] allFiles;
    private IP4Resource[] selectedFiles;
    private boolean useSelected = false;
    private String comboTitle;
    private PendingCombo changeCombo;
    private Label countLabel;

    private String defaultDescription = null;
    private int selectedId;

    /**
     * Creates an open dialog
     * 
     * @param parent
     * @param resources
     * @param connection
     * @param selectedId
     * @param dialogTitle
     * @param comboTitle
     */
    public OpenDialog(Shell parent, IP4Resource[] resources,
            IP4Connection connection, int selectedId, String dialogTitle,
            String comboTitle) {
        this(parent, resources, connection, selectedId, dialogTitle,
                comboTitle, null);
    }

    /**
     * Creates an open dialog
     * 
     * @param parent
     * @param resources
     * @param connection
     * @param selectedId
     * @param dialogTitle
     * @param comboTitle
     * @param description
     */
    public OpenDialog(Shell parent, IP4Resource[] resources,
            IP4Connection connection, int selectedId, String dialogTitle,
            String comboTitle, String description) {
        this(parent, resources, connection, dialogTitle, comboTitle,
                description);
        this.selectedId = selectedId;
    }

    /**
     * Creates an open dialog
     * 
     * @param parent
     * @param resources
     * @param connection
     * @param dialogTitle
     * @param comboTitle
     */
    public OpenDialog(Shell parent, IP4Resource[] resources,
            IP4Connection connection, String dialogTitle, String comboTitle) {
        this(parent, resources, connection, dialogTitle, comboTitle, null);
    }

    /**
     * Creates an open dialog
     * 
     * @param parent
     * @param resources
     * @param connection
     * @param dialogTitle
     * @param comboTitle
     * @param description
     */
    public OpenDialog(Shell parent, IP4Resource[] resources,
            IP4Connection connection, String dialogTitle, String comboTitle,
            String description) {
        super(parent, dialogTitle);
        this.connection = connection;
        this.allFiles = resources;
        this.comboTitle = comboTitle;
        this.defaultDescription = description;
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

    /**
     * Get the select changelist id
     * 
     * @return - selected changelist id
     */
    public int getSelectedChangeId() {
        return this.changeCombo.getSelected();
    }

    /**
     * Get pending description
     * 
     * @return - description
     */
    public String getDescription() {
        return this.changeCombo.getDescription();
    }

    /**
     * Use the selected changelist as the new active one?
     * 
     * @return - true to use as current, false otherwise
     */
    public boolean useSelected() {
        return this.useSelected;
    }

    private void updateCount() {
        int count = viewer.getCheckedElements().length;
        int max = viewer.getTable().getItemCount();
        countLabel.setText(MessageFormat.format(
                Messages.OpenDialog_FilesNumSelected, count, max));

        if (count == 0) {
            setErrorMessage(Messages.OpenDialog_MustSelectAtLeastOneFile, null);
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

        changeCombo = createChangeCombo(composite);

        countLabel = new Label(composite, SWT.LEFT);
        GridData clData = new GridData(SWT.FILL, SWT.FILL, true, false);
        clData.verticalIndent = 5;
        clData.horizontalSpan = 2;
        countLabel.setLayoutData(clData);

        viewer = new FileListViewer(composite, allFiles, allFiles, false);
        ((GridData) viewer.getTable().getLayoutData()).horizontalSpan = 2;

        final Button useSelectedButton = new Button(composite, SWT.CHECK);
        useSelectedButton.setText(Messages.OpenDialog_UseSelectedChangelist);
        GridData usbData = new GridData(SWT.FILL, SWT.FILL, true, false);
        usbData.horizontalSpan = 2;
        useSelectedButton.setLayoutData(usbData);
        useSelectedButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                useSelected = useSelectedButton.getSelection();
            }

        });
        updateCount();
        viewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                updateCount();
            }
        });

        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(changeCombo.getCombo(),
                        IHelpContextIds.ADD_EDIT_DELETE_CHANGES);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(viewer.getControl(),
                        IHelpContextIds.ADD_EDIT_DELETE_FILES);
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
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        updateSelection();
        super.okPressed();
    }

    private PendingCombo createChangeCombo(Composite parent) {
        PendingCombo combo = new PendingCombo(this.comboTitle, this.connection);
        combo.setErrorDisplay(this);
        combo.createControl(parent, 1, this.selectedId, this.defaultDescription);
        return combo;
    }

}
