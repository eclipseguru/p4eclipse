/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.model;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IFolderDiffListener {

    /**
     * Diff generated callback
     * 
     * @param container
     */
    void diffsGenerated(FileDiffContainer container);

}
