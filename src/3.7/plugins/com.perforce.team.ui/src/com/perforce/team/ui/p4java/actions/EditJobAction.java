/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.jobs.EditJobDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditJobAction extends P4DoubleClickAction {

    private void updateJob(final String id, final IP4Job job,
            final IP4Changelist[] added, final IP4Changelist[] removed) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), added.length + removed.length + 1);

                if (!monitor.isCanceled()) {
                    for (IP4Changelist list : added) {
                        monitor.setTaskName(MessageFormat.format(
                                Messages.EditJobAction_FixingChangelist,
                                list.getId()));
                        FixJobAction fix = new FixJobAction();
                        fix.setAsync(false);
                        fix.fix(list, new IP4Job[] { job });
                        monitor.worked(1);
                    }
                }

                if (!monitor.isCanceled()) {
                    for (IP4Changelist list : removed) {
                        // Use cached changelist when possible to
                        // appropriately update it when it appears in other
                        // user interface contexts
                        if (list instanceof IP4PendingChangelist) {
                            IP4PendingChangelist cached = job.getConnection()
                                    .getPendingChangelist(list.getId());
                            if (cached != null) {
                                list = cached;
                            }
                        }
                        monitor.setTaskName(MessageFormat.format(
                                Messages.EditJobAction_UnfixingChangelist,
                                list.getId()));
                        UnfixJobAction unfix = new UnfixJobAction();
                        unfix.setAsync(false);
                        unfix.unfix(list, job);
                        monitor.worked(1);
                    }
                }

                if (!monitor.isCanceled()) {
                    monitor.setTaskName(MessageFormat.format(
                            Messages.EditJobAction_RefreshingJob, id));
                    job.refresh();
                    P4Workspace.getWorkspace().notifyListeners(
                            new P4Event(EventType.REFRESHED, job));
                    monitor.worked(1);
                }

                monitor.done();
            }

            @Override
            public String getTitle() {
                return MessageFormat.format(Messages.EditJobAction_UpdatingJob,
                        id);
            }
        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleResourceSelection();
        if (resource instanceof IP4Job) {
            final IP4Job job = (IP4Job) resource;
            String id = job.getId();
            if (id == null) {
                id = ""; //$NON-NLS-1$
            }
            final String jobId = id;
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask(MessageFormat.format(
                            Messages.EditJobAction_EditingJob, jobId), 3);
                    monitor.subTask(Messages.EditJobAction_RefreshingJobSubtask);
                    job.refresh();
                    monitor.worked(1);
                    monitor.subTask(Messages.EditJobAction_RefreshingChangelistsSubtask);
                    final IP4Changelist[] changelists = job.getConnection()
                            .getFixes(job);
                    monitor.worked(1);
                    monitor.subTask(Messages.EditJobAction_DisplayingDialogSubtask);
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            EditJobDialog dialog = new EditJobDialog(
                                    getShell(), job, changelists);
                            if (EditJobDialog.OK == dialog.open()) {
                                IP4Changelist[] added = dialog
                                        .getAddedChangelists();
                                IP4Changelist[] removed = dialog
                                        .getRemovedChangelists();
                                updateJob(jobId, job, added, removed);
                            }
                        }
                    });
                    monitor.done();
                }

                @Override
                public String getTitle() {
                    return MessageFormat.format(
                            Messages.EditJobAction_LoadingJob, jobId);
                }
            };
            runRunnable(runnable);
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        if (containsOnlineConnection()) {
            return getSingleResourceSelection() instanceof IP4Job;
        }
        return false;
    }

}
