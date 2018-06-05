/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.ConnectionParameters;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionWizardDialog extends WizardDialog {

    /**
     * @param parentShell
     * @param newWizard
     */
    public ConnectionWizardDialog(Shell parentShell, ConnectionWizard newWizard) {
        super(parentShell, newWizard);
//        setHelpAvailable(false);

    }

    /**
     * Get the connection wizard
     * 
     * @see org.eclipse.jface.wizard.WizardDialog#getWizard()
     */
    @Override
    public IConnectionWizard getWizard() {
        return (IConnectionWizard) super.getWizard();
    }

    /**
     * Get connection parameters
     * 
     * @return - configured parameters
     */
    public ConnectionParameters getConnectionParameters() {
        ConnectionParameters params = new ConnectionParameters();
        IConnectionWizard wizard = getWizard();
        params.setPort(wizard.getPort());
        params.setClient(wizard.getClient());
        params.setUser(wizard.getUser());
        params.setCharset(wizard.getCharset());
        return params;
    }

//    /**
//     * Get the imported folders
//     * 
//     * @return - p4 folders array
//     */
//    public IP4Folder[] getImportedFolders() {
//        return ((ConnectionWizard) getWizard()).getImportedFolders();
//    }

}
