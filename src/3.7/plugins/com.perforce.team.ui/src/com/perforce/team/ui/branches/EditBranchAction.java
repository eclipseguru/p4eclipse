/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class EditBranchAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource != null) {
            final IP4Branch branch = (IP4Branch) resource;
            String name = branch.getName();
            if (name == null) {
                name = ""; //$NON-NLS-1$
            }
            final String branchName = name;
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask(NLS
                            .bind(Messages.EditBranchAction_EditingBranch,
                                    branchName), 2);
                    monitor.subTask(Messages.EditBranchAction_RefreshingBranchSubTask);
                    branch.refresh();
                    monitor.worked(1);
                    monitor.subTask(Messages.EditBranchAction_DisplayingBranchSubTask);
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            EditBranchDialog dialog = new EditBranchDialog(
                                    getShell(), branch);
                            if (EditBranchDialog.OK == dialog.open()) {
                                updateBranch(branch, branchName);
                            }
                        }
                    });
                    monitor.done();
                }

                @Override
                public String getTitle() {
                    return NLS.bind(Messages.EditBranchAction_LoadingBranch,
                            branchName);
                }
            };
            runRunnable(runnable);
        }
    }

    private void updateBranch(final IP4Branch branch, final String name) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 1);
                monitor.setTaskName(NLS.bind(
                        Messages.EditBranchAction_RefreshingBranch, name));
                branch.refresh();
                P4Workspace.getWorkspace().notifyListeners(
                        new P4Event(EventType.REFRESHED, branch));
                monitor.worked(1);
                monitor.done();
            }

            @Override
            public String getTitle() {
                return NLS.bind(Messages.EditBranchAction_UpdatingBranch, name);
            }
        };
        runRunnable(runnable);
    }

}
