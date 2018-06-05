/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.descriptors;

import com.perforce.team.core.extensions.ExtensionPointRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DescriptorRegistry extends ExtensionPointRegistry {

    /**
     * NAME_ATTRIBUTE
     */
    public static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$

    /**
     * TYPE_ATTRIBUTE
     */
    public static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$

    /**
     * ICON_ATTRIBUTE
     */
    public static final String ICON_ATTRIBUTE = "icon"; //$NON-NLS-1$

    /**
     * IMPORTANT_ATTRIBUTE
     */
    public static final String IMPORTANT_ATTRIBUTE = "important"; //$NON-NLS-1$

    /**
     * CREATE_DESCRIPTION_ATTRIBUTE
     */
    public static final String CREATE_DESCRIPTION_ATTRIBUTE = "createDescription"; //$NON-NLS-1$

    private String name;
    private Map<String, ElementDescriptor> descriptors;

    /**
     * Create descriptor registry
     * 
     * @param extensionPointId
     * @param elementName
     */
    public DescriptorRegistry(String extensionPointId, String elementName) {
        this.descriptors = new HashMap<String, ElementDescriptor>();
        this.name = elementName;
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(extensionPointId);
        List<IConfigurationElement> matching = new ArrayList<IConfigurationElement>();
        for (IConfigurationElement element : elements) {
            if (elementName.equals(element.getAttribute(NAME_ATTRIBUTE))) {
                matching.add(element);
            }
        }
        if (!matching.isEmpty()) {
            buildDescriptors(matching);
        }
    }

    /**
     * Get name
     * 
     * @return element name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get element descriptors
     * 
     * @return non-null but possibly empty array of descriptors
     */
    public ElementDescriptor[] getDescriptors() {
        return this.descriptors.values().toArray(
                new ElementDescriptor[this.descriptors.size()]);
    }

    /**
     * Get descriptor for type
     * 
     * @param type
     * @return element descriptor or null if none for type
     */
    public ElementDescriptor getDescriptor(String type) {
        ElementDescriptor descriptor = null;
        if (type != null) {
            descriptor = this.descriptors.get(type);
        }
        return descriptor;
    }

    /**
     * Build descriptors
     * 
     * @param elements
     */
    protected void buildDescriptors(Collection<IConfigurationElement> elements) {
        for (IConfigurationElement element : elements) {
            String type = element.getAttribute(TYPE_ATTRIBUTE);
            String icon = element.getAttribute(ICON_ATTRIBUTE);
            IConfigurationElement[] description = element
                    .getChildren(CREATE_DESCRIPTION_ATTRIBUTE);
            if (type != null && icon != null && description.length == 1) {
                ImageDescriptor image = AbstractUIPlugin
                        .imageDescriptorFromPlugin(element.getContributor()
                                .getName(), icon);
                ElementDescriptor descriptor = new ElementDescriptor(this.name,
                        type, image, description[0].getValue(),
                        getBoolean(element.getAttribute(IMPORTANT_ATTRIBUTE)));
                if (!this.descriptors.containsKey(type)) {
                    this.descriptors.put(type, descriptor);
                }
            }
        }
    }
}
