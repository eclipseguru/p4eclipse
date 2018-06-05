/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.p4java.IP4Connection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IBulkChange {

    /**
     * Add the jobs to this model
     * 
     * @param jobs
     */
    void add(IJobProxy[] jobs);

    /**
     * Get connection for bulk change
     * 
     * @return - p4 connection
     */
    IP4Connection getConnection();

}
