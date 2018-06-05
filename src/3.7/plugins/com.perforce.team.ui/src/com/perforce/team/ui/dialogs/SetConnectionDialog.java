package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003, 2004 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.connection.BasicConnectionWidget;

/**
 * P4 Connection dialog
 */
public class SetConnectionDialog extends P4StatusDialog {

    // Contains all connection controls and logic
    private BasicConnectionWidget connectionControl;

    private ConnectionParameters params = null;

    /**
     * Constructor.
     * 
     * @param parent
     *            the window in which to display the dialog
     */
    public SetConnectionDialog(Shell parent) {
        super(parent);
        setTitle(Messages.SetConnectionDialog_ServerConnection);
        setModalResizeStyle();
        setStatusLineAboveButtons(true);
        connectionControl = createWidget();
        connectionControl.setErrorDisplay(this);
    }

    /**
     * Create non-null basic connection widget
     * 
     * @return - widget
     */
    protected BasicConnectionWidget createWidget() {
        return new BasicConnectionWidget(false);
    }

    /**
     * Get widget being displayed
     * 
     * @return - widget
     */
    protected BasicConnectionWidget getWidget() {
        return this.connectionControl;
    }

    /**
     * Get the dialog settings
     * 
     * @return the dialog settings
     */
    public IDialogSettings getDialogSettings() {
        return PerforceUIPlugin.getPlugin().getDialogSettings();
    }

    /**
     * Get the connection parameters
     * 
     * @return the connection parameters
     */
    public ConnectionParameters getConnectionParams() {
        return params;
    }

    /**
     * Set the connection parameters
     * 
     * @param params
     *            the connection parameters
     */
    public void setConnectionParams(ConnectionParameters params) {
        this.params = params;
    }

    /**
     * OK button pressed
     */
    @Override
    protected void okPressed() {
        params = connectionControl.getConnectionParameters();
        // params will be null if we cannot connect to the server
        if (params != null) {
            if (params.getCharsetNoNull().equals("") //$NON-NLS-1$
                    || PerforceCharsets.isSupported(params.getCharsetNoNull())) {
                super.okPressed();
            } else {
                MessageDialog.openWarning(getParentShell(),
                        Messages.SetConnectionDialog_InvalidCharsetTitle,
                        Messages.SetConnectionDialog_InvalidCharsetMessage);
            }
        }
    }

    /**
     * Create dialog controls.
     * 
     * @param parent
     * @return - main control
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = connectionControl.createControl(dialogArea, true);
        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        if (params == null) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        } else {
            connectionControl.setConnectionParameters(params);
        }
    }

}
