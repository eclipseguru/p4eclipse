/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import com.perforce.p4java.server.callback.ILogCallback.LogTraceLevel;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4WorkspaceConfigurer;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * This class syncs the settings between the UI plugin and the core p4 workspace
 * object. It sets the initial UI error display class and other p4 workspace
 * settings initially and updates them appropriately when preference changes
 * occur
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4WorkspaceConfigurer implements IP4WorkspaceConfigurer {

    private void configureUiSettings(final P4Workspace workspace) {
        IPreferenceStore uiStore = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        loadP4JavaProperties(
                workspace,
                uiStore.getString(IPreferenceConstants.CUSTOM_P4JAVA_PROPERTIES));
        workspace.setPersistOffline(uiStore
                .getBoolean(IPreferenceConstants.PERSIST_OFFINE));
        loadTraceLevel(workspace,
                uiStore.getString(IPreferenceConstants.TRACE_LEVEL));

        IPropertyChangeListener uiListener = new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (IPreferenceConstants.TRACE_LEVEL
                        .equals(event.getProperty())) {
                    loadTraceLevel(workspace, event.getNewValue());
                } else if (IPreferenceConstants.CUSTOM_P4JAVA_PROPERTIES
                        .equals(event.getProperty())) {
                    String value = event.getNewValue() != null ? event
                            .getNewValue().toString() : null;
                    loadP4JavaProperties(workspace, value);
                }
            }
        };
        uiStore.addPropertyChangeListener(uiListener);
    }

    private void loadTraceLevel(P4Workspace workspace, Object value) {
        String sValue = value != null ? value.toString() : null;
        if (sValue != null && sValue.length() > 0) {
            try {
                LogTraceLevel level = LogTraceLevel.valueOf(sValue);
                workspace.setTraceLevel(level);
            } catch (IllegalArgumentException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    private void loadP4JavaProperties(P4Workspace workspace, String properties) {
        workspace.getAdvancedProperties().clear();
        if (properties != null) {
            String[] pairs = properties
                    .split(IPreferenceConstants.VALUE_DELIMITER);
            for (String pair : pairs) {
                String[] sections = pair
                        .split(IPreferenceConstants.PAIR_DELIMITER);
                if (sections.length == 2) {
                    workspace.getAdvancedProperties().put(sections[0],
                            sections[1]);
                }
            }
            workspace.getServerProperties().putAll(workspace.getAdvancedProperties());
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4WorkspaceConfigurer#configure(com.perforce.team.core.p4java.P4Workspace)
     */
    public void configure(final P4Workspace workspace) {
        workspace.setErrorHandler(P4ConnectionManager.getManager());
        configureUiSettings(workspace);
    }
}
