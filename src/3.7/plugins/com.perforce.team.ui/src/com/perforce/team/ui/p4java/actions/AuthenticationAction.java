/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.dialogs.AuthenticationDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AuthenticationAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        P4Collection collection = getConnectionSelection();
        IP4Resource[] resources = collection.members();
        if (resources.length == 1) {
            IP4Connection connection = resources[0].getConnection();
            if (connection != null) {
                AuthenticationDialog dialog = new AuthenticationDialog(
                        getShell(), connection);
                dialog.open();
            }
        }
    }

}
