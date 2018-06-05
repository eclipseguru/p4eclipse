/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

import com.perforce.team.core.p4java.IP4File;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IDepotMatch {

    /**
     * Open match in editor
     */
    void openInEditor();

    /**
     * Get full depot path of match. This will include the revision specifier
     * 
     * @return - full depot path
     */
    String getDepotPath();

    /**
     * Get revision number of match
     * 
     * @return - revision number
     */
    int getRevision();

    /**
     * Get file of match
     * 
     * @return - file
     */
    IP4File getFile();

}
