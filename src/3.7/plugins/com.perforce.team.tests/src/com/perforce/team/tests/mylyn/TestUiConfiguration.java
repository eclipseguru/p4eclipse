/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.ui.mylyn.P4DefaultJobUiConfiguration;

import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TestUiConfiguration extends P4DefaultJobUiConfiguration {

    /**
     * @see com.perforce.team.ui.mylyn.P4DefaultJobUiConfiguration#isConfigurationFor(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public boolean isConfigurationFor(TaskRepository repository) {
        return repository.getRepositoryUrl().contains("testtest:1234");
    }

}
