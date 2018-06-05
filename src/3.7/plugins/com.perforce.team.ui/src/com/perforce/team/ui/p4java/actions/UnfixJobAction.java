/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class UnfixJobAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        P4Collection collection = getResourceSelection();
        IP4Resource[] resources = collection.members();
        boolean enabled = false;
        for (IP4Resource resource : resources) {
            if (resource instanceof IP4Job
                    && resource.getParent() instanceof IP4PendingChangelist) {
                enabled = true;
                break;
            }
        }
        return enabled;
    }

    /**
     * Unfix the changelists from the specified jobs
     * 
     * @param lists
     * @param jobs
     */
    public void unfix(final IP4Changelist[] lists, final IP4Job[] jobs) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                for (IP4Changelist list : lists) {
                    createCollection(jobs).unfix(list);
                }
            }

        };
        runRunnable(runnable);
    }

    /**
     * Unfix the changelists from the specified job
     * 
     * @param lists
     * @param job
     */
    public void unfix(IP4Changelist[] lists, IP4Job job) {
        unfix(lists, new IP4Job[] { job });
    }

    /**
     * Unfix the changelist from the specified jobs
     * 
     * @param list
     * @param jobs
     */
    public void unfix(final IP4Changelist list, IP4Job[] jobs) {
        unfix(new IP4Changelist[] { list }, jobs);
    }

    /**
     * Unfix the changelist from the specified job
     * 
     * @param list
     * @param job
     */
    public void unfix(IP4Changelist list, IP4Job job) {
        unfix(list, new IP4Job[] { job });
    }

    private void unfix(final IP4Resource[] resources) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                for (IP4Resource resource : resources) {
                    if (resource instanceof IP4Job) {
                        IP4Job job = (IP4Job) resource;
                        IP4Container parent = job.getParent();
                        if (parent instanceof IP4PendingChangelist) {
                            IP4PendingChangelist list = (IP4PendingChangelist) parent;
                            createCollection(new IP4Resource[] { job }).unfix(
                                    list);
                        }
                    }
                }
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        P4Collection collection = getResourceSelection();
        if (!collection.isEmpty()) {
            unfix(collection.members());
        }
    }
}
