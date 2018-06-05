/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4DefaultPendingChangelistLocator implements
        IPendingChangelistLocator {

    /**
     * Pending changelist locator token
     */
    protected class PendingToken implements ITaskLocatorToken {

        private String id;
        private String summary;

        /**
         * Create pending token from task
         * 
         * @param task
         */
        public PendingToken(ITask task) {
            this.id = getTaskIdentifier(task);
            this.summary = task.getSummary();
            if (this.summary != null) {
                this.summary = this.summary.trim();
            }
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof PendingToken) {
                PendingToken other = (PendingToken) obj;
                if (id != null && id.equals(other.id)) {
                    if (summary == null) {
                        return other.summary == null;
                    } else {
                        return summary.equals(other.summary);
                    }
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
        	// no need summary here
        	if(id!=null)
        		return id.hashCode();
        	return super.hashCode();
        }
        
        /**
         * @see com.perforce.team.ui.mylyn.IPendingChangelistLocator.ITaskLocatorToken#getComment()
         */
        public String getComment() {
            StringBuilder comment = new StringBuilder(id);
            if (summary != null) {
                comment.append(" : "); //$NON-NLS-1$
                comment.append(summary);
            }
            return comment.toString();
        }

    }

    private ISchedulingRule updateRule = P4Runner.createRule();

    /**
     * Get legacy comment
     * 
     * @param task
     * @return non-null legacy comment message
     */
    public String getLegacyComment(ITask task) {
        String key = ""; //$NON-NLS-1$
        if (task != null) {
            key = task.getTaskKey();
        }
        return MessageFormat.format(
                P4DefaultJobUiConfiguration.COMMENT_PATTERN, key);
    }

    private String getTaskIdentifier(ITask task) {
        String id = task.getTaskKey();
        if (id == null) {
            id = task.getTaskId();
        }
        if (id != null) {
            id = id.trim();
        }
        return id;
    }

    /**
     * @see com.perforce.team.ui.mylyn.IPendingChangelistLocator#find(com.perforce.team.core.p4java.IP4PendingChangelist[],
     *      org.eclipse.mylyn.tasks.core.ITask)
     */
    public IP4PendingChangelist find(IP4PendingChangelist[] lists, ITask task) {
        if (lists == null || lists.length == 0 || task == null) {
            return null;
        }
        IP4PendingChangelist list = null;
        String legacyComment = getLegacyComment(task);
        String id = getTaskIdentifier(task);
        for (IP4PendingChangelist pending : lists) {
            String description = pending.getDescription().trim();
            if (legacyComment.equals(description)) {
                list = pending;
                break;
            }
            if (!StringUtils.isEmpty(id) && description.startsWith(id)) {
                list = pending;
                break;
            }
        }
        return list;
    }

    /**
     * @see com.perforce.team.ui.mylyn.IPendingChangelistLocator#update(com.perforce.team.core.p4java.IP4PendingChangelist,
     *      org.eclipse.mylyn.tasks.core.ITask,
     *      com.perforce.team.ui.mylyn.IPendingChangelistLocator.ITaskLocatorToken)
     */
    public ITaskLocatorToken update(final IP4PendingChangelist list,
            final ITask task, ITaskLocatorToken token) {
        if (list == null || task == null) {
            return null;
        }
        final ITaskLocatorToken newToken = generateToken(task);
        if (newToken.equals(token)) {
            return token;
        }
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return MessageFormat
                        .format(Messages.P4DefaultPendingChangelistLocator_UpdatingChangelistDescription,
                                Integer.toString(list.getId()));
            }

            @Override
            public void run(IProgressMonitor monitor) {
            	/*
            	 * job059713: use the Mylyn commit comment template for the changelist description
            	 */
            	list.updateServerDescription(P4DefaultJobUiConfiguration.getCommitComment(task,newToken));
            }

        };
        P4Runner.schedule(runnable, updateRule);
        return newToken;
    }

    /**
     * @see com.perforce.team.ui.mylyn.IPendingChangelistLocator#generateToken(org.eclipse.mylyn.tasks.core.ITask)
     */
    public ITaskLocatorToken generateToken(ITask task) {
        return task != null ? new PendingToken(task) : null;
    }

}
