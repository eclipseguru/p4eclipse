/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.model;

import com.perforce.team.ui.changelists.Folder.Type;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IGroupProvider {

    /**
     * Get parent of entry
     * 
     * @param entry
     * @return parent
     */
    Object getParent(FileEntry entry);

    /**
     * Get unique pair
     * 
     * @param entry
     * @return unique pair
     */
    Object getUniquePair(FileEntry entry);

    /**
     * Get type of parent currently configured to return
     * 
     * @return type
     */
    Type getType();

}
