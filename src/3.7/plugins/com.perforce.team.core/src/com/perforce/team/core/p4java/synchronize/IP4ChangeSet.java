/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.synchronize;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IP4ChangeSet {

    /**
     * Get connection of change set
     * 
     * @return - p4 connection
     */
    IP4Connection getConnection();

    /**
     * Get comment of change set
     * 
     * @return - comment
     */
    String getComment();

    /**
     * Get underlying changelist
     * 
     * @return - p4 changelist
     */
    IP4Changelist getChangelist();

    /**
     * Get change set name
     * 
     * @return - name
     */
    String getName();

    /**
     * Is the changeset valid?
     * 
     * @return - true if valid, false if invalid
     */
    boolean isValid();

    /**
     * Set the changeset as valid or invalid
     * 
     * @param valid
     */
    void setValid(boolean valid);

    /**
     * Get changeset id
     * 
     * @return - changelist id
     */
    int getId();

    /**
     * Used to differentiate changesets when they are equal but the higher
     * priority changeset should be managed.
     * 
     * @return - priority
     */
    int getPriority();

}
