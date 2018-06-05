/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;

import org.eclipse.core.resources.IResource;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4TaskRepositoryLinkProvider extends
        AbstractTaskRepositoryLinkProvider {

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractTaskRepositoryLinkProvider#getTaskRepository(org.eclipse.core.resources.IResource,
     *      org.eclipse.mylyn.tasks.core.IRepositoryManager)
     */
    @Override
    public TaskRepository getTaskRepository(IResource resource,
            IRepositoryManager repositoryManager) {
        TaskRepository repository = null;
        if (resource != null) {
            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(resource.getProject());
            if (connection != null) {
                repository = P4MylynUiUtils.getRepository(connection);
            }
        }
        return repository;
    }

}
