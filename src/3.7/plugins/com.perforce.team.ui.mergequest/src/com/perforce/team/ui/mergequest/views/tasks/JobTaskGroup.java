/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobTaskGroup extends TaskGroup {

    /**
     * NO_TASKS_ATTACHED
     */
    public static final String NO_TASKS_ATTACHED = Messages.JobTaskGroup_NoJobsAttached;

    private IP4Job job;

    /**
     * Create job task group
     * 
     * @param job
     */
    public JobTaskGroup(IP4Job job) {
        super(job != null ? job.getId() : NO_TASKS_ATTACHED);
        this.job = job;
    }

    /**
     * Get job
     * 
     * @return - job or null
     */
    public IP4Job getJob() {
        return this.job;
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        return PerforceUIPlugin.getDescriptor(IPerforceUIConstants.IMG_CHG_JOB);
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        Object adapted = null;
        if (adapter == IP4Job.class || adapter == IP4Resource.class) {
            adapted = this.job;
        }
        if (adapted == null) {
            adapted = super.getAdapter(adapter);
        }
        return adapted;
    }

}
