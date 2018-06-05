/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.P4ClientUtil;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4TeamUtils;
import com.perforce.team.ui.server.EditClientDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditClientAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleResourceSelection();
        if (resource instanceof IP4Connection) {
            final IP4Connection connection = (IP4Connection) resource;
            if (!connection.isOffline()) {
                IP4Runnable runnable = new P4Runnable() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        connection.refreshClient();
                        UIJob job = new UIJob(
                                Messages.EditClientAction_EditingPerforceClient) {

                            @Override
                            public IStatus runInUIThread(
                                    IProgressMonitor monitor) {
                                editClient(connection);
                                return Status.OK_STATUS;
                            }
                        };
                        job.schedule();
                    }

                    @Override
                    public String getTitle() {
                        return Messages.EditClientAction_RefreshingPerforceClient;
                    }

                };
                runRunnable(runnable);
            }
        }
    }

    private void editClient(IP4Connection connection) {
        try {
        	IClient oldClient=connection.getClient();
            EditClientDialog dialog = new EditClientDialog(getShell(),
                    connection);
            if (EditClientDialog.OK == dialog.open()) {
                // Refresh connection if the client was successfully edited
                connection.markForRefresh();
                P4ConnectionManager.getManager().notifyListeners(
                        new P4Event(EventType.REFRESHED, connection));
	            if(P4ClientUtil.shouldSyncClient(oldClient, connection.getClient())){
	            	P4TeamUtils.processClientChange(connection, getShell(),false, null);
	            }
            }
        } catch (P4JavaException e) {
            MessageDialog.openError(getShell(),
                    Messages.EditClientAction_ClientNotFoundTitle,
                    Messages.EditClientAction_ClientNotFoundMessage);
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return this.getSelection() != null && this.getSelection().size() == 1
                && containsOnlineConnection();
    }

}
