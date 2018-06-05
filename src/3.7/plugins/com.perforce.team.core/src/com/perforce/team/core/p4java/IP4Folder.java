/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Folder extends IP4Container {

    /**
     * Gets the local Eclipse workspace containers representing this p4 folder
     * 
     * @return - array of workspace containers
     */
    IContainer[] getLocalContainers();

    /**
     * Updates the locations of this folder by doing a where
     */
    void updateLocation();

    /**
     * Get the first depot path returned from a p4 where on the folder's current
     * local path. This will return null if this folder only has a depot path.
     * 
     * @return - first depot mapping returned from a 'p4 where'
     */
    String getFirstWhereRemotePath();

    /**
     * Get complete history of folder
     * 
     * @param monitor
     * @return - p4 revision history
     */
    IP4ChangelistRevision[] getCompleteHistory(IProgressMonitor monitor);

}
