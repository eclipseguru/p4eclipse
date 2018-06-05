/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobProxyContainer extends PlatformObject {

    private IRepositoryQuery query;
    private IP4Connection connection;

    /**
     * Create a job proxy container
     * 
     * @param query
     */
    public JobProxyContainer(IRepositoryQuery query) {
        this.query = query;
    }

    /**
     * Create a job proxy container
     * 
     * @param connection
     */
    public JobProxyContainer(IP4Connection connection) {
        this.connection = connection;
    }

    /**
     * Get job proxies
     * 
     * @return - non-null but possibly empty array
     */
    public IJobProxy[] getJobs() {
        if (this.query != null) {
            List<IJobProxy> jobs = new ArrayList<IJobProxy>();
            if (query instanceof ITaskContainer) {
                ITaskContainer container = (ITaskContainer) query;
                for (ITask task : container.getChildren()) {
                    jobs.add(new TaskProxy(task));
                }
            }
            return jobs.toArray(new IJobProxy[jobs.size()]);
        }
        return new IJobProxy[0];
    }

    /**
     * Get connection
     * 
     * @return - connection
     */
    public IP4Connection getConnection() {
        if (this.query != null) {
            return P4MylynUiUtils.getConnection(query.getConnectorKind(),
                    query.getRepositoryUrl());
        }
        if (this.connection != null) {
            return this.connection;
        }
        return null;
    }

}
