/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4Revision;

import org.eclipse.ui.IStorageEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IRevisionInputCache {

    /**
     * Get cache input or create a new input for the specified p4 revision
     * 
     * @param revision
     * @return - editor input
     */
    IStorageEditorInput getRevisionInput(IP4Revision revision);

    /**
     * Empty the revision input cache
     */
    void clear();

}
