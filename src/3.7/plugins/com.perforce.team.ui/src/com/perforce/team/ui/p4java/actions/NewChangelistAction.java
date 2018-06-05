/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.dialogs.ChangeSpecDialog;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class NewChangelistAction extends P4Action {

    /**
     * Runs the action and optionally shows a dialog where files can be selected
     * that will be re-opened in the new changelist. If showDialog is false a
     * new empty changelist will be created with the description parameter if
     * non-null. If showDialog is false and description is null than this action
     * will do nothing.
     * 
     * @param showDialog
     * @param description
     * @param files
     * @param jobs
     */
    public void runAction(boolean showDialog, String description,
            IP4File[] files, IP4Job[] jobs) {
        final P4Collection collection = getResourceSelection();
        IP4Connection connection = null;
        for (IP4Resource resource : collection.members()) {
            if (resource.getConnection() != null) {
                connection = resource.getConnection();
                break;
            }
        }
        if (connection != null) {
            if (jobs == null) {
                jobs = new IP4Job[0];
            }
            if (files == null) {
                files = new IP4File[0];
            }
            refreshAndCreate(showDialog, description, files, jobs, connection);
        }
    }

    private void refreshAndCreate(final boolean showDialog,
            final String description, final IP4File[] files,
            final IP4Job[] jobs, final IP4Connection connection) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                IP4PendingChangelist defaultChangelist = connection
                        .getPendingChangelist(0);
                if (defaultChangelist != null) {
                    defaultChangelist.refresh();
                    create(showDialog, description, files, jobs, connection,
                            defaultChangelist);
                }
            }

            @Override
            public String getTitle() {
                return Messages.NewChangelistAction_RefreshingDefaultChangelist;
            }
        };
        runRunnable(runnable);
    }

    private void create(boolean showDialog, String description,
            IP4File[] files, IP4Job[] jobs, final IP4Connection connection,
            final IP4PendingChangelist defaultChangelist) {
        if (showDialog) {
            UIJob job = new UIJob(
                    Messages.NewChangelistAction_DisplayingNewChangelistDialog) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    final ChangeSpecDialog dlg = new ChangeSpecDialog(
                            defaultChangelist, null, getShell(), false);
                    if (dlg.open() == Window.OK) {
                        String enteredDescription = dlg.getDescription();
                        IP4File[] selectedFiles = dlg.getCheckedFiles();
                        IP4Job[] selectedJobs = dlg.getCheckedJobs();
                        createChangelist(enteredDescription, selectedJobs,
                                selectedFiles, connection);
                    }
                    return Status.OK_STATUS;
                }
            };
            job.schedule();
        } else {
            createChangelist(description, jobs, files, connection);
        }
    }

    /**
     * Create a new pending changelist with the specified files in the
     * selection.
     */
    public void create() {
        P4Collection collection = getFileSelection();
        Map<IP4Connection, List<IP4File>> connectionMap = collection
                .toFileMap();
        for (Map.Entry<IP4Connection, List<IP4File>> entry: connectionMap.entrySet()) {
        	final IP4Connection connection = entry.getKey();
            final IP4File[] files = entry.getValue().toArray(new IP4File[0]);
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    // TODO: "Test" seems unlikely to be an appropriate dialog
                    // title here
                    final ChangeSpecDialog dlg = new ChangeSpecDialog(
                            connection, files, null, getShell(),
                            Messages.NewChangelistAction_Test);
                    if (dlg.open() == Window.OK) {
                        String enteredDescription = dlg.getDescription();
                        IP4File[] selectedFiles = dlg.getCheckedFiles();
                        IP4Job[] selectedJobs = dlg.getCheckedJobs();
                        createChangelist(enteredDescription, selectedJobs,
                                selectedFiles, connection);
                    }
                }
            });
        }
    }

    private void createChangelist(final String p4Description,
            final IP4Job[] p4Jobs, final IP4File[] p4Files,
            final IP4Connection p4Connection) {
        if (p4Jobs != null && p4Files != null && p4Description != null) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    p4Connection.createChangelist(p4Description, p4Files,
                            p4Jobs);
                }

                @Override
                public String getTitle() {
                    return Messages.NewChangelistAction_CreatingNewChangelist;
                }

            };
            runRunnable(runnable);
        }
    }

    /**
     * Runs the action and optionally shows a dialog where files can be selected
     * that will be re-opened in the new changelist. If showDialog is false a
     * new empty changelist will be created with the description parameter if
     * non-null. If showDialog is false and description is null than this action
     * will do nothing.
     * 
     * @param showDialog
     * @param description
     */
    public void runAction(boolean showDialog, String description) {
        runAction(showDialog, description, null, null);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        runAction(true, null);
    }

}
