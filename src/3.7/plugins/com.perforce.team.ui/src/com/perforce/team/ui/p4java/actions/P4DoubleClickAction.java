/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.PerforceProviderPlugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4DoubleClickAction extends P4Action {

    /**
     * OVERRIDE_CLASS_ATTRIBUTE
     */
    public static final String OVERRIDE_CLASS_ATTRIBUTE = "overrideClass"; //$NON-NLS-1$

    /**
     * BASE_CLASS_ATTRIBUTE
     */
    public static final String BASE_CLASS_ATTRIBUTE = "baseClass"; //$NON-NLS-1$

    /**
     * DOUBLE_CLICK_ELEMENT
     */
    public static final String DOUBLE_CLICK_ELEMENT = "doubleClick"; //$NON-NLS-1$

    private Map<String, IConfigurationElement> doubleClickExtensions;

    private P4Action getExtendedAction(String className) {
        P4Action action = null;
        if (className != null) {
            if (doubleClickExtensions == null) {
                doubleClickExtensions = new HashMap<String, IConfigurationElement>();
                IConfigurationElement[] elements = Platform
                        .getExtensionRegistry().getConfigurationElementsFor(
                                ACTION_EXTENSION_POINT);
                for (IConfigurationElement element : elements) {
                    if (DOUBLE_CLICK_ELEMENT.equals(element.getName())) {
                        String base = element
                                .getAttribute(BASE_CLASS_ATTRIBUTE);
                        if (base != null
                                && !doubleClickExtensions.containsKey(base)) {
                            String override = element
                                    .getAttribute(OVERRIDE_CLASS_ATTRIBUTE);
                            if (override != null) {
                                try {
                                    Object testAction = element
                                            .createExecutableExtension(OVERRIDE_CLASS_ATTRIBUTE);
                                    if (testAction instanceof P4Action) {
                                        doubleClickExtensions
                                                .put(base, element);
                                    }
                                } catch (CoreException e) {
                                    PerforceProviderPlugin.logError(e);
                                }

                            }
                        }
                    }
                }
            }
            IConfigurationElement extension = doubleClickExtensions
                    .get(className);
            if (extension != null) {
                try {
                    action = (P4Action) extension
                            .createExecutableExtension(OVERRIDE_CLASS_ATTRIBUTE);
                } catch (CoreException e) {
                    PerforceProviderPlugin.logError(e);
                }

            }
        }
        return action;
    }

    /**
     * Double click the action which allows overriding double clicks
     * 
     * @param action
     */
    public void doubleClick(Action action) {
        P4Action extension = getExtendedAction(this.getClass().getName());
        if (extension != null) {
            extension.setCollection(this.collection);
            extension.selectionChanged(action, this.getSelection());
            extension.run(action);
        } else {
            run(action);
        }
    }

}
