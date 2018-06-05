/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class WorkOfflineAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        P4Collection collection = getConnectionSelection();
        if (!collection.isEmpty()) {
            updateConnections(collection.members());
        }
    }

    private void updateConnections(final IP4Resource[] resources) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                for (IP4Resource resource : resources) {
                    if (resource instanceof IP4Connection
                            && !((IP4Connection) resource).isOffline()) {
                        IP4Connection connection = (IP4Connection) resource;
                        connection.setOffline(true);
                        P4Workspace.getWorkspace().notifyListeners(
                                new P4Event(EventType.CHANGED, connection));
                    }
                }
                updateActionState();
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = false;
        P4Collection collection = getConnectionSelection();
        IP4Resource[] resources = collection.members();
        for (IP4Resource resource : resources) {
            IP4Connection connection = resource.getConnection();
            if (connection != null && !connection.isOffline()) {
                enabled = true;
                break;
            }
        }
        return enabled;
    }

}
