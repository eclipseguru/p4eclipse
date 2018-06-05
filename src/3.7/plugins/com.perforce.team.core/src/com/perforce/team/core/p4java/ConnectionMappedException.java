/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.team.core.PerforceProviderPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionMappedException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private transient IStatus status = null;

    /**
     * Creates a new excpetion of for projects that are still mapped to a
     * connection
     * 
     * @param stillMappedProject
     */
    public ConnectionMappedException(IProject[] stillMappedProject) {
        if (stillMappedProject != null && stillMappedProject.length > 0) {
            List<IStatus> children = new ArrayList<IStatus>();
            children.add(new Status(
                    IStatus.ERROR,
                    PerforceProviderPlugin.ID,
                    IStatus.OK,
                    "The projects that are shared with the selected connection are:",
                    null));
            for (IProject project : stillMappedProject) {
                children.add(new Status(IStatus.ERROR,
                        PerforceProviderPlugin.ID, IStatus.OK, project
                                .getName(), null));
            }
            status = new MultiStatus(
                    PerforceProviderPlugin.ID,
                    IStatus.ERROR,
                    children.toArray(new IStatus[0]),
                    "One or more projects are still associated with this connection.  Select the Details button to show those projects.",
                    null);
        } else {
            status = new Status(IStatus.OK, PerforceProviderPlugin.ID,
                    IStatus.OK, "", null); //$NON-NLS-1$
        }
    }

    /**
     * Get the status of this exception
     * 
     * @return - status of exception containing messages to display
     */
    public IStatus getStatus() {
        return this.status;
    }

}
