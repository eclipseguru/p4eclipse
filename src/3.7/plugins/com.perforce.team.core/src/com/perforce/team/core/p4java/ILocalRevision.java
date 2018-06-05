/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import org.eclipse.core.resources.IFile;
import org.eclipse.team.core.history.IFileRevision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface ILocalRevision extends IFileRevision {

    /**
     * Get file that this is a revision of
     * 
     * @return - parent file
     */
    IFile getFile();

    /**
     * Is this revision the latest?
     * 
     * @return - true if latest
     */
    boolean isCurrent();

    /**
     * Get local path
     * 
     * @return - local path
     */
    String getLocalPath();

}
