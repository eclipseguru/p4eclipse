/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.IFileSpec;

import java.io.InputStream;
import java.util.Date;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IP4ShelveFile extends IP4Resource {

    /**
     * Get changelist id
     * 
     * @return - pending changelist id with shelved file
     */
    int getId();

    /**
     * Get shelved changelist
     * 
     * @return - shelved changelist
     */
    IP4ShelvedChangelist getChangelist();

    /**
     * Get revision specifier
     * 
     * @return - revision specifier
     */
    String getRevision();

    /**
     * Get date of changelist
     * 
     * @return - date
     */
    Date getDate();

    /**
     * Get user of changelist
     * 
     * @return - user
     */
    String getUser();

    /**
     * Get workspace of changelist
     * 
     * @return - workspace
     */
    String getWorkspace();

    /**
     * Get description of changelist
     * 
     * @return - description
     */
    String getDescription();

    /**
     * Get file
     * 
     * @return - file in shelved changelist
     */
    IP4File getFile();

    /**
     * Get remote contents of shelve file
     * 
     * @return - input stream
     */
    InputStream getRemoteContents();

    /**
     * Unshelve this file
     * 
     * @return - non-null array of file specs
     */
    IFileSpec[] unshelve();

    /**
     * Unshelve this file to the specified changelist
     * 
     * @param toChangelist
     * @return - non-null array of file specs
     */
    IFileSpec[] unshelve(int toChangelist);

    /**
     * Unshelve this file to the specified changelist and optionally overwrite
     * if it is already writable
     * 
     * @param toChangelist
     * @param overwrite
     * @return - non-null array of file specs
     */
    IFileSpec[] unshelve(int toChangelist, boolean overwrite);

}
