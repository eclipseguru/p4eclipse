/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.preferences.IPreferenceHandler;
import com.perforce.team.ui.views.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class TimeLapseRegistry {

    /**
     * TIMELAPSE_EXTENSION_POINT
     */
    public static final String TIMELAPSE_EXTENSION_POINT = "com.perforce.team.ui.timelapse"; //$NON-NLS-1$

    /**
     * CONTENT_TYPE_ELEMENT
     */
    public static final String CONTENT_TYPE_ELEMENT = "contentType"; //$NON-NLS-1$

    /**
     * EDITOR_ELEMENT
     */
    public static final String EDITOR_ELEMENT = "editor"; //$NON-NLS-1$

    /**
     * CONTEXT_HANDLER_ELEMENT
     */
    public static final String CONTEXT_HANDLER_ELEMENT = "contextHandler"; //$NON-NLS-1$

    /**
     * PREFERENCE_HANDLER_ELEMENT
     */
    public static final String PREFERENCE_HANDLER_ELEMENT = "preferenceHandler"; //$NON-NLS-1$

    /**
     * ID_ATTRIBUTE
     */
    public static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$

    /**
     * CLASS_ATTRIBUTE
     */
    public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

    private static TimeLapseRegistry registry;

    /**
     * Get time lapse editor registry
     * 
     * @return - registry
     */
    public static synchronized TimeLapseRegistry getRegistry() {
        if (registry == null) {
            registry = new TimeLapseRegistry();
        }
        return registry;
    }

    /**
     * Time lapse extension point entry
     */
    private static class Entry {

        String editorId;
        IContextHandler handler;
        IPreferenceHandler prefHandler;
    }

    private Map<String, Entry> typeToEditors;

    private TimeLapseRegistry() {
        typeToEditors = new HashMap<String, Entry>();
        loadExtensionPoints();
    }

    /**
     * Get array of content types with registered time lapse editors
     * 
     * @return - content types
     */
    public String[] getContentTypes() {
        Collection<String> types = this.typeToEditors.keySet();
        return types.toArray(new String[types.size()]);
    }

    /**
     * Get editor id for specified file
     * 
     * @param file
     * @return - editor id
     */
    public String getEditorId(IFile file) {
        String editor = null;
        if (file != null) {
            try {
                IContentDescription description = file.getContentDescription();
                if (description != null) {
                    editor = getEditorId(description.getContentType());
                }
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return editor;
    }

    /**
     * Get editor id for specified content type
     * 
     * @param type
     * @return - editor id
     */
    public String getEditorId(IContentType type) {
        return getEditorId(type, true);
    }

    /**
     * Get editor id for specified content type
     * 
     * @param type
     * @param checkBaseType
     * @return - editor id
     */
    public String getEditorId(IContentType type, boolean checkBaseType) {
        String editor = null;
        if (type != null) {
            editor = getEditorId(type.getId());
            if (checkBaseType && editor == null) {
                editor = getEditorId(type.getBaseType());
            }
        }
        return editor;
    }

    /**
     * Get editor id for content type excluding it if it is disabled
     * 
     * @param contentType
     * @return - editor id or null if not found or disabled.
     */
    public String getEditorId(String contentType) {
        return getEditorId(contentType, true);
    }

    /**
     * Get editor id for content type optionally excluding it if is it disabled.
     * 
     * @param contentType
     * @param checkIfDisabled
     * @return - editor id or null if not found or disabled and checkIfDisabled
     *         is specified as true.
     */
    public String getEditorId(String contentType, boolean checkIfDisabled) {
        if (checkIfDisabled && contentType != null) {
            String[] disabled = SessionManager
                    .getEntries(IPreferenceConstants.DISABLED_TIMELAPSE_CONTENT_TYPES);
            if (Arrays.asList(disabled).contains(contentType)) {
                contentType = null;
            }
        }
        return getEditor(contentType);
    }

    /**
     * Get context handler for a specified file
     * 
     * @param file
     * @return - editor id
     */
    public IContextHandler getHandler(IFile file) {
        IContextHandler handler = null;
        if (file != null) {
            try {
                IContentDescription description = file.getContentDescription();
                if (description != null) {
                    handler = getHandler(description.getContentType());
                }
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return handler;
    }

    /**
     * Get context handler for a specified content type
     * 
     * @param type
     * @return - context handler or null if no context handler registered for
     *         the specified content type.
     */
    public IContextHandler getHandler(IContentType type) {
        return type != null ? getHandler(type.getId()) : null;
    }

    /**
     * Get context handler for a specified content type
     * 
     * @param contentType
     * @return - context handler or null if no context handler registered for
     *         the specified content type.
     */
    public IContextHandler getHandler(String contentType) {
        Entry entry = null;
        if (contentType != null) {
            entry = this.typeToEditors.get(contentType);
        }
        return entry != null ? entry.handler : null;
    }

    /**
     * Get registered preference handlers
     * 
     * @return - non-null array of preference handlers
     */
    public IPreferenceHandler[] getPreferenceHandlers() {
        List<IPreferenceHandler> handlers = new ArrayList<IPreferenceHandler>();
        for (Entry entry : this.typeToEditors.values()) {
            if (entry.prefHandler != null) {
                handlers.add(entry.prefHandler);
            }
        }
        return handlers.toArray(new IPreferenceHandler[handlers.size()]);
    }

    private String getEditor(String contentType) {
        Entry entry = null;
        if (contentType != null) {
            entry = this.typeToEditors.get(contentType);
        }
        return entry != null ? entry.editorId : null;
    }

    private void loadExtensionPoints() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(TIMELAPSE_EXTENSION_POINT);
        for (IConfigurationElement element : elements) {
            if (EDITOR_ELEMENT.equals(element.getName())) {
                String id = element.getAttribute(ID_ATTRIBUTE);
                if (id != null) {

                    IContextHandler handler = null;
                    IConfigurationElement[] handlers = element
                            .getChildren(CONTEXT_HANDLER_ELEMENT);
                    // Only use first handler found, possibly support handler
                    // chain in the future
                    if (handlers.length > 0) {
                        String handlerClass = handlers[0]
                                .getAttribute(CLASS_ATTRIBUTE);
                        if (handlerClass != null) {
                            try {
                                Object possibleHandler = handlers[0]
                                        .createExecutableExtension(CLASS_ATTRIBUTE);
                                if (possibleHandler instanceof IContextHandler) {
                                    handler = (IContextHandler) possibleHandler;
                                }
                            } catch (CoreException e) {
                                PerforceProviderPlugin.logError(e);
                            }
                        }
                    }

                    IPreferenceHandler prefHandler = null;
                    IConfigurationElement[] prefHandlers = element
                            .getChildren(PREFERENCE_HANDLER_ELEMENT);
                    // Only use first pref handler found, possibly support
                    // handler
                    // chain in the future
                    if (prefHandlers.length > 0) {
                        String handlerClass = prefHandlers[0]
                                .getAttribute(CLASS_ATTRIBUTE);
                        if (handlerClass != null) {
                            try {
                                Object possibleHandler = prefHandlers[0]
                                        .createExecutableExtension(CLASS_ATTRIBUTE);
                                if (possibleHandler instanceof IPreferenceHandler) {
                                    prefHandler = (IPreferenceHandler) possibleHandler;
                                }
                            } catch (CoreException e) {
                                PerforceProviderPlugin.logError(e);
                            }
                        }
                    }

                    IConfigurationElement[] types = element
                            .getChildren(CONTENT_TYPE_ELEMENT);
                    for (IConfigurationElement typeElement : types) {
                        String typeId = typeElement.getAttribute(ID_ATTRIBUTE);
                        if (typeId != null) {
                            Entry entry = new Entry();
                            entry.handler = handler;
                            entry.prefHandler = prefHandler;
                            entry.editorId = id;
                            this.typeToEditors.put(typeId, entry);
                        }
                    }
                }
            }
        }
    }
}
