/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.diff;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class DiffRegistry {

    /**
     * FILE_DIFF_EXTENSION_POINT
     */
    public static final String FILE_DIFF_EXTENSION_POINT = "com.perforce.team.ui.fileDiff"; //$NON-NLS-1$

    /**
     * DIFF_ELEMENT
     */
    public static final String DIFF_ELEMENT = "diff"; //$NON-NLS-1$

    /**
     * CONTENT_TYPE_ELEMENT
     */
    public static final String CONTENT_TYPE_ATTRIBUTE = "contentType"; //$NON-NLS-1$

    /**
     * EXTENSION_ATTRIBUTE
     */
    public static final String EXTENSION_ATTRIBUTE = "extension"; //$NON-NLS-1$

    /**
     * CLASS_ELEMENT
     */
    public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

    private static DiffRegistry registry;

    /**
     * Get time lapse editor registry
     * 
     * @return - registry
     */
    public static synchronized DiffRegistry getRegistry() {
        if (registry == null) {
            registry = new DiffRegistry();
        }
        return registry;
    }

    private Map<String, IConfigurationElement> typeToDiffers;
    private Map<String, IConfigurationElement> extToDiffers;

    private DiffRegistry() {
        typeToDiffers = new HashMap<String, IConfigurationElement>();
        extToDiffers = new HashMap<String, IConfigurationElement>();
        loadExtensionPoints();
    }

    /**
     * Get differ for a resource
     * 
     * @param resource
     * @return - differ
     */
    public IFileDiffer getDiffer(IP4Resource resource) {
        IFileDiffer differ = null;
        if (resource != null) {
            String name = resource.getName();
            if (name != null) {
                IContentType type = Platform.getContentTypeManager()
                        .findContentTypeFor(name);
                if (type != null) {
                    return getDiffer(type, true);
                }
            }
        }
        return differ;
    }

    /**
     * Get array of content types with registered time lapse editors
     * 
     * @return - content types
     */
    public String[] getContentTypes() {
        Collection<String> types = this.typeToDiffers.keySet();
        return types.toArray(new String[types.size()]);
    }

    /**
     * Get the registered differ for the specified file extension
     * 
     * @param extension
     * @return - file differ or null if none for the specified extension
     */
    public IFileDiffer getDifferByExtension(String extension) {
        IFileDiffer differ = null;
        if (extension != null) {
            try {
                IConfigurationElement element = extToDiffers.get(extension);
                if (element != null) {
                    differ = (IFileDiffer) element
                            .createExecutableExtension(CLASS_ATTRIBUTE);
                }
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return differ;
    }

    /**
     * Get differ for a specified non-null content type
     * 
     * @param contentType
     * @param checkBaseType
     * @return - differ or null if none registered for the specified content
     *         type
     */
    public IFileDiffer getDiffer(IContentType contentType, boolean checkBaseType) {
        IFileDiffer differ = null;
        if (contentType != null) {
            try {
                IConfigurationElement element = typeToDiffers.get(contentType
                        .getId());
                if (element != null) {
                    differ = (IFileDiffer) element
                            .createExecutableExtension(CLASS_ATTRIBUTE);
                } else {
                    IContentType parent = contentType.getBaseType();
                    if (parent != null && !parent.equals(contentType)) {
                        differ = getDiffer(parent, true);
                    }
                }
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return differ;
    }

    /**
     * Get differ for a specified non-null content type
     * 
     * @param contentType
     * @return - differ or null if none registered for the specified content
     *         type
     */
    public IFileDiffer getDiffer(String contentType) {
        if (contentType != null) {
            try {
                IConfigurationElement element = typeToDiffers.get(contentType);
                if (element != null) {
                    return (IFileDiffer) element
                            .createExecutableExtension(CLASS_ATTRIBUTE);
                }
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return null;
    }

    private void loadExtensionPoints() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(FILE_DIFF_EXTENSION_POINT);
        for (IConfigurationElement element : elements) {
            if (DIFF_ELEMENT.equals(element.getName())) {
                String diffClass = element.getAttribute(CLASS_ATTRIBUTE);
                if (diffClass != null) {
                    Object differ = null;
                    try {
                        differ = element
                                .createExecutableExtension(CLASS_ATTRIBUTE);
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                    if (differ instanceof IFileDiffer) {
                        String contentType = element
                                .getAttribute(CONTENT_TYPE_ATTRIBUTE);
                        if (contentType != null) {
                            this.typeToDiffers.put(contentType, element);
                        }
                        String extension = element
                                .getAttribute(EXTENSION_ATTRIBUTE);
                        if (extension != null) {
                            this.extToDiffers.put(extension, element);
                        }
                    }
                }
            }
        }
    }

}
