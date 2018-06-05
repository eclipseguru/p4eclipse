/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.connection.BaseConnectionWizardPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionSelectionPage extends BaseConnectionWizardPage {

    private Composite displayArea;
    private Button newButton;
    private Button existingButton;
    private TableViewer connectionTable;

    private boolean newConnection = false;
    private IP4Connection selected = null;

    /**
     * @param pageName
     */
    protected ConnectionSelectionPage(String pageName) {
        super(pageName);
        setTitle(Messages.ConnectionSelectionPage_ChoosePerforceConnectionTitle);
        setDescription(Messages.ConnectionSelectionPage_ChoosePerforceConnectionDescription);
    }

    /**
     * True if new connection is selected
     * 
     * @return - true if new connection
     */
    public boolean isNewConnection() {
        return newConnection;
    }

    /**
     * True if existing connection is selected
     * 
     * @return - true if existing connection
     */
    public boolean isExistingConnection() {
        return !newConnection;
    }

    /**
     * Get the currently selected connection
     * 
     * @return - current selected connection
     */
    public IP4Connection getConnection() {
        return this.selected;
    }

    /**
     * Get all connections currently available to select
     * 
     * @return - array of p4 connections
     */
    public IP4Connection[] getConnections() {
        return (IP4Connection[]) connectionTable.getInput();
    }

    private void validate() {
        String message = null;
        if (isExistingConnection() && getConnection() == null) {
            message = Messages.ConnectionSelectionPage_SelectExistingConnection;
        }
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SelectionAdapter buttonAdapter = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                newConnection = newButton.getSelection();
                if (newConnection) {
                    selected = null;
                    connectionTable.setSelection(null);
                }
                connectionTable.getTable().setEnabled(!newConnection);
                validate();
            }

        };

        newButton = new Button(displayArea, SWT.RADIO);
        newButton
                .setText(Messages.ConnectionSelectionPage_ConfigureNewConnection);
        newButton.addSelectionListener(buttonAdapter);
        existingButton = new Button(displayArea, SWT.RADIO);
        existingButton
                .setText(Messages.ConnectionSelectionPage_UseExistingConnection);
        existingButton.addSelectionListener(buttonAdapter);

        connectionTable = new TableViewer(displayArea, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);
        connectionTable.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        connectionTable.setLabelProvider(new PerforceLabelProvider());
        connectionTable.setContentProvider(new ArrayContentProvider());

        IP4Connection[] connections = P4ConnectionManager.getManager()
                .getConnections();
        connectionTable.setInput(connections);

        if (connections.length > 0) {
            existingButton.setSelection(true);
            newConnection = false;
            selected = connections[0];
            connectionTable.setSelection(new StructuredSelection(selected));
        } else {
            newButton.setSelection(true);
            newConnection = true;
            connectionTable.getTable().setEnabled(false);
            connectionTable.setSelection(null);
        }

        connectionTable
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        selected = (IP4Connection) ((IStructuredSelection) event
                                .getSelection()).getFirstElement();
                        validate();
                    }
                });

        existingButton.addSelectionListener(buttonAdapter);

        setControl(displayArea);
    }

    /**
     * Get the connection viewer
     * 
     * @return - table viewer
     */
    public TableViewer getConnectionViewer() {
        return this.connectionTable;
    }

}
