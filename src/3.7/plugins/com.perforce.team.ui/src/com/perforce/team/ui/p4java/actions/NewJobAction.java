/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.jobs.NewJobDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class NewJobAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleResourceSelection();
        if (resource instanceof IP4Connection) {
            final IP4Connection p4Connection = (IP4Connection) resource;
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    final IP4Job template = p4Connection.getJob(""); //$NON-NLS-1$
                    monitor.setTaskName(Messages.NewJobAction_DisplayingNewJobDialog);
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            NewJobDialog dialog = new NewJobDialog(getShell(),
                                    p4Connection, template);
                            if (NewJobDialog.OK == dialog.open()) {
                                final IP4Job job = dialog.getCreatedJob();
                                if (job != null) {
                                    final IP4Changelist[] added = dialog
                                            .getAddedChangelists();
                                    IP4Runnable runnable = new P4Runnable() {

                                        @Override
                                        public void run(IProgressMonitor monitor) {
                                            for (IP4Changelist list : added) {
                                                FixJobAction fix = new FixJobAction();
                                                fix.fix(list,
                                                        new IP4Job[] { job });
                                            }
                                            sendCreateEvent(job);
                                        }
                                    };
                                    runRunnable(runnable);
                                }
                            }
                        }
                    });
                }

                @Override
                public String getTitle() {
                    return Messages.NewJobAction_RetrievingJobTemplate;
                }

            };
            runRunnable(runnable);
        }
    }

    private void sendCreateEvent(IP4Job job) {
        P4Workspace.getWorkspace().notifyListeners(
                new P4Event(EventType.CREATE_JOB, job));
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return containsOnlineConnection();
    }

}
