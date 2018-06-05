/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.dialogs.JobFixDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FixJobAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = false;
        P4Collection collection = getResourceSelection();
        IP4Resource[] members = collection.members();
        if (members.length == 1 && members[0] instanceof IP4PendingChangelist) {
            IP4PendingChangelist list = (IP4PendingChangelist) members[0];
            enabled = !list.isDefault() && !list.isReadOnly()
                    && list.isOnClient();
        }
        return enabled;
    }

    /**
     * Runs
     * 
     * @param jobs
     */
    public void runAction(IP4Job[] jobs) {
        P4Collection collection = getResourceSelection();
        IP4Resource[] resources = collection.members();
        if (resources.length == 1 && resources[0] instanceof IP4Changelist) {
            IP4Changelist list = (IP4Changelist) resources[0];
            fix(list, jobs);
        }
    }

    /**
     * Fix the changelist with the specified jobs
     * 
     * @param list
     * @param jobs
     */
    public void fix(final IP4Changelist list, final IP4Job[] jobs) {
        fix(new IP4Changelist[] { list }, jobs);
    }

    /**
     * Fix the changelists with the specified jobs
     * 
     * @param lists
     * @param jobs
     */
    public void fix(final IP4Changelist[] lists, final IP4Job[] jobs) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
            	try {
            		for (IP4Changelist list : lists) {
            			createCollection(jobs).fix(list);
            		}					
				} catch (Exception e) {
					PerforceProviderPlugin.logWarning(e);
				}
            }

        };
        runRunnable(runnable);
    }

    /**
     * Fix the changelist with the specified job
     * 
     * @param lists
     * @param job
     */
    public void fix(IP4Changelist[] lists, IP4Job job) {
        fix(lists, new IP4Job[] { job });
    }

    /**
     * Fix the changelist with the specified job
     * 
     * @param list
     * @param job
     */
    public void fix(IP4Changelist list, IP4Job job) {
        fix(list, new IP4Job[] { job });
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        P4Collection collection = getResourceSelection();
        IP4Resource[] resources = collection.members();
        if (resources.length == 1) {
            IP4Resource resource = resources[0];
            JobFixDialog dialog = new JobFixDialog(getShell(),
                    resource.getConnection());
            if (dialog.open() == JobFixDialog.OK) {
                IP4Job[] selected = dialog.getSelectedJobs();
                if (selected != null && selected.length > 0) {
                    runAction(selected);
                }
            }
        }
    }
}
