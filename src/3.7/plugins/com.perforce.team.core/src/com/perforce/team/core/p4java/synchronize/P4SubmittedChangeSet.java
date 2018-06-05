/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.synchronize;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;

import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.team.internal.core.subscribers.CheckedInChangeSet;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4SubmittedChangeSet extends CheckedInChangeSet implements
        IP4ChangeSet {

    /**
     * SUBMITTED_PRIORITY
     */
    public static final int SUBMITTED_PRIORITY = 1;

    /**
     * Valid flag
     */
    protected boolean valid = true;

    /**
     * P4 submitted changelist
     */
    protected IP4SubmittedChangelist changelist;

    /**
     * 
     * @param list
     */
    public P4SubmittedChangeSet(IP4SubmittedChangelist list) {
        this.changelist = list;
        setName(MessageFormat.format(Messages.SubmittedChange,
                this.changelist.getId()));
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#getId()
     */
    public int getId() {
        return this.changelist.getId();
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.CheckedInChangeSet#getAuthor()
     */
    @Override
    public String getAuthor() {
        return this.changelist.getUserName();
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.CheckedInChangeSet#getDate()
     */
    @Override
    public Date getDate() {
        return this.changelist.getDate();
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ChangeSet#getComment()
     */
    @Override
    public String getComment() {
        return this.changelist.getDescription();
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#getConnection()
     */
    public IP4Connection getConnection() {
        return this.changelist.getConnection();
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#getChangelist()
     */
    public IP4Changelist getChangelist() {
        return this.changelist;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#isValid()
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#setValid(boolean)
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ChangeSet#getPriority()
     */
    public int getPriority() {
        return SUBMITTED_PRIORITY;
    }

}
