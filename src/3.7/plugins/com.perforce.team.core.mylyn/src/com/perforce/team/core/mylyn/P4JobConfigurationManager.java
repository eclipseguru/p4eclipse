/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * Manages extension point contributed {@link IP4JobConfiguration} elements.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4JobConfigurationManager extends P4ConfigurationManager {

    /**
     * EXTENSION_ID
     */
    public static final String EXTENSION_ID = "com.perforce.team.core.mylyn.configuration"; //$NON-NLS-1$

    /**
     * Create a job configuration manager
     */
    public P4JobConfigurationManager() {
        super(EXTENSION_ID);
    }

    /**
     * @see com.perforce.team.core.mylyn.P4ConfigurationManager#isValidConfiguration(java.lang.Object)
     */
    @Override
    protected boolean isValidConfiguration(Object config) {
        return config instanceof IP4JobConfiguration;
    }

    /**
     * Get a job configuration for the specified task repository
     * 
     * @param repository
     * @return - job configuration
     */
    public IP4JobConfiguration getConfiguration(TaskRepository repository) {
        return (IP4JobConfiguration) findConfiguration(repository);
    }

    /**
     * @see com.perforce.team.core.mylyn.P4ConfigurationManager#createDefault()
     */
    @Override
    protected IP4Configuration createDefault() {
        return new P4DefaultJobConfiguration();
    }

}
