/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.mylyn.IP4MylynUiConstants;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobProxy extends PlatformObject implements IJobProxy {

    private IP4Job job;

    /**
     * Create a job proxy
     * 
     * @param job
     */
    public JobProxy(IP4Job job) {
        this.job = job;
    }

    /**
     * @see com.perforce.team.ui.mylyn.job.IJobProxy#getConnection()
     */
    public IP4Connection getConnection() {
        return this.job.getConnection();
    }

    /**
     * @see com.perforce.team.ui.mylyn.job.IJobProxy#getId()
     */
    public String getId() {
        return this.job.getId();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return PerforceUiMylynPlugin
                .getImageDescriptor(IP4MylynUiConstants.IMG_JOB);
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return this.job.getId() + " : " + this.job.getShortDescription(); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof JobProxy) {
            return this.job.equals(((JobProxy) obj).job);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.job.hashCode();
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IP4Resource.class == adapter || IP4Job.class == adapter) {
            return this.job;
        }
        if (ITask.class == adapter) {
            return P4MylynUiUtils.getTask(getConnection().getParameters()
                    .getPort(), getId());
        }
        return super.getAdapter(adapter);
    }

}
