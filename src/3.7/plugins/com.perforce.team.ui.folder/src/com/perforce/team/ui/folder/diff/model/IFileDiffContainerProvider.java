/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.model;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IFileDiffContainerProvider {

    /**
     * Get container
     * 
     * @return file diff container
     */
    FileDiffContainer getContainer();

    /**
     * Add folder diff listener
     * 
     * @param listener
     */
    void addListener(IFolderDiffListener listener);

    /**
     * Remove folder diff listener
     * 
     * @param listener
     */
    void removeListener(IFolderDiffListener listener);

}
