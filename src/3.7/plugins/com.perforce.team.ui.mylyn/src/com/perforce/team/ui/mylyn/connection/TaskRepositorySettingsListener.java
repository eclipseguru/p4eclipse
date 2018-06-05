/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.connection;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;

import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryAdapter;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TaskRepositorySettingsListener extends TaskRepositoryAdapter {

    /**
     * @see org.eclipse.mylyn.tasks.core.IRepositoryListener#repositoryRemoved(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public void repositoryRemoved(TaskRepository repository) {
        for (IP4Connection connection : P4ConnectionManager.getManager()
                .getConnections()) {
            String kind = P4MylynUiUtils.getConnectionSetting(
                    IPreferenceConstants.CONNECTION_LINK_KIND, connection);
            if (kind.equals(repository.getConnectorKind())) {
                String url = P4MylynUiUtils.getConnectionSetting(
                        IPreferenceConstants.CONNECTION_LINK_URL, connection);
                if (url.equals(repository.getRepositoryUrl())) {
                    LinkRepositoryAction link = new LinkRepositoryAction();
                    link.updateLink(connection, null);
                }
            }
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.IRepositoryListener#repositoryUrlChanged(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      java.lang.String)
     */
    @Override
    public void repositoryUrlChanged(TaskRepository repository, String oldUrl) {
        for (IP4Connection connection : P4ConnectionManager.getManager()
                .getConnections()) {
            String kind = P4MylynUiUtils.getConnectionSetting(
                    IPreferenceConstants.CONNECTION_LINK_KIND, connection);
            if (kind.equals(repository.getConnectorKind())) {
                String url = P4MylynUiUtils.getConnectionSetting(
                        IPreferenceConstants.CONNECTION_LINK_URL, connection);
                if (url.equals(oldUrl)) {
                    LinkRepositoryAction link = new LinkRepositoryAction();
                    link.updateLink(connection, repository);
                }
            }
        }
    }

}
