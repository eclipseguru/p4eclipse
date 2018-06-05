/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.diff;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;

import org.eclipse.core.resources.IStorage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IFileDiffer {

    /**
     * Generate differences for specified file via two-way comparison of
     * specified storage object that represent the specified file in two
     * different states of content.
     * 
     * @param resource
     * @param file
     * @param storage1
     * @param storage2
     */
    void generateDiff(IP4Resource resource, IP4File file, IStorage storage1,
            IStorage storage2);

    /**
     * Get differences for specified file
     * 
     * @param file
     * @return - non-null array of differences
     */
    Object[] getDiff(IP4Resource file);

    /**
     * Has the differ generated the differences for the specified file
     * 
     * @param file
     * @return - true if generated, false otherwise
     */
    boolean diffGenerated(IP4Resource file);

    /**
     * Dispose of the file differ
     */
    void dispose();

}
