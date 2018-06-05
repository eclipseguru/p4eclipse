/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.p4java.IP4Connection;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BulkJobInput extends PlatformObject implements IEditorInput {

    private IP4Connection connection;
    private IJobProxy[] jobs;

    /**
     * Create a bulk job editor input
     * 
     * @param connection
     */
    public BulkJobInput(IP4Connection connection) {
        this(connection, new JobProxy[0]);
    }

    /**
     * Create a bulk job editor input
     * 
     * @param connection
     * @param jobs
     */
    public BulkJobInput(IP4Connection connection, IJobProxy[] jobs) {
        this.connection = connection;
        this.jobs = jobs;
    }

    /**
     * Get jobs in input
     * 
     * @return - non-null but possibly empty array
     */
    public IJobProxy[] getJobs() {
        return this.jobs;
    }

    /**
     * Get connection of input
     * 
     * @return - connection
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists() {
        return false;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return Messages.BulkJobInput_BulkJobChanges;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        return null;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return Messages.BulkJobInput_PerformBulkChanges;
    }

}
