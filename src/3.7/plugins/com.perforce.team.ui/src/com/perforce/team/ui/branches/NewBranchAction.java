/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class NewBranchAction extends P4Action {

    private IP4Branch createdBranch = null;

    /**
     * Return the last branch this action created
     * 
     * @return p4 branch
     */
    public IP4Branch getCreatedBranch() {
        return this.createdBranch;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final IP4Connection connection = getSingleOnlineConnectionSelection();
        if (connection != null) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask(Messages.NewBranchAction_CreateNewBranch,
                            3);

                    monitor.setTaskName(Messages.NewBranchAction_GeneratingBranchTemplate);
                    final IP4Branch branch = connection
                            .getBranch(IP4Branch.TEMPLATE_NAME);
                    monitor.worked(1);

                    monitor.setTaskName(Messages.NewBranchAction_DisplayingNewBranchDialog);
                    final IP4Branch[] created = new IP4Branch[] { null };
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            NewBranchDialog dialog = new NewBranchDialog(
                                    P4UIUtils.getDialogShell(), connection,
                                    branch);
                            if (NewBranchDialog.OK == dialog.open()) {
                                created[0] = dialog.getCreatedBranch();
                            }
                        }
                    });
                    monitor.worked(1);

                    if (created[0] != null) {
                        monitor.setTaskName(Messages.NewBranchAction_RefreshingNewBranch);
                        sendCreateEvent(created[0]);
                    }
                    monitor.worked(1);

                    monitor.done();
                }

                @Override
                public String getTitle() {
                    return Messages.NewBranchAction_RetrievingBranchTemplate;
                }

            };
            runRunnable(runnable);
        }
    }

    private void sendCreateEvent(IP4Branch branch) {
        IP4Branch created = branch.getConnection().getBranch(branch.getName());
        if (created != null) {
            createdBranch = created;
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.CREATE_BRANCH, created));
        }
    }

}
