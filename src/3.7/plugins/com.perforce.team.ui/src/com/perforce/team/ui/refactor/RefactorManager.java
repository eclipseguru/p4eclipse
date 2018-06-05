/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class RefactorManager implements IRefactorManager {

    /**
     * @see com.perforce.team.ui.refactor.IRefactorManager#isEnabled()
     */
    public boolean isEnabled() {
        return isEnabled(getPreference());
    }

    /**
     * @see com.perforce.team.ui.refactor.IRefactorManager#isEnabled(java.lang.String)
     */
    public boolean isEnabled(String preference) {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(preference);
    }

    /**
     * Get active pending changelist id of associated connection for specified
     * resource
     * 
     * @param resource
     * @return - changelist id or -1
     */
    protected int getActiveId(IResource resource) {
        int id = -1;
        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(resource.getProject());
        if (connection != null) {
            IP4PendingChangelist list = connection.getActivePendingChangelist();
            if (list != null) {
                id = list.getId();
            }
        }
        return id;
    }

    /**
     * Does the resource exist?
     * 
     * @param resource
     * @return - true if resource exists, false otherwise
     */
    protected boolean exists(IResource resource) {
        IPath location = resource.getLocation();
        return location != null ? location.toFile().exists() : false;
    }

    /**
     * @see com.perforce.team.ui.refactor.IRefactorManager#getPreference()
     */
    public String getPreference() {
        return IPerforceUIConstants.PREF_REFACTOR_SUPPORT;
    }

}
