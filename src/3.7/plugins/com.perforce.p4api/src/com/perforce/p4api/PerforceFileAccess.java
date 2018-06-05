package com.perforce.p4api;

/*
 * Copyright (c) 20036 Perforce Software.  All rights reserved.
 *
 */

/**
 * Handle files for specific connection
 */
public class PerforceFileAccess {

    /**
     * Get the filename from a path
     * 
     * @param path
     *            the full name
     * @return just the filename part.
     */
    public static String getFilename(String path) {
        int idx;
        if (path.indexOf('/') != -1) {
            idx = path.lastIndexOf('/');
        } else {
            idx = path.lastIndexOf('\\');
        }
        return path.substring(idx + 1);
    }

    /**
     * Get the full path to parent folder from a path
     * 
     * @param path
     * @return - folder path part
     */
    public static String getFolder(String path) {
        int idx;
        if (path.indexOf('/') != -1) {
            idx = path.lastIndexOf('/');
        } else {
            idx = path.lastIndexOf('\\');
        }
        return path.substring(0, idx);
    }

}
