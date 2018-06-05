/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.team.core.mylyn.IP4MylynConstants;

import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryMigrator;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4JobMigrator extends AbstractRepositoryMigrator {

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryMigrator#getConnectorKind()
     */
    @Override
    public String getConnectorKind() {
        return IP4MylynConstants.KIND;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryMigrator#migrateRepository(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public boolean migrateRepository(TaskRepository repository) {
        boolean migrated = false;
        if (repository != null
                && repository
                        .getProperty(IRepositoryConstants.PROPERTY_CATEGORY) == null) {
            repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY,
                    IRepositoryConstants.CATEGORY_TASKS);
            migrated = true;
        }
        return migrated;
    }

}
