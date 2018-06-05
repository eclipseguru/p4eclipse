/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.core.PerforceProviderPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class ContentAssistRegistry {

    /**
     * CONTENT_ASSIST_EXTENSION_POINT
     */
    public static final String CONTENT_ASSIST_EXTENSION_POINT = "com.perforce.team.ui.contentAssist"; //$NON-NLS-1$

    /**
     * ASSIST_ELEMENT
     */
    public static final String ASSIST_ELEMENT = "provider"; //$NON-NLS-1$

    /**
     * CONTEXT_ATTRIBUTE
     */
    public static final String CONTEXT_ATTRIBUTE = "context"; //$NON-NLS-1$

    /**
     * CLASS_ELEMENT
     */
    public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

    private static ContentAssistRegistry registry;

    /**
     * Get time lapse editor registry
     * 
     * @return - registry
     */
    public static synchronized ContentAssistRegistry getRegistry() {
        if (registry == null) {
            registry = new ContentAssistRegistry();
        }
        return registry;
    }

    private Map<String, List<IContentAssistProvider>> contextToProviders;

    private ContentAssistRegistry() {
        contextToProviders = new HashMap<String, List<IContentAssistProvider>>();
        loadExtensionPoints();
    }

    /**
     * Get content assist providers for specified context
     * 
     * @param context
     * @return - non-null but possibly empty array of content assist providers
     */
    public IContentAssistProvider[] getProviders(String context) {
        IContentAssistProvider[] providers = null;
        if (context != null) {
            List<IContentAssistProvider> contextProviders = this.contextToProviders
                    .get(context);
            if (contextProviders != null) {
                providers = contextProviders
                        .toArray(new IContentAssistProvider[contextProviders
                                .size()]);
            }

        }
        if (providers == null) {
            providers = new IContentAssistProvider[0];
        }
        return providers;
    }

    private void loadExtensionPoints() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(CONTENT_ASSIST_EXTENSION_POINT);
        for (IConfigurationElement element : elements) {
            if (ASSIST_ELEMENT.equals(element.getName())) {
                String context = element.getAttribute(CONTEXT_ATTRIBUTE);
                String providerClass = element.getAttribute(CLASS_ATTRIBUTE);
                if (context != null && providerClass != null) {
                    try {
                        Object provider = element
                                .createExecutableExtension(CLASS_ATTRIBUTE);
                        if (provider instanceof IContentAssistProvider) {
                            List<IContentAssistProvider> providers = this.contextToProviders
                                    .get(context);
                            if (providers == null) {
                                providers = new ArrayList<IContentAssistProvider>();
                                this.contextToProviders.put(context, providers);
                            }
                            providers.add((IContentAssistProvider) provider);
                        }
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            }
        }
    }

}
