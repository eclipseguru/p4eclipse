/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.core.PerforceProviderPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PageFactoryRegistry {

    /**
     * PAGE_EXTENSION_POINT
     */
    public static final String PAGE_EXTENSION_POINT = "com.perforce.team.ui.page"; //$NON-NLS-1$

    /**
     * PAGE_ELEMENT
     */
    public static final String PAGE_ELEMENT = "page"; //$NON-NLS-1$

    /**
     * EDITOR_ID_ATTRIBUTE
     */
    public static final String EDITOR_ID_ATTRIBUTE = "editorId"; //$NON-NLS-1$

    /**
     * ID_ATTRIBUTE
     */
    public static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$

    /**
     * FACTORY_ATTRIBUTE
     */
    public static final String FACTORY_ATTRIBUTE = "factory"; //$NON-NLS-1$

    private static PageFactoryRegistry registry = null;

    /**
     * Get registry singleton
     * 
     * @return - page factory registry
     */
    public static synchronized PageFactoryRegistry getRegistry() {
        if (registry == null) {
            registry = new PageFactoryRegistry();
        }
        return registry;
    }

    private Map<String, Collection<IP4PageFactory>> factories;

    private PageFactoryRegistry() {
        this.factories = new HashMap<String, Collection<IP4PageFactory>>();
        load();
    }

    private void load() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(PAGE_EXTENSION_POINT);
        Map<String, Map<String, IP4PageFactory>> loadedFactories = new HashMap<String, Map<String, IP4PageFactory>>();
        for (IConfigurationElement element : elements) {
            if (PAGE_ELEMENT.equals(element.getName())) {
                String factory = element.getAttribute(FACTORY_ATTRIBUTE);
                String editorId = element.getAttribute(EDITOR_ID_ATTRIBUTE);
                String id = element.getAttribute(ID_ATTRIBUTE);
                if (factory != null && editorId != null && id != null) {
                    Object pageFactory = null;
                    try {
                        pageFactory = element
                                .createExecutableExtension(FACTORY_ATTRIBUTE);
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                    if (pageFactory instanceof IP4PageFactory) {
                        Map<String, IP4PageFactory> editorFactories = loadedFactories
                                .get(editorId);
                        if (editorFactories == null) {
                            editorFactories = new TreeMap<String, IP4PageFactory>();
                            loadedFactories.put(editorId, editorFactories);
                        }
                        editorFactories.put(id, (IP4PageFactory) pageFactory);
                    }
                }
            }
        }
        for (Map.Entry<String, Map<String, IP4PageFactory>> entry : loadedFactories.entrySet()) {
            this.factories
                    .put(entry.getKey(), entry.getValue().values());
        }
    }

    /**
     * Add contributed pages to the editor
     * 
     * @param editor
     * @param id
     */
    public void addPages(FormEditor editor, String id) {
        if (editor != null && id != null) {
            Collection<IP4PageFactory> pageFactories = this.factories.get(id);
            if (pageFactories != null) {
                for (IP4PageFactory factory : pageFactories) {
                    IFormPage page = factory.createPage(editor);
                    if (page != null) {
                        try {
                            editor.addPage(page);
                        } catch (PartInitException e) {
                            PerforceProviderPlugin.logError(e);
                        }
                    }
                }
            }
        }
    }

}
