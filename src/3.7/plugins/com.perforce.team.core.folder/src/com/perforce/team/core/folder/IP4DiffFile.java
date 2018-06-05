/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.folder;

import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;

import java.io.InputStream;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4DiffFile extends IP4Resource {

    /**
     * Get underlying p4 file
     * 
     * @return p4 file
     */
    IP4File getFile();

    /**
     * Get contents of file 1 in diff
     * 
     * @return input stream
     */
    InputStream getFile1Contents();

    /**
     * Get contents of file 2 in diff
     * 
     * @return input stream
     */
    InputStream getFile2Contents();

    /**
     * Get underlying file diff
     * 
     * @return file diff
     */
    IFileDiff getDiff();

    /**
     * Get diff file pair
     * 
     * @return diff file
     */
    IP4DiffFile getPair();

    /**
     * Get diff status
     * 
     * @return diff status
     */
    Status getStatus();

    /**
     * Get revision number of diff file
     * 
     * @return revision number
     */
    int getRevision();

    /**
     * Does this diff file represent file 1 of the diff?
     * 
     * @return true if file 1, false if file 2
     */
    boolean isFile1();

}