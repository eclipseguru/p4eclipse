/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.connection.ConnectionWizard;
import com.perforce.team.ui.connection.ConnectionWizardDialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JobSettingsPage extends WizardPage implements
        ITaskRepositoryPage {

    private boolean titleUpdated = false;
    private Text titleText;
    private TableViewer existingConnectionTable;
    private TaskRepository repository;
    private List<TaskRepository> existingRepos = new ArrayList<TaskRepository>();

    /**
     * @param taskRepository
     */
    public P4JobSettingsPage(TaskRepository taskRepository) {
        super("jobSettings"); //$NON-NLS-1$
        setTitle(Messages.P4JobSettingsPage_PerforceJobs);
        setDescription(Messages.P4JobSettingsPage_ConfigureJobsRepository);
        this.repository = taskRepository;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage#applyTo(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    public void applyTo(TaskRepository repository) {
        IP4Connection connection = getConnection();
        if (connection != null) {
            P4MylynUiUtils.setTaskSettings(connection, repository);
            String title = this.titleText.getText().trim();
            if (title.length() == 0) {
                title = connection.getParameters().getPort();
            }
            repository.setRepositoryLabel(title);
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage#getRepositoryUrl()
     */
    public String getRepositoryUrl() {
        IP4Connection connection = getConnection();
        return connection != null ? connection.getParameters().getPort() : null;
    }

    private void createExistingArea(Composite displayArea) {
        Label tableLabel = new Label(displayArea, SWT.NONE);
        tableLabel.setText(Messages.P4JobSettingsPage_ServerConnections);
        tableLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        existingConnectionTable = createExistingServerArea(displayArea);

        SelectionListener selectionAdapter = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnablement();
                setTitleToSelection();
            }

        };
        existingConnectionTable.getTable().addSelectionListener(
                selectionAdapter);

        Button newConnection = new Button(displayArea, SWT.NONE);
        Image addImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_ADD).createImage();
        P4UIUtils.registerDisposal(newConnection, addImage);
        newConnection.setImage(addImage);
        newConnection.setText(Messages.P4JobSettingsPage_AddConnection);
        newConnection.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ConnectionWizardDialog dialog = new ConnectionWizardDialog(
                        P4UIUtils.getDialogShell(), new ConnectionWizard());
                if (ConnectionWizardDialog.OK == dialog.open()) {
                    ConnectionParameters params = dialog
                            .getConnectionParameters();
                    if (P4ConnectionManager.getManager().containsConnection(
                            params)) {
                        existingConnectionTable.setInput(P4ConnectionManager
                                .getManager().getConnections());
                        IP4Connection select = P4ConnectionManager.getManager()
                                .getConnection(params);
                        existingConnectionTable
                                .setSelection(new StructuredSelection(select));
                        setTitleToSelection();
                        updateEnablement();
                    }
                }
            }

        });
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite titleArea = new Composite(displayArea, SWT.NONE);
        GridLayout taLayout = new GridLayout(2, false);
        titleArea.setLayout(taLayout);
        titleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label titleLabel = new Label(titleArea, SWT.NONE);
        titleLabel.setText(Messages.P4JobSettingsPage_Label);
        titleText = new Text(titleArea, SWT.SINGLE | SWT.BORDER);
        titleText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        titleText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                updateEnablement();
                titleUpdated = true;
            }
        });

        createExistingArea(displayArea);

        for (TaskRepository repository : TasksUi.getRepositoryManager()
                .getRepositories(IP4MylynConstants.KIND)) {
            if (!repository.equals(this.repository)) {
                this.existingRepos.add(repository);
            }
        }

        if (this.repository == null) {
            if (existingRepos.isEmpty()
                    && existingConnectionTable.getTable().getItemCount() > 0) {
                existingConnectionTable.getTable().setSelection(0);
            }
            setPageComplete(false);
        } else {
            IP4Connection connection = P4MylynUtils
                    .getConnection(this.repository);
            if (connection != null) {
                existingConnectionTable.setSelection(new StructuredSelection(
                        connection));
            }
            fillWidget();
        }
        setControl(displayArea);
    }

    private void fillWidget() {
        if (this.repository != null) {
            String label = this.repository.getRepositoryLabel();
            if (label != null) {
                titleText.setText(label);
            }
        }
    }

    private void setTitleToSelection() {
        if (!titleUpdated) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                this.titleText.setText(connection.getParameters().getPort());
                this.titleUpdated = false;
            }
        }
    }

    private void updateEnablement() {
        String message = null;
        String name = this.titleText.getText().trim();
        for (TaskRepository repo : this.existingRepos) {
            if (name.equals(repo.getRepositoryLabel())) {
                message = Messages.P4JobSettingsPage_RepositoryLabelExists;
                break;
            }
        }

        if (message == null) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                for (TaskRepository repo : this.existingRepos) {
                    if (repo.equals(P4MylynUiUtils.findRepository(connection,
                            IP4MylynConstants.KIND))) {
                        message = Messages.P4JobSettingsPage_RepositoryExists;
                        break;
                    }
                }
            }
        }

        if (message == null) {
            if (existingConnectionTable != null
                    && existingConnectionTable.getTable().getSelectionCount() != 1) {
                message = Messages.P4JobSettingsPage_SelectServer;
            }
        }

        setErrorMessage(message);
        setPageComplete(getErrorMessage() == null);
    }

    private TableViewer createExistingServerArea(Composite parent) {
        Composite existingArea = new Composite(parent, SWT.NONE);
        existingArea.setLayout(new GridLayout(1, true));
        existingArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        TableViewer viewer = new TableViewer(existingArea, SWT.FULL_SELECTION
                | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
        viewer.setLabelProvider(new PerforceLabelProvider(false));
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setSorter(new ViewerSorter());
        GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true);
        vData.heightHint = 100;
        viewer.getTable().setLayoutData(vData);
        viewer.setInput(P4ConnectionManager.getManager().getConnections());
        return viewer;
    }

    /**
     * Get the selected connection
     * 
     * @return - connection
     */
    private IP4Connection getConnection() {
        IP4Connection connection = null;
        if (existingConnectionTable != null) {
            IStructuredSelection selection = (IStructuredSelection) existingConnectionTable
                    .getSelection();
            if (selection.getFirstElement() instanceof IP4Connection) {
                connection = (IP4Connection) selection.getFirstElement();
            }
        }
        return connection;
    }

    /** @since 3.6 */
    public void performFinish(TaskRepository repository) {
        applyTo(repository);
    }

	public boolean preFinish(TaskRepository repository) {
		// This is new from 3.7 API 
		return true;
	}

}
