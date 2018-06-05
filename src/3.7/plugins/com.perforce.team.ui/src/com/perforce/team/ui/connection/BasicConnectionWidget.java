/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.ui.IEnableDisplay;
import com.perforce.team.ui.IErrorDisplay;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.dialogs.IHelpContextIds;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BasicConnectionWidget implements IErrorProvider, IEnableDisplay {

    /**
     * Regex for port only specified which will cause localhost to be filled in
     */
    private static final String PORT_ONLY = "\\d+"; //$NON-NLS-1$

    private IErrorDisplay errorDisplay;

    // Connection controls
    private Text port;
    private Text client;
    private Text user;
    private Combo charset;

    private boolean readOnly = false;
    private String errorMessage = null;

    /**
     * BasicConnectionWidget
     * 
     */
    public BasicConnectionWidget() {
        this(false);
    }

    /**
     * BasicConnectionWidget
     * 
     * @param readOnly
     */
    public BasicConnectionWidget(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Create widget controls
     * 
     * @param parent
     *            the parent window
     * @param wrapInGroup
     *            - wrap in labelled group
     * @return the main composite control
     */
    public Composite createControl(Composite parent, boolean wrapInGroup) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite wrapper = displayArea;
        if (wrapInGroup) {
            displayArea.setLayout(new GridLayout(1, true));
            wrapper = new Group(displayArea, SWT.NONE);
            ((Group) wrapper)
                    .setText(Messages.BasicConnectionWidget_ServerConnection);
            wrapper.setLayout(new GridLayout(2, false));
            wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        } else {
            displayArea.setLayout(new GridLayout(2, false));
        }
        port = createLabeledText(wrapper,
                Messages.BasicConnectionWidget_ServerAddress);
        port.setEnabled(!readOnly);
        user = createLabeledText(wrapper, Messages.BasicConnectionWidget_User);
        user.setEnabled(!readOnly);
        client = createLabeledText(wrapper,
                Messages.BasicConnectionWidget_Client);
        client.setEnabled(!readOnly);
        charset = createLabeledCombo(wrapper,
                Messages.BasicConnectionWidget_Charset);
        charset.setEnabled(!readOnly);
        charset.select(charset.indexOf("none"));

        ModifyListener modifyListener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        };

        port.addModifyListener(modifyListener);
        user.addModifyListener(modifyListener);
        client.addModifyListener(modifyListener);
        charset.addModifyListener(modifyListener);

        if (!readOnly) {
            port.setFocus();
        }

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(port, IHelpContextIds.SHARE_PROJECT_PORT);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(user, IHelpContextIds.SHARE_PROJECT_USER);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(client, IHelpContextIds.SHARE_PROJECT_CLIENT);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(charset, IHelpContextIds.SHARE_PROJECT_CHARSET);

        return displayArea;
    }

    /**
     * Update text boxes with specified connection parameters.
     * 
     * @param params
     *            the connection parameters
     */
    public void setConnectionParameters(ConnectionParameters params) {
        port.setText(params.getPortNoNull());
        client.setText(params.getClientNoNull());
        user.setText(params.getUserNoNull());
        String charsetStr = params.getCharset();

        if (charsetStr != null) {
            String select = P4UIUtils.getDisplayCharset(charsetStr);
            if (select != null) {
                charset.setText(select);
            }
        }
    }

    /**
     * Create a labeled text box
     * 
     * @param parent
     *            the parent window
     * @param text
     *            the text for the box
     * @return the text box
     */
    private Text createLabeledText(Composite parent, String text) {
        DialogUtils.createLabel(parent, text);
        return DialogUtils.createTextField(parent,
                IDialogConstants.ENTRY_FIELD_WIDTH, true);
    }

    /**
     * Create a labeled text box
     * 
     * @param parent
     *            the parent window
     * @param text
     *            the text for the box
     * @return the text box
     */
    private Combo createLabeledCombo(Composite parent, String text) {
        DialogUtils.createLabel(parent, text);
        return DialogUtils.createCombo(parent, P4UIUtils.getDisplayCharsets(),
                true);
    }

    /**
     * Get the connection info from the fields and create a connection
     * parameters object.
     * 
     * @return an object representing the connection
     */
    public ConnectionParameters getConnectionParameters() {
        ConnectionParameters params = new ConnectionParameters();

        // Support :1666 and 1666 type entries in the port field
        String portText = port.getText().trim();
        String originalText = portText;
        if (portText.startsWith(":")) { //$NON-NLS-1$
            portText = portText.substring(1);
        }
        if (portText.matches(PORT_ONLY)) {
            portText = "localhost:" + portText; //$NON-NLS-1$
        } else {
            portText = originalText;
        }

        params.setPort(portText);
        params.setClient(client.getText().trim());
        params.setUser(user.getText().trim());
        params.setCharset(P4UIUtils.getP4Charset(charset.getText()));
        return params;
    }

    /**
     * Sets the port
     * 
     * @param port
     *            - value to set the port field to
     */
    public void setPort(String port) {
        if (port != null && this.port != null && !this.port.isDisposed()) {
            this.port.setText(port);
        }
    }

    /**
     * Sets the client
     * 
     * @param client
     *            - value to set the client field to
     */
    public void setClient(String client) {
        if (client != null && this.client != null && !this.client.isDisposed()) {
            this.client.setText(client);
        }
    }

    /**
     * Sets the user
     * 
     * @param user
     *            - value to set the user field to
     */
    public void setUser(String user) {
        if (user != null && this.user != null && !this.user.isDisposed()) {
            this.user.setText(user);
        }
    }

    /**
     * Sets the charset
     * 
     * @param charset
     *            - value to set the charset field to
     */
    public void setCharset(String charset) {
        if (charset != null && this.charset != null
                && !this.charset.isDisposed()) {
            this.charset.setText(charset);
        }
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#setErrorDisplay(com.perforce.team.ui.IErrorDisplay)
     */
    public void setErrorDisplay(IErrorDisplay display) {
        this.errorDisplay = display;
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#getErrorMessage()
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#validate()
     */
    public void validate() {
        this.errorMessage = validatePort();
        if (this.errorMessage == null) {
            if (user.getText().trim().length() == 0) {
                this.errorMessage = Messages.BasicConnectionWidget_MustSpecifyUser;
            } else if (client.getText().trim().length() == 0) {
                this.errorMessage = Messages.BasicConnectionWidget_MustSpecifyClient;
            } else if (charset.getText().length() > 0) {
                if (!PerforceCharsets.isSupported(P4UIUtils.getP4Charset(charset.getText()))) {
                    errorMessage = Messages.BasicConnectionWidget_MustSpecifyCharset;
                }
            }
        }
        if (this.errorDisplay != null) {
            this.errorDisplay.setErrorMessage(this.errorMessage, this);
        }
    }

    private String validatePort() {
        String port = this.port.getText().trim();
        if (port.isEmpty()) {
            return Messages.BasicConnectionWidget_MustSpecifyPort;
        }
        Pattern p = Pattern.compile("((ssl):)?([^:]+):([\\d]+)"); //$NON-NLS-1$
        Matcher m = p.matcher(port);
        if (!m.matches()) {
            return Messages.BasicConnectionWidget_InvalidServerAddress;
        }

        return null;
    }
 
    /**
     * @see com.perforce.team.ui.IEnableDisplay#isEnabled()
     */
    public boolean isEnabled() {
        if (this.port != null && !this.port.isDisposed()) {
            return this.port.isEnabled();
        }
        return false;
    }

    /**
     * @see com.perforce.team.ui.IEnableDisplay#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        if (this.port != null && !this.port.isDisposed()) {
            this.port.setEnabled(enabled);
            this.user.setEnabled(enabled);
            this.client.setEnabled(enabled);
            this.charset.setEnabled(enabled);
        }
    }
}
