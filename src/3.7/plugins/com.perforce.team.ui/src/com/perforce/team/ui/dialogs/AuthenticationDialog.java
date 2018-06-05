/*
 * Created on 26-Aug-04
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.perforce.team.ui.dialogs;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Administrator
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AuthenticationDialog extends PerforceDialog {

    private Button saveCheck = null;
    private IP4Connection connection;

    /**
     * Constructor.
     * 
     * @param parent
     *            the window in which to display the dialog
     * @param connection
     *            the connection
     */
    public AuthenticationDialog(Shell parent, IP4Connection connection) {
        super(parent, Messages.AuthenticationDialog_PerforceAuthentication);
        this.connection = connection;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Group conSettings = createGroup(composite,
                Messages.AuthenticationDialog_Connection, 1);
        ConnectionParameters params = connection.getParameters();
        createLabel(conSettings, "P4PORT: " + params.getPortNoNull() //$NON-NLS-1$
                + ", P4USER: " + params.getUserNoNull() + ", P4CLIENT: " //$NON-NLS-1$ //$NON-NLS-2$
                + params.getClientNoNull());

        Group status = createGroup(composite,
                Messages.AuthenticationDialog_PasswordSettings, 1);
        saveCheck = createCheck(status,
                Messages.AuthenticationDialog_RememberPassword);
        saveCheck.setSelection(params.savePassword());
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(saveCheck, IHelpContextIds.AUTH_REMEMBER_PASSWORD);
        saveCheck.setFocus();

        return dialogArea;
    }

    /**
     * Is save password checkbox selected?
     * 
     * @return - true if selected, false otherwise
     */
    public boolean savePassword() {
        return saveCheck.getSelection();
    }

    /**
     * OK button pressed.
     */
    @Override
    protected void okPressed() {
        if (saveCheck != null) {
            connection.getParameters()
                    .setSavePassword(saveCheck.getSelection());
        }
        super.okPressed();
    }

}
