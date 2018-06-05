/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileSpec;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IP4SubmittedFile extends IP4Resource {

    /**
     * Get underlying p4 file
     * 
     * @return - p4 file
     */
    IP4File getFile();

    /**
     * Get submitted changelist
     * 
     * @return - p4 submitted changelist
     */
    IP4SubmittedChangelist getChangelist();

    /**
     * Gets the action
     * 
     * @return - file action
     */
    FileAction getAction();

    /**
     * Get underlying p4java file spec
     * 
     * @return - file spec
     */
    IFileSpec getFileSpec();

    /**
     * Get revision number
     * 
     * @return - revision number of submitted file
     */
    int getRevision();

}
