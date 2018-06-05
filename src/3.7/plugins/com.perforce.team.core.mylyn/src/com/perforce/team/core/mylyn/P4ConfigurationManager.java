/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import com.perforce.team.core.PerforceProviderPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4ConfigurationManager {

    /**
     * CONFIGURATION_ELEMENT
     */
    public static final String CONFIGURATION_ELEMENT = "configuration"; //$NON-NLS-1$

    /**
     * CLASS_ATTRIBUTE
     */
    public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

    /**
     * ID_ATTRIBUTE
     */
    public static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$

    private Map<TaskRepository, IP4Configuration> repositoryConfigs = null;

    /**
     * Loaded configurations
     */
    protected IP4Configuration[] configs;

    private IP4Configuration defaultConfig;

    /**
     * 
     * @param extensionId
     */
    public P4ConfigurationManager(String extensionId) {
        Map<String, IP4Configuration> allConfigs = new TreeMap<String, IP4Configuration>();
        for (IConfigurationElement element : Platform.getExtensionRegistry()
                .getConfigurationElementsFor(extensionId)) {
            if (CONFIGURATION_ELEMENT.equals(element.getName())) {
                String id = element.getAttribute(ID_ATTRIBUTE);
                String configClass = element.getAttribute(CLASS_ATTRIBUTE);
                if (configClass != null && id != null) {
                    try {
                        Object config = element
                                .createExecutableExtension(CLASS_ATTRIBUTE);
                        if (isValidConfiguration(config)) {
                            allConfigs.put(id, (IP4Configuration) config);
                        }
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            }
        }
        this.configs = allConfigs.values().toArray(
                new IP4Configuration[allConfigs.size()]);
        this.defaultConfig = createDefault();
        this.repositoryConfigs = Collections
                .synchronizedMap(new WeakHashMap<TaskRepository, IP4Configuration>());
    }

    /**
     * Default default configuration
     * 
     * @return - non-null default configuration
     */
    protected abstract IP4Configuration createDefault();

    /**
     * Is valid config object
     * 
     * @param config
     * @return - true if valid, false otherwise
     */
    protected boolean isValidConfiguration(Object config) {
        return config instanceof IP4Configuration;
    }

    /**
     * Get configuration for a task repository
     * 
     * @param repository
     * @return - config or null if all returned false for configuring the
     *         specified repository
     */
    protected IP4Configuration findConfiguration(TaskRepository repository) {
        IP4Configuration config = null;
        if (repository != null) {
            config = this.repositoryConfigs.get(repository);
            if (config == null) {
                for (IP4Configuration candidate : this.configs) {
                    if (candidate.isConfigurationFor(repository)) {
                        config = candidate;
                        break;
                    }
                }
                if (config == null) {
                    config = defaultConfig;
                }
                this.repositoryConfigs.put(repository, config);
            }
        } else {
            config = defaultConfig;
        }
        return config;
    }

}
