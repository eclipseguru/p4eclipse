/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder.xml;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BuilderRegistry {

    /**
     * Tag name attribute
     */
    public static final String TAG_NAME_ATTRIBUTE = "tagName"; //$NON-NLS-1$

    /**
     * Element class attribute
     */
    public static final String ELEMENT_CLASS_ATTRIBUTE = "elementClass"; //$NON-NLS-1$

    /**
     * Builder class attribute
     */
    public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

    private Map<String, IElementBuilder> classBuilders;
    private Map<String, IElementBuilder> nameBuilders;

    /**
     * @param extensionPoint
     */
    public BuilderRegistry(String extensionPoint) {
        this.classBuilders = new HashMap<String, IElementBuilder>();
        this.nameBuilders = new HashMap<String, IElementBuilder>();
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(extensionPoint);
        generateBuilders(elements);
    }

    /**
     * Get tag names to build
     * 
     * @return non-null but possibly empty array of strings
     */
    public String[] getTagNames() {
        return this.nameBuilders.keySet().toArray(
                new String[nameBuilders.size()]);
    }

    private void generateBuilders(IConfigurationElement[] elements) {
        for (IConfigurationElement element : elements) {
            String elementName = element.getAttribute(TAG_NAME_ATTRIBUTE);
            String elementClass = element.getAttribute(ELEMENT_CLASS_ATTRIBUTE);
            String builder = element.getAttribute(CLASS_ATTRIBUTE);
            if ((elementName != null || elementClass != null)
                    && builder != null) {
                try {
                    Object createdBuilder = element
                            .createExecutableExtension(CLASS_ATTRIBUTE);
                    if (createdBuilder instanceof IElementBuilder) {
                        if (elementClass != null) {
                            this.classBuilders.put(elementClass,
                                    (IElementBuilder) createdBuilder);
                        }
                        if (elementName != null) {
                            this.nameBuilders.put(elementName,
                                    (IElementBuilder) createdBuilder);
                        }
                    }
                } catch (CoreException ce) {
                    PerforceProviderPlugin.logError(ce);
                }
            }
        }
    }

    /**
     * Get builder for tag name
     * 
     * @param tagName
     * @return element builder or null if not found
     */
    public IElementBuilder getBuilder(String tagName) {
        return tagName != null ? this.nameBuilders.get(tagName) : null;
    }

    /**
     * Get builder for element
     * 
     * @param element
     * @return element builder
     */
    public IElementBuilder getBuilder(IBranchGraphElement element) {
        return element != null ? this.classBuilders.get(element.getClass()
                .getName()) : null;

    }
}
