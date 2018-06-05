/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import java.io.File;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface ITempFileInput {

    /**
     * Should this temp file input be deleted after fetched?
     * 
     * @return - true if users of this input can should delete the temp file
     *         storage after contents are fetched.
     */
    boolean deletePostLoad();

    /**
     * Get temp file used by this input
     * 
     * @return - temp file
     */
    File getFile();

}
