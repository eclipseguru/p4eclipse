/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.server.ServerInfoDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerInfoAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        showServerInfoDialogs(true);
    }

    /**
     * Show and return the server info dialogs
     * 
     * @param block
     * @return - opened info dialogs if block is set to false
     */
    public ServerInfoDialog[] showServerInfoDialogs(boolean block) {
        List<ServerInfoDialog> dialogs = new ArrayList<ServerInfoDialog>();
        P4Collection collection = getResourceSelection();
        IP4Resource[] members = collection.members();
        Set<IP4Connection> connections = new HashSet<IP4Connection>();
        for (IP4Resource resource : members) {
            IP4Connection connection = resource.getConnection();
            if (connection != null) {
                connections.add(connection);
            }
        }
        for (IP4Connection connection : connections) {
            ServerInfoDialog dialog = ServerInfoDialog.showServerInfo(
                    getShell(), connection, block);
            if (!block && dialog != null) {
                dialogs.add(dialog);
            }
        }
        return dialogs.toArray(new ServerInfoDialog[0]);
    }

}
