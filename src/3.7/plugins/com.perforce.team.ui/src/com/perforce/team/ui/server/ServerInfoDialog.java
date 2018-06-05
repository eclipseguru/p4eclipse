/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.server;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.dialogs.PerforceDialog;
import com.perforce.team.ui.p4java.actions.EditClientAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerInfoDialog extends PerforceDialog {

    private static final int NUM_CHARS = 80;

    /**
     * Shows the server info dialog for a p4 connection
     * 
     * @param shell
     * @param connection
     */
    public static void showServerInfo(Shell shell, IP4Connection connection) {
        showServerInfo(shell, connection, true);
    }

    /**
     * Shows the server info dialog for a p4 connection
     * 
     * @param shell
     * @param connection
     * @param block
     * @return - opened dialog
     */
    public static ServerInfoDialog showServerInfo(Shell shell,
            IP4Connection connection, boolean block) {
        ServerInfoDialog dialog = new ServerInfoDialog(shell, connection);
        dialog.setBlockOnOpen(block);
        dialog.open();
        return dialog;
    }

    private IP4Connection connection;

    private Composite displayArea;

    private Group serverArea;
    private Link editClientLink;
    private Text addressText;
    private Text rootText;
    private Text dateText;
    private Text uptimeText;
    private Text versionText;
    private Text licenseText;
    
    private Group clientArea;

    /**
     * @param parent
     * @param connection
     */
    public ServerInfoDialog(Shell parent, IP4Connection connection) {
        super(parent, Messages.ServerInfoDialog_ConnectionInformation);
        this.connection = connection;
        if (this.connection.getAddress() != null) {
            this.title = MessageFormat.format(
                    Messages.ServerInfoDialog_ConnectionInformationFor,
                    this.connection.getAddress());
        }
        setModalResizeStyle();
    }

    /**
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        displayArea = new Composite(c, SWT.NONE);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);
        displayArea.setLayout(new GridLayout(1, true));

        GridData textData = new GridData();
        textData.grabExcessHorizontalSpace = true;
        textData.horizontalAlignment = GridData.FILL;
        GC gc = new GC(c);
        gc.setFont(c.getFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();
        textData.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics,
                NUM_CHARS);

        GridLayout groupLayout = new GridLayout(2, false);

        createClientArea(groupLayout, textData);
        createServerArea(groupLayout, textData);
     
        return c;
    }

    private void createClientArea(GridLayout groupLayout, GridData textData) {
        clientArea = new Group(displayArea, SWT.NONE);
        clientArea.setText(Messages.ServerInfoDialog_ClientInformation);
        clientArea.setLayout(groupLayout);
        clientArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        editClientLink = new Link(clientArea, SWT.NONE);
        editClientLink.setText(Messages.ServerInfoDialog_EditClient);
        editClientLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                EditClientAction edit = new EditClientAction();
                edit.selectionChanged(null, new StructuredSelection(connection));
                close();
                edit.run(null);
            }

        });
        GridData eclData = new GridData(SWT.END, SWT.FILL, true, false);
        eclData.horizontalSpan = 2;
        editClientLink.setLayoutData(eclData);

        addField(clientArea, Messages.ServerInfoDialog_ClientName, this.connection.getClientName(), textData);
        addField(clientArea, Messages.ServerInfoDialog_ClientHost, this.connection.getServerInfoClientHost(), textData);
        addField(clientArea, Messages.ServerInfoDialog_ClientRoot, this.connection.getClientRoot(), textData);
        addField(clientArea, Messages.ServerInfoDialog_ClientAddress, this.connection.getServerInfoClientAddress(), textData);
    }
    
    private void createServerArea(GridLayout groupLayout, GridData textData) {
        serverArea = new Group(displayArea, SWT.NONE);
        serverArea.setText(Messages.ServerInfoDialog_ServerInformation);
        serverArea.setLayout(groupLayout);
        serverArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        addressText = addField(serverArea, Messages.ServerInfoDialog_ServerAddress, this.connection.getAddress(), textData);
        rootText = addField(serverArea, Messages.ServerInfoDialog_ServerRoot, this.connection.getRoot(), textData);
        dateText = addField(serverArea, Messages.ServerInfoDialog_ServerDate, this.connection.getDate(), textData);
        uptimeText = addField(serverArea, Messages.ServerInfoDialog_ServerUptime, this.connection.getUptime(), textData);
        versionText = addField(serverArea, Messages.ServerInfoDialog_ServerVersion, this.connection.getVersion(), textData);
        licenseText = addField(serverArea, Messages.ServerInfoDialog_ServerLicense, this.connection.getLicense(), textData);


        IServerInfo info = this.connection.getServerInfo();
        if (info == null)
            return;

        addField(serverArea, Messages.ServerInfoDialog_ServerEncryption, info.isServerEncrypted() ? "encrypted" : null, textData);
        addField(serverArea, Messages.ServerInfoDialog_BrokerVersion, info.getBrokerVersion(), textData);
        addField(serverArea, Messages.ServerInfoDialog_BrokerAddress, info.getBrokerAddress(), textData);
        addField(serverArea, Messages.ServerInfoDialog_BrokerEncryption, info.isBrokerEncrypted() ? "encrypted" : null, textData);

        addField(serverArea, Messages.ServerInfoDialog_SandboxVersion, info.getSandboxVersion(), textData);
        addField(serverArea, Messages.ServerInfoDialog_SandboxPort, info.getSandboxPort(), textData);
    }

    private Text addField(Group area, String labelText, String value, GridData textData) {
        if (value == null)
            return null;
        createLabel(area, labelText);
        Text text = DialogUtils.createSelectableLabel(area, textData);
        text.setText(value);
        return text;
    }
    /**
     * Get server address field value
     * 
     * @return - address field value
     */
    public String getAddress() {
        return this.addressText.getText();
    }

    /**
     * Get server version field value
     * 
     * @return - version field value
     */
    public String getVersion() {
        return this.versionText.getText();
    }

    /**
     * Get server license field value
     * 
     * @return - license field value
     */
    public String getLicense() {
        return this.licenseText.getText();
    }

    /**
     * Get server uptime field value
     * 
     * @return - uptime field value
     */
    public String getUptime() {
        return this.uptimeText.getText();
    }

    /**
     * Get server root field value
     * 
     * @return - root field value
     */
    public String getRoot() {
        return this.rootText.getText();
    }

    /**
     * Get server date field value
     * 
     * @return - date field value
     */
    public String getDate() {
        return this.dateText.getText();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

}
