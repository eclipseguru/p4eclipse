/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.connection;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4ConnectionListener;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskRepositoryAction;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionSettingsListener extends P4ConnectionListener {

    /**
     * @see com.perforce.team.core.p4java.P4ConnectionListener#connectionChanged(com.perforce.team.core.p4java.IP4Connection,
     *      com.perforce.team.core.ConnectionParameters)
     */
    @Override
    public void connectionChanged(IP4Connection connection,
            ConnectionParameters previousParams) {

        TaskRepository repository = P4MylynUiUtils.findRepository(
                previousParams, IP4MylynConstants.KIND);
        if (repository != null) {
            P4MylynUiUtils.setTaskSettings(connection, repository);
        }

        String linkPref = P4MylynUiUtils.getConnectionSetting(
                IPreferenceConstants.CONNECTION_LINK_KIND, previousParams);
        if (linkPref.length() > 0) {
            P4MylynUiUtils.setConnectionSetting(
                    IPreferenceConstants.CONNECTION_LINK_KIND, "", //$NON-NLS-1$
                    previousParams);
            P4MylynUiUtils.setConnectionSetting(
                    IPreferenceConstants.CONNECTION_LINK_KIND, linkPref,
                    connection);
        }

        String urlPref = P4MylynUiUtils.getConnectionSetting(
                IPreferenceConstants.CONNECTION_LINK_URL, previousParams);
        if (urlPref.length() > 0) {
            P4MylynUiUtils.setConnectionSetting(
                    IPreferenceConstants.CONNECTION_LINK_URL, "", //$NON-NLS-1$
                    previousParams);
            P4MylynUiUtils.setConnectionSetting(
                    IPreferenceConstants.CONNECTION_LINK_URL, urlPref,
                    connection);
        }
    }

    private void deleteRepository(final TaskRepository repository) {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                DeleteTaskRepositoryAction deleteRepo = new DeleteTaskRepositoryAction();
                deleteRepo
                        .selectionChanged(new StructuredSelection(repository));
                deleteRepo.run();
            }
        });
    }

    /**
     * @see com.perforce.team.core.p4java.P4ConnectionListener#connectionRemovalRequested(ConnectionParameters)
     */
    @Override
    public void connectionRemovalRequested(ConnectionParameters params) {
        TaskRepository repository = P4MylynUiUtils.findRepository(params,
                IP4MylynConstants.KIND);
        if (repository != null) {
            String message = MessageFormat.format(
                    Messages.ConnectionSettingsListener_DeleteMessage,
                    params.getPort(), repository.getRepositoryLabel());
            if (P4ConnectionManager.getManager().openConfirm(
                    Messages.ConnectionSettingsListener_DeleteTitle, message)) {
                deleteRepository(repository);
            }
        }
    }
}
