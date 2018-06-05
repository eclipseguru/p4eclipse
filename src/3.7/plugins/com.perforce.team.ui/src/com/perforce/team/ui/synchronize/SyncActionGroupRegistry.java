/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.PerforceProviderPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SyncActionGroupRegistry {

    /**
     * SYNCHRONIZE_POINT
     */
    public static final String SYNCHRONIZE_POINT = "com.perforce.team.ui.synchronize"; //$NON-NLS-1$

    /**
     * ACTIONS_ELEMENT
     */
    public static final String ACTIONS_ELEMENT = "actions"; //$NON-NLS-1$

    /**
     * CLASS_ATTRIBUTE
     */
    public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

    private static SyncActionGroupRegistry registry = null;

    private List<IConfigurationElement> actionGroups;

    /**
     * Get sync action group registry
     * 
     * @return registry
     */
    public static SyncActionGroupRegistry getRegistry() {
        if (registry == null) {
            registry = new SyncActionGroupRegistry();
        }
        return registry;
    }

    /**
     * Create registry using {@link #SYNCHRONIZE_POINT} extension point
     */
    public SyncActionGroupRegistry() {
        this(SYNCHRONIZE_POINT);
    }

    /**
     * Create registry for specified extension point
     * 
     * @param extensionPoint
     */
    public SyncActionGroupRegistry(String extensionPoint) {
        this.actionGroups = new ArrayList<IConfigurationElement>();
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(extensionPoint);
        for (IConfigurationElement element : elements) {
            if (ACTIONS_ELEMENT.equals(element.getName())) {
                String className = element.getAttribute(CLASS_ATTRIBUTE);
                if (className != null) {
                    try {
                        Object group = element
                                .createExecutableExtension(CLASS_ATTRIBUTE);
                        if (group instanceof SynchronizePageActionGroup) {
                            actionGroups.add(element);
                        }
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            }
        }
    }

    /**
     * Generate action groups
     * 
     * @return non-null but possibly empty array
     */
    public SynchronizePageActionGroup[] generateGroups() {
        List<SynchronizePageActionGroup> groups = new ArrayList<SynchronizePageActionGroup>();
        for (IConfigurationElement element : actionGroups) {
            try {
                SynchronizePageActionGroup group = (SynchronizePageActionGroup) element
                        .createExecutableExtension(CLASS_ATTRIBUTE);
                if (group != null) {
                    groups.add(group);
                }
            } catch (CoreException e) {
                // Ignore since logged during initial load
            }
        }
        return groups.toArray(new SynchronizePageActionGroup[groups.size()]);
    }

}
