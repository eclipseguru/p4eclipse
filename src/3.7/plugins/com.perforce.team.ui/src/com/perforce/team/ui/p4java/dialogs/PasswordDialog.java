/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.dialogs.PerforceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PasswordDialog extends PerforceDialog {

    private Text passwordControl;
    private Button saveCheck;
    private boolean showOffline = true;
    private IP4Connection connection;
    private String password = null;

    /**
     * Creates a new password dialog
     * 
     * @param parent
     * @param connection
     */
    public PasswordDialog(Shell parent, IP4Connection connection) {
        this(parent, connection, true);
    }

    /**
     * Creates a new password dialog
     * 
     * @param parent
     * @param connection
     * @param showOffline
     */
    public PasswordDialog(Shell parent, IP4Connection connection,
            boolean showOffline) {
        super(parent, Messages.PasswordDialog_EnterPassword);
        this.connection = connection;
        this.showOffline = showOffline;
        setModalResizeStyle();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Group conSettings = createGroup(composite,
                Messages.PasswordDialog_Connection, 1);
        createLabel(conSettings, MessageFormat.format(
                Messages.PasswordDialog_Server, this.connection.getAddress()));
        createLabel(conSettings, MessageFormat.format(
                Messages.PasswordDialog_User, this.connection.getUser()));
        createLabel(conSettings,
                MessageFormat.format(Messages.PasswordDialog_Client,
                        this.connection.getClientName()));

        createLabel(composite, Messages.PasswordDialog_EnterPasswordLabel);
        passwordControl = new Text(composite, SWT.SINGLE | SWT.BORDER
                | SWT.PASSWORD);
        passwordControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        passwordControl.setEchoChar('*');
        passwordControl.setFocus();

        saveCheck = createCheck(composite,
                Messages.PasswordDialog_RememberPassword);
        saveCheck.setSelection(this.connection.getParameters().savePassword());
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(saveCheck, IHelpContextIds.AUTH_REMEMBER_PASSWORD);

        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                Messages.PasswordDialog_Cancel, false);
        if (showOffline) {
            Button goOffline = createButton(parent, IDialogConstants.ABORT_ID,
                    Messages.PasswordDialog_WorkOffline, false);
            goOffline.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    connection.setOffline(true);
                    P4Workspace.getWorkspace().notifyListeners(
                            new P4Event(EventType.CHANGED, connection));
                    close();
                }

            });
        }
    }

    /**
     * Get the password fielded
     * 
     * @return - current password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Get password field
     * 
     * @return - password text
     */
    public Text getPasswordText() {
        return this.passwordControl;
    }

    /**
     * Update the internal password field with the current text field value
     */
    public void updatePassword() {
        this.password = this.passwordControl.getText();
    }

    /**
     * OK button pressed.
     */
    @Override
    protected void okPressed() {
        updatePassword();
        boolean save = saveCheck.getSelection();
        this.connection.getParameters().setSavePassword(save);
        super.okPressed();
    }
}
