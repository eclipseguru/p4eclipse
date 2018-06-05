/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.team.core.p4java.IP4PendingChangelist;

import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IPendingChangelistLocator {

    /**
     * Task locator token
     */
    public interface ITaskLocatorToken {

        /**
         * Get comment
         * 
         * @return comment
         */
        String getComment();

    }

    /**
     * Find a pending changelist in the specified array that matches the task.
     * The returned pending changelist will be used as the context changeset for
     * the task.
     * 
     * @param lists
     * @param task
     * @return pending changelist, new pending changelist will be created if
     *         null returned
     */
    IP4PendingChangelist find(IP4PendingChangelist[] lists, ITask task);

    /**
     * Generate token for task
     * 
     * @param task
     * @return task locator token
     */
    ITaskLocatorToken generateToken(ITask task);

    /**
     * Update changelist description for the specified task. This method may be
     * called from the UI-thread so it does not need to return before the update
     * completes. It can instance schedule an update to occur and return
     * immediately.
     * 
     * @param list
     * @param task
     * @param token
     * @param comment
     *            previous token
     * @return token
     */
    ITaskLocatorToken update(IP4PendingChangelist list, ITask task,
            ITaskLocatorToken token);

}
