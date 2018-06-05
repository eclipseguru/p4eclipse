/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * Base interface for configuration extension point objects to determine at
 * runtime whether the can and should configure the specified task repository.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Configuration {

    /**
     * Is this a configuration of the job spec for the specified task
     * repository?
     * 
     * @param repository
     * @return - true if configuration, false otherwise
     */
    boolean isConfigurationFor(TaskRepository repository);

}
