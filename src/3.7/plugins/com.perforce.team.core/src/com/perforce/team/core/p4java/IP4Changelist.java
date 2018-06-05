/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.util.Date;
import java.util.List;

import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IExtendedFileSpec;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Changelist extends IP4Container, Comparable<IP4Changelist> {

    /**
     * Sets the description for this changelist
     * 
     * @param description
     */
    void setDescription(String description);

    /**
     * Gets the description
     * 
     * @return - changelist description
     */
    String getDescription();

    /**
     * Get shortened description
     * 
     * @param length
     * @return - shortened description
     */
    String getShortenedDescription(int length);

    /**
     * Gets a short description synopsis for this changelist
     * 
     * @return - short description ending in ... if there is more
     */
    String getShortDescription();

    /**
     * Gets the client name this changelist is for
     * 
     * @return - client name
     */
    String getClientName();

    /**
     * Gets the user name this changelist is for
     * 
     * @return - user name
     */
    String getUserName();

    /**
     * Is this changelist a user's default changelist
     * 
     * @return - true if a default changelist, false otherwise
     */
    boolean isDefault();

    /**
     * Gets the changelist id
     * 
     * @return - changelist id
     */
    int getId();

    /**
     * Gets the date of the changelist
     * 
     * @return - changelist date
     */
    Date getDate();

    /**
     * Gets the changelist status
     * 
     * @return - changelist status
     */
    ChangelistStatus getStatus();

    /**
     * Gets the underlying p4 java changelist
     * 
     * @return - p4 changelist
     */
    IChangelist getChangelist();

    /**
     * Get the files that are currently associated with this changelist
     * 
     * @return - array of files associated with this changelist
     */
    IP4Resource[] getFiles();

    /**
     * Get the jobs that are currently in this changelist
     * 
     * @return - array of jobs associated with this changelist
     */
    IP4Job[] getJobs();

    /**
     * Fixes a job
     * 
     * @param job
     * @return - the fixed job or null if fixing failed
     */
    IP4Job fix(IP4Job job);

    /**
     * Fixes an array of jobs
     * 
     * @param jobs
     * @return - array of jobs that were actually fixed
     */
    IP4Job[] fix(IP4Job[] jobs);

    /**
     * Unfixes a job
     * 
     * @param job
     * @return - the unfixed job or null if unfixing failed
     */
    IP4Job unfix(IP4Job job);

    /**
     * Unfixes an array of jobs
     * 
     * @param jobs
     * @return - array of jobs that were actually removed as fixed by this
     *         changelist
     */
    IP4Job[] unfix(IP4Job[] jobs);

    /**
     * Add a job to this changelist's cache of fixed jobs. Does not actually
     * make a server call, that should be done via a specific P4 action or
     * {@link P4Collection}
     * 
     * @param job
     */
    void addJob(IP4Job job);

    /**
     * Removes a job from this changelist's cache of fixed jobs. Does not
     * actually make a server call, that should be done via a specific P4 action
     * or {@link P4Collection}
     * 
     * @param job
     */
    void removeJob(IP4Job job);

    /**
     * Get the job ids associated with this changelist by contacting the server
     * directly. This method will not return any cached data or update the
     * internal model.
     * 
     * @return - non-null but possibly empty array of job ids
     */
    String[] getJobIds();

    /**
     * @return the specs of opened files in the changelist
     */
    List<IExtendedFileSpec> getOpenedSpecs();
}
