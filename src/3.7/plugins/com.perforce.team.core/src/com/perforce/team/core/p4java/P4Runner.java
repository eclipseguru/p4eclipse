/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class P4Runner {

	// job family
	public static final Object FAMILY_P4_RUNNER = new Object();
	
    private P4Runner() {
        // Does nothing by default
    }

    /**
     * Create a scheduling rule
     * 
     * @return - rule
     */
    public static ISchedulingRule createRule() {
        return new ISchedulingRule() {

            public boolean isConflicting(ISchedulingRule rule) {
                return this == rule;
            }

            public boolean contains(ISchedulingRule rule) {
                return this == rule;
            }
        };
    }

    /**
     * Schedules a p4 runnable
     * 
     * @param runnable
     * @return - created job
     */
    public static Job schedule(final IP4Runnable runnable) {
        return schedule(runnable, null);
    }

    /**
     * Schedules a p4 runnable with an optional rule
     * 
     * @param runnable
     * @param rule
     * @return - created job
     */
    public static Job schedule(final IP4Runnable runnable, ISchedulingRule rule) {
        if (runnable != null) {
            String title = runnable.getTitle();
            if (title == null) {
                title = Messages.P4Runner_0;
            }
            Job job = new Job(title) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    runnable.run(monitor);
                    return Status.OK_STATUS;
                }
                
                @Override
                public boolean belongsTo(Object family) {
                	return FAMILY_P4_RUNNER==family;
                }

            };
            job.setRule(rule);
            job.setSystem(runnable.getTitle() == null);
            job.schedule();
            return job;
        }
        return null;
    }

}
