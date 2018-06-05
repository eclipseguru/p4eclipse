/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.team.core.mylyn.IP4Configuration;
import com.perforce.team.core.mylyn.P4ConfigurationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.LegendElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4JobUiConfigurationManager extends P4ConfigurationManager {

    /**
     * EXTENSION_ID
     */
    public static final String EXTENSION_ID = "com.perforce.team.ui.mylyn.configuration"; //$NON-NLS-1$

    /**
     */
    public P4JobUiConfigurationManager() {
        super(EXTENSION_ID);
    }

    /**
     * @see com.perforce.team.core.mylyn.P4ConfigurationManager#isValidConfiguration(java.lang.Object)
     */
    @Override
    protected boolean isValidConfiguration(Object config) {
        return config instanceof IP4JobUiConfiguration;
    }

    /**
     * Get job ui configuration for specified repository
     * 
     * @param repository
     * @return - job ui configuration
     */
    public IP4JobUiConfiguration getConfiguration(TaskRepository repository) {
        return (IP4JobUiConfiguration) findConfiguration(repository);
    }

    /**
     * @see com.perforce.team.core.mylyn.P4ConfigurationManager#createDefault()
     */
    @Override
    protected IP4Configuration createDefault() {
        return new P4DefaultJobUiConfiguration();
    }

    /**
     * Get all contributed legend elements
     * 
     * @return - legend elements
     */
    public List<LegendElement> getLegendElements() {
        List<LegendElement> elements = new ArrayList<LegendElement>();
        for (IP4Configuration config : configs) {
            Collection<LegendElement> contributed = ((IP4JobUiConfiguration) config)
                    .getLegendElements();
            if (contributed != null && !contributed.isEmpty()) {
                elements.addAll(contributed);
            }
        }
        return elements;
    }
}
