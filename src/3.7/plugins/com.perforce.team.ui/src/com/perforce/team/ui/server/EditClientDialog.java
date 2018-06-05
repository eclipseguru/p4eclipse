/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.server;

import java.text.MessageFormat;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.P4ClientUtil;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.SWTUtils;
import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditClientDialog extends P4StatusDialog{

    private IP4Connection connection = null;
    private IClient spec;
    private Composite displayArea;
    private ClientWidget editor;

    /**
     * @param parent
     * @param connection
     * @throws P4JavaException
     *             - if the spec can not be retrieved
     */
    public EditClientDialog(Shell parent, IP4Connection connection)
            throws P4JavaException {
        super(parent);
        setModalResizeStyle();
        this.connection = connection;
        if (this.connection != null) {
            String title = MessageFormat.format(
                    Messages.EditClientDialog_Workspace, this.connection
                            .getParameters().getClient(), this.connection
                            .getParameters().getPort(), this.connection
                            .getParameters().getUser());
            setTitle(title);
            this.spec = this.connection.getClient();
        }
        if (this.spec == null) {
            throw new P4JavaException(Messages.EditClientDialog_SpecNotFound);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okStatusButton = createButton(parent, IDialogConstants.OK_ID,
                Messages.EditClientDialog_Save, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    public void create() {
    	super.create();
    	updateButtonsEnableState(ValidationStatus.error(Messages.EditClientDialog_ClientNotChanged));
    }
    
    /**
     * Get the client widget
     * 
     * @return - client widget
     */
    public ClientWidget getClientWidget() {
        return this.editor;
    }

    /**
     * Get edited spec
     * 
     * @return - edited spec
     */
    public IClient getEditedSpec() {
        return this.editor.getCurrentSpec();
    }

    private void save() {
        this.editor.updateCurrentSpec();
        // Get edited client
        IClient newSpec = getEditedSpec();
        connection.updateClient(newSpec);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
    	save();
    	super.okPressed();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        editor = new ClientWidget(this.connection, this.spec);
        editor.createControl(displayArea);
        editor.setErrorDisplay(this);
        SWTUtils.addContentListener(c, new Runnable() {
			public void run() {
				computeChangeAndUpdateButton();
			}
		});

        return c;
    }

	protected void computeChangeAndUpdateButton() {
		this.editor.updateCurrentSpec();
		IClient newSpec = getEditedSpec();
		if(newSpec==null || !P4ClientUtil.isClientChanged(spec,newSpec)){
			okStatusButton.setEnabled(false);
		}else
			okStatusButton.setEnabled(true);
	}
}
