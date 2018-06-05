/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4ConnectionProvider;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class UserTaskGroup extends TaskGroup {

    private IP4ConnectionProvider provider;

    /**
     * Create a user task group
     * 
     * @param name
     * @param provider
     */
    public UserTaskGroup(String name, IP4ConnectionProvider provider) {
        super(name);
        this.provider = provider;
    }

    /**
     * Is this grouping associated with the user of the configured connection
     * 
     * @return - true if current user, false otherwise
     */
    public boolean isCurrentUser() {
        IP4Connection connection = this.provider.getConnection();
        if (connection == null) {
            return false;
        }
        String name = this.getLabel(this);
        if (connection.isCaseSensitive()) {
            return name.equals(connection.getParameters().getUser());
        } else {
            return name.equalsIgnoreCase(connection.getParameters().getUser());
        }
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        if (isCurrentUser()) {
            return P4BranchGraphPlugin
                    .getImageDescriptor("icons/user_current.png"); //$NON-NLS-1$
        } else {
            return P4BranchGraphPlugin
                    .getImageDescriptor("icons/user_other.png"); //$NON-NLS-1$
        }
    }

}