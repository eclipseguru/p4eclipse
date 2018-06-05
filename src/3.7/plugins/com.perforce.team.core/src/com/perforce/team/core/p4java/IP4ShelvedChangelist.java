/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.IFileSpec;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IP4ShelvedChangelist extends IP4Changelist {

    /**
     * Unshelve the entire shelved changelist
     * 
     * @return - non-null array of file specs
     */
    IFileSpec[] unshelve();

    /**
     * Unshelve the entire shelved changelist
     * 
     * @param toChangelist
     *            - changelist to unshelve into
     * @return - non-null array of file specs
     */
    IFileSpec[] unshelve(int toChangelist);

    /**
     * Unshelve the specified files from this shelved changelist
     * 
     * @param files
     * @return - non-null array of file specs
     */
    IFileSpec[] unshelve(IP4Resource[] files);

    /**
     * Unshelve the specified files from this shelved changelist
     * 
     * @param files
     * @param toChangelist
     *            - changelist to unshelve into
     * @return - non-null array of file specs
     */
    IFileSpec[] unshelve(IP4Resource[] files, int toChangelist);

    /**
     * Unshelve the specified files from this shelved changelist
     * 
     * @param files
     * @param toChangelist
     *            - changelist to unshelve into
     * @param overwrite
     * @return - non-null array of file specs
     */
    IFileSpec[] unshelve(IP4Resource[] files, int toChangelist,
            boolean overwrite);

}
