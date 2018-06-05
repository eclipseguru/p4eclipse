/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4PendingChangelist extends IP4Changelist {

    /**
     * NEW
     */
    int NEW = -2;

    /**
     * SUBMIT_FAILED_MESSAGE
     */
    String SUBMIT_FAILED_MESSAGE = "Submit failed";

    /**
     * SUBMIT_ABORTED_MESSAGE
     */
    String SUBMIT_ABORTED_MESSAGE = "Submit aborted";

    /**
     * SUBMIT_FAILED_CHANGELIST
     */
    String SUBMIT_FAILED_CHANGELIST = "p4 submit -c"; //$NON-NLS-1$

    /**
     * Is this pending changelist part of the current user/client pairing
     * 
     * @return - true if this pending changelist is both associated with the
     *         user and client for the connection current being used
     */
    boolean isOnClient();

    /**
     * Submits part or all of the changelist
     * 
     * @param description
     * @param files
     * @param monitor
     * @return - submitted changelist id or -1 if submit failed
     */
    int submit(String description, IP4File[] files, IProgressMonitor monitor);

    /**
     * Submits the specified files and jobs in this changelist
     * 
     * @param description
     * @param files
     * @param jobs
     * @param monitor
     * @return - submitted changelist id or -1 if submit failed
     */
    int submit(String description, IP4File[] files, IP4Job[] jobs, IProgressMonitor monitor);

    /**
     * Submit the specified files and jobs in this changelist and optionally
     * re-opens the submitted files
     * 
     * @param reopen
     * @param description
     * @param files
     * @param jobs
     * @param monitor
     * @return - submitted changelist id or -1 if submit failed
     */
    int submit(boolean reopen, String description, IP4File[] files,
            IP4Job[] jobs, IProgressMonitor monitor);

    /**
     * Submit the specified files and jobs in this changelist and optionally
     * re-opens the submitted files and optionally changes the status of the
     * associated jobs
     * 
     * @param reopen
     * @param description
     * @param files
     * @param jobs
     * @param jobStatus
     * @param monitor
     * @return - id of submitted changelist or -1 if it failed
     */
    int submit(boolean reopen, String description, IP4File[] files,
            IP4Job[] jobs, String jobStatus, IProgressMonitor monitor);

    /**
     * Removes a file from this changelist's cache of open files. Does not
     * actually make a server call, that should be done via a specific P4 action
     * or {@link P4Collection}
     * 
     * @param file
     * @return - true if the file was removed, false otherwise
     */
    boolean removeFile(IP4File file);

    /**
     * Add a file to this changelist's cache of open files. Does not actually
     * make a server call, that should be done via a specific P4 action or
     * {@link P4Collection}
     * 
     * @param file
     */
    void addFile(IP4File file);

    /**
     * Reopens the specified resources in this changelist
     * 
     * @param resources
     */
    void reopen(IP4Resource[] resources);

    /**
     * Sets the description of this pending changelist via a call to change it
     * remotely on the server
     * 
     * @param description
     */
    void updateServerDescription(String description);

    /**
     * Is this pending changelist the active one being used by its parent
     * connection?
     * 
     * @return - true if current pending changelist, false otherwise
     */
    boolean isActive();

    /**
     * Make this pending changelist the active one being used by its parent
     * connection.
     */
    void makeActive();

    /**
     * Returns true if this changelist should be allowed to have
     * {@link #delete()} called on it.
     * 
     * Returns true if and only if all the following conditions are met:
     * 
     * {@link #needsRefresh()} is false
     * 
     * {@link #isDefault()} is false
     * 
     * {@link #isReadOnly()} is false
     * 
     * {@link #members()} array length is 0
     * 
     * @return - true if deleteable, false otherwise
     */
    boolean isDeleteable();

    /**
     * Returns true if the changelist is shelved
     * 
     * @return - true if shelved, false otherwise
     */
    boolean isShelved();

    /**
     * Delete the shelved files in this changelist
     */
    void deleteShelved();

    /**
     * Delete the specified resources on the shelved changelist
     * 
     * @param resources
     */
    void deleteShelve(IP4Resource[] resources);

    /**
     * Shelve the specified files
     * 
     * @param files
     */
    void shelve(IP4File[] files);

    /**
     * Update existing shelved files with versions in pending changelist
     */
    void updateShelvedFiles();

    /**
     * Update the specified resources on the shelved changelist
     * 
     * @param resources
     */
    void updateShelvedFiles(IP4Resource[] resources);

    /**
     * Replace shelved files with content of current pending changelist
     */
    void replaceShelvedFiles();

    /**
     * Get all members, shelved and pending
     * 
     * @return - non-null array of all members
     */
    IP4Resource[] getAllMembers();

    /**
     * Get shelved changes
     * 
     * @return - shelved changelist
     */
    IP4ShelvedChangelist getShelvedChanges();

    /**
     * Get the pending {@link IP4File} objects in this changelist
     * 
     * @return - non-null array of {@link IP4File} objects
     */
    IP4File[] getPendingFiles();

}
