/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.connection;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.dialogs.P4StatusDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskRepositoryLabelProvider;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionMappingDialog extends P4StatusDialog {

    private IP4Connection[] connections = null;
    private TaskRepository[] repositories = null;

    private IP4Connection selectedConnection = null;
    private TaskRepository selectedRepository = null;

    private boolean forceRepository = true;

    /**
     * @param parent
     * @param connections
     * @param repositories
     */
    public ConnectionMappingDialog(Shell parent, IP4Connection[] connections,
            TaskRepository[] repositories) {
        this(parent, connections, repositories, null, true);
    }

    /**
     * 
     * @param parent
     * @param connections
     * @param repositories
     * @param selected
     * @param forceRepository
     */
    public ConnectionMappingDialog(Shell parent, IP4Connection[] connections,
            TaskRepository[] repositories, TaskRepository selected,
            boolean forceRepository) {
        super(parent,
                Messages.ConnectionMappingDialog_NewConnectionMappingTitle);
        setModalResizeStyle();
        this.connections = connections;
        this.repositories = repositories;
        this.selectedRepository = selected;
        this.forceRepository = forceRepository;
    }

    /**
     * Get selected connection
     * 
     * @return - connection
     */
    public IP4Connection getConnection() {
        return this.selectedConnection;
    }

    /**
     * Get selected repository
     * 
     * @return - task repository
     */
    public TaskRepository getRepository() {
        return this.selectedRepository;
    }

    private void validate() {
        String message = null;
        if (this.selectedConnection == null) {
            message = Messages.ConnectionMappingDialog_SelectConnection;
        } else if (this.selectedRepository == null && forceRepository) {
            message = Messages.ConnectionMappingDialog_SelectTaskRepository;
        }
        setErrorMessage(message);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        Composite displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(new GridLayout(2, false));

        if (this.connections.length > 1) {
            Label connectionLabel = new Label(displayArea, SWT.NONE);
            connectionLabel
                    .setText(Messages.ConnectionMappingDialog_Connections);
            GridData clData = new GridData(SWT.FILL, SWT.FILL, true, true);
            clData.horizontalSpan = 2;
            connectionLabel.setLayoutData(clData);

            final CheckboxTableViewer connectionViewer = CheckboxTableViewer
                    .newCheckList(displayArea, SWT.H_SCROLL | SWT.V_SCROLL
                            | SWT.BORDER);
            GridData cvData = new GridData(SWT.FILL, SWT.FILL, true, true);
            cvData.heightHint = P4UIUtils.VIEWER_HEIGHT_HINT;
            cvData.horizontalSpan = 2;
            connectionViewer.getTable().setLayoutData(cvData);
            connectionViewer.getTable().setLinesVisible(true);
            connectionViewer.setSorter(new ViewerSorter());
            connectionViewer.setContentProvider(new ArrayContentProvider());
            connectionViewer.setLabelProvider(new PerforceLabelProvider());
            connectionViewer.setInput(this.connections);
            connectionViewer.addCheckStateListener(new ICheckStateListener() {

                public void checkStateChanged(CheckStateChangedEvent event) {
                    if (event.getChecked()) {
                        connectionViewer
                                .setCheckedElements(new Object[] { event
                                        .getElement() });
                        selectedConnection = (IP4Connection) event.getElement();
                    } else {
                        selectedConnection = null;
                    }
                    validate();
                }
            });
        } else if (this.connections.length > 0) {
            this.selectedConnection = this.connections[0];
            final PerforceLabelProvider labelProvider = new PerforceLabelProvider();
            Label imageLabel = new Label(displayArea, SWT.NONE);
            imageLabel
                    .setImage(labelProvider.getImage(this.selectedConnection));
            Label textLabel = new Label(displayArea, SWT.NONE);
            textLabel.setText(labelProvider.getText(this.selectedConnection));
            imageLabel.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(DisposeEvent e) {
                    labelProvider.dispose();
                }
            });
        }

        Label repositoryLabel = new Label(displayArea, SWT.NONE);
        repositoryLabel
                .setText(Messages.ConnectionMappingDialog_TaskRepositories);
        GridData rlData = new GridData(SWT.FILL, SWT.FILL, true, true);
        rlData.horizontalSpan = 2;
        repositoryLabel.setLayoutData(rlData);

        final CheckboxTableViewer repositoryViewer = CheckboxTableViewer
                .newCheckList(displayArea, SWT.H_SCROLL | SWT.V_SCROLL
                        | SWT.BORDER);
        GridData rvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        rvData.heightHint = P4UIUtils.VIEWER_HEIGHT_HINT;
        rvData.horizontalSpan = 2;
        repositoryViewer.getTable().setLayoutData(rvData);
        repositoryViewer.getTable().setLinesVisible(true);
        repositoryViewer.setContentProvider(new ArrayContentProvider());
        repositoryViewer.setLabelProvider(new DecoratingLabelProvider(
                new TaskRepositoryLabelProvider(), PlatformUI.getWorkbench()
                        .getDecoratorManager().getLabelDecorator()));
        repositoryViewer.setSorter(new ViewerSorter());
        repositoryViewer.setInput(this.repositories);
        repositoryViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    repositoryViewer.setCheckedElements(new Object[] { event
                            .getElement() });
                    selectedRepository = (TaskRepository) event.getElement();
                } else {
                    selectedRepository = null;
                }
                validate();
            }
        });

        if (this.selectedRepository != null) {
            repositoryViewer.setChecked(this.selectedRepository, true);
        }

        validate();

        return c;
    }

    /**
     * @see org.eclipse.jface.dialogs.StatusDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okStatusButton = createButton(parent, IDialogConstants.OK_ID,
                Messages.ConnectionMappingDialog_Link, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

}
