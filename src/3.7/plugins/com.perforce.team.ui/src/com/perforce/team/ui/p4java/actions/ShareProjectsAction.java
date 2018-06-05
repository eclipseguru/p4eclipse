/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4TeamUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.project.ShareProjectsDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShareProjectsAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return containsSingleOnlineConnection();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleResourceSelection();
        if (resource != null) {
            IP4Connection connection = resource.getConnection();
            if (connection != null) {
                ShareProjectsDialog dialog = new ShareProjectsDialog(
                        P4UIUtils.getShell(), connection);
                if (ShareProjectsDialog.OK == dialog.open()) {
                    shareProjects(connection, dialog.getSelectedProjects());
                }
            }
        }
    }

    /**
     * Share projects with a connection
     * 
     * @param connection
     * @param projects
     */
    public void shareProjects(final IP4Connection connection,
            final IProject[] projects) {
        if (connection != null && projects != null && projects.length > 0) {
            final String title = MessageFormat.format(
                    Messages.ShareProjectsAction_SharingProjectsMessage,
                    projects.length, connection.getName());
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask(getTitle(), projects.length);
                    ConnectionParameters params = connection.getParameters();
                    for (IProject project : projects) {
                        monitor.setTaskName(MessageFormat.format(
                                Messages.ShareProjectsAction_SharingProject,
                                project.getName()));
                        P4TeamUtils.shareProject(project, params);
                        monitor.worked(1);
                    }
                    monitor.done();
                }

                @Override
                public String getTitle() {
                    return title.toString();
                }
            };
            runRunnable(runnable);
        }
    }
}
