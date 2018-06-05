/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.core.mylyn.P4DefaultJobConfiguration;

import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TestCoreConfiguration extends P4DefaultJobConfiguration {

    /**
     * @see com.perforce.team.core.mylyn.P4DefaultJobConfiguration#isConfigurationFor(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public boolean isConfigurationFor(TaskRepository repository) {
        return repository.getRepositoryUrl().contains("testtest:1234");
    }

}
