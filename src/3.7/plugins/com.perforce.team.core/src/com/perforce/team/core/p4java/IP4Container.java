/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.io.File;

/**
 * Base interface representing a folder or depot in a Perforce repository
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Container extends IP4Resource {

    /**
     * ELLIPSIS - ...
     */
    String ELLIPSIS = "..."; //$NON-NLS-1$

    /**
     * DIR_WILDCARD
     */
    String DIR_WILDCARD = File.separatorChar + "*"; //$NON-NLS-1$

    /**
     * DIR_ELLIPSIS
     */
    String DIR_ELLIPSIS = File.separatorChar + ELLIPSIS;

    /**
     * REMOTE_ELLIPSIS
     */
    String REMOTE_ELLIPSIS = "/" + ELLIPSIS; //$NON-NLS-1$

    /**
     * DEPOT_PREFIX
     */
    String DEPOT_PREFIX = "//"; //$NON-NLS-1$

    /**
     * Gets the members of this container. This will involve making a remote
     * call to get the listing of resources if this is the first call. Otherwise
     * it will be the last list of resource retreived via a call to
     * {@link IP4Resource#refresh()}
     * 
     * @return - array of p4 resources
     */
    IP4Resource[] members();

    /**
     * Get all local files under this container at all levels
     * 
     * @return - array of files that are currently in the eclipse workspace
     *         under this container
     */
    IP4File[] getAllLocalFiles();

    /**
     * Gets the number of members in this container. This method should be used
     * instead of using {@link #members()}.length to determine container size
     * since some containers may store their resources internally in a non-array
     * collection and it could be more efficient to get the size of a collection
     * without unnecessarily converting to an array.
     * 
     * @return number of members in container
     */
    int size();

}
