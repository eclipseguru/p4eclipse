/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.connection;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class LinkRepositoryAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return containsSingleOnlineConnection();
    }

    /**
     * Update the link. A null repository will unlink the connection from any
     * task repository
     * 
     * @param connection
     * @param repository
     */
    public void updateLink(IP4Connection connection, TaskRepository repository) {
        if (connection != null) {
            if (repository != null) {
                P4MylynUiUtils.setConnectionSetting(
                        IPreferenceConstants.CONNECTION_LINK_URL,
                        repository.getRepositoryUrl(), connection);
                P4MylynUiUtils.setConnectionSetting(
                        IPreferenceConstants.CONNECTION_LINK_KIND,
                        repository.getConnectorKind(), connection);
            } else {
                P4MylynUiUtils.setConnectionSetting(
                        IPreferenceConstants.CONNECTION_LINK_URL, "", //$NON-NLS-1$
                        connection);
                P4MylynUiUtils.setConnectionSetting(
                        IPreferenceConstants.CONNECTION_LINK_KIND, "", //$NON-NLS-1$
                        connection);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final IP4Connection connection = getSingleOnlineConnectionSelection();
        if (connection != null) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    TaskRepository repository = P4MylynUiUtils.getRepository(
                            connection.getParameters(), false);
                    ConnectionMappingDialog dialog = new ConnectionMappingDialog(
                            P4UIUtils.getDialogShell(),
                            new IP4Connection[] { connection }, P4MylynUiUtils
                                    .getNonPerforceRepositories(), repository,
                            false);
                    if (ConnectionMappingDialog.OK == dialog.open()) {
                        updateLink(connection, dialog.getRepository());
                    }
                }
            });
        }

    }

}
