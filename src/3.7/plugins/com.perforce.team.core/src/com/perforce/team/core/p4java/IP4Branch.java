/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.exception.P4JavaException;

import java.util.Date;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IP4Branch extends IP4Resource {

    /**
     * Template branch name
     */
    String TEMPLATE_NAME = "x_new_spec_name_x"; //$NON-NLS-1$

    /**
     * Get owner of this branch
     * 
     * @return - branch owner
     */
    String getOwner();

    /**
     * Get last access time of this branch
     * 
     * @return - access time
     */
    Date getAccessTime();

    /**
     * Get last update time of this branch
     * 
     * @return - update time
     */
    Date getUpdateTime();

    /**
     * Get the description of this branch
     * 
     * @return - branch description
     */
    String getDescription();

    /**
     * Is this branch locked?
     * 
     * @return - true if locked, false otherwise
     */
    boolean isLocked();

    /**
     * Get the view mapping of this branch
     * 
     * @return - branch view mapping
     */
    ViewMap<IBranchMapping> getView();

    /**
     * Update a branch with the specified spec
     * 
     * @param branch
     * @throws P4JavaException
     */
    void update(IBranchSpec branch) throws P4JavaException;

    /**
     * Get submitted changelists reported from running an interchanges using
     * this branch
     * 
     * @return - non-null but possible empty array of p4 submitted changelists
     */
    IP4SubmittedChangelist[] getInterchanges();

    /**
     * Get submitted changelists reported from running an interchanges using
     * this branch
     * 
     * @param reverse
     *            - true to reverse interchange command
     * @return - non-null but possible empty array of p4 submitted changelists
     */
    IP4SubmittedChangelist[] getInterchanges(boolean reverse);

    /**
     * Get diffs between the source and target paths in this branch using source
     * and target filters specified
     * 
     * @param sourceFilter
     * @param rightFilter
     * @return non-null but possibly empty array of diffs
     */
    IFileDiff[] getDiffs(String sourceFilter, String rightFilter);

    /**
     * Get diffs between the source and target paths in this branch
     * 
     * @return non-null but possibly empty array of diffs
     */
    IFileDiff[] getDiffs();

}
