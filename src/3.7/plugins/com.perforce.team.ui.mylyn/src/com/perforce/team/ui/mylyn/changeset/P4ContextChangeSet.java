/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.changeset;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.synchronize.IP4ReusableChangeSet;
import com.perforce.team.core.p4java.synchronize.P4PendingChangeSet;
import com.perforce.team.ui.mylyn.IPendingChangelistLocator;
import com.perforce.team.ui.mylyn.IPendingChangelistLocator.ITaskLocatorToken;
import com.perforce.team.ui.mylyn.P4JobConnectorUi;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;
import com.perforce.team.ui.p4java.actions.FixJobAction;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.internal.resources.ui.ResourcesUiBridgePlugin;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.team.ui.ContextChangeSet;
import org.eclipse.mylyn.internal.team.ui.LinkedTaskInfo;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.resources.ui.ResourcesUi;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.team.ui.AbstractTaskReference;
import org.eclipse.mylyn.team.ui.IContextChangeSet;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.provider.ThreeWayDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiff;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.osgi.service.prefs.Preferences;

/**
 * P4 context change set
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ContextChangeSet extends P4PendingChangeSet implements
        IAdaptable, IContextChangeSet, IP4ReusableChangeSet {

    /**
     * CTX_PERSISTANCE_TITLE
     */
    private static final String CTX_PERSISTANCE_TITLE = "title"; //$NON-NLS-1$

    /**
     * PERSITANCE_TITLE
     */
    private static final String PERSITANCE_TITLE = "{0} ({1})"; //$NON-NLS-1$

    /**
     * CONTEXT_PRIORITY
     */
    public static final int CONTEXT_PRIORITY = 3;

    private boolean suppressInterestContribution = false;
    private IP4Connection connection;
    private ITask task;
    private IPendingChangelistLocator locator;
    private ITaskLocatorToken token;

    /**
     * @param task
     * @param connection
     * @param manager
     */
    public P4ContextChangeSet(ITask task, IP4Connection connection,
            ActiveChangeSetManager manager) {
        super(manager, task.getSummary());
        this.task = task;
        this.connection = connection;
        initializeChangelist();
    }

    private void initializeChangelist() {
        P4JobConnectorUi connector = P4MylynUiUtils.getPerforceConnectorUi();
        this.locator = connector.getChangelistLocator(this.task);
        String comment = connector.getChangelistDescription(this.task);

        IP4PendingChangelist[] lists = null;
        if (this.connection.isPendingLoaded()) {
            lists = this.connection.getCachedPendingChangelists();
        } else {
            lists = this.connection.getPendingChangelists(false);
        }

        if (locator != null) {
            this.list = this.locator.find(lists, task);
        } else {
            this.list = findList(comment, lists);
        }

        if (list == null) {
            this.list = this.connection.createChangelist(comment, null);
        } else if (this.list.needsRefresh()) {
            this.list.refresh();
        }

        if (this.list != null) {
            this.id = this.list.getId();
        }
        fixJob();
        updateTitle();
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.P4PendingChangeSet#save(org.osgi.service.prefs.Preferences)
     */
    @Override
    public void save(Preferences prefs) {
        super.save(prefs);
        prefs.put(CTX_PERSISTANCE_TITLE, getTitleForPersistance());
        prefs.put(CTX_SAVED_COMMENT, getComment());
    }

    private String getTitleForPersistance() {
        return MessageFormat.format(PERSITANCE_TITLE, getTitle(),
                this.task.getHandleIdentifier());
    }

    private void fixJob() {
        // Only attempt to fix job if the task comes from a Perforce task
        // repository
    	/*
    	 * job060401: If non-perforce task id matches a job, link the job to the changelist.
    	 */
        //if (IP4MylynConstants.KIND.equals(this.task.getConnectorKind())) {
            if (this.list == null) {
                return;
            }
            P4Runner.schedule(new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    IP4Job job = connection.getJob(task.getTaskKey());
                    if (job != null) {
                        FixJobAction fix = new FixJobAction();
                        fix.setAsync(false);
                        fix.fix(list, job);
                    }
                }

                @Override
                public String getTitle() {
                    return MessageFormat.format(
                            Messages.P4ContextChangeSet_FixJobChangelist,
                            task.getTaskKey(), list.getId());
                }
            });
//        }
    }

    private IP4PendingChangelist findList(String comment,
            IP4PendingChangelist[] lists) {
        IP4PendingChangelist list = null;
        for (IP4PendingChangelist pending : lists) {
            if (comment.equals(pending.getDescription().trim())) {
                list = pending;
                break;
            }
        }
        return list;
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSet#getComment()
     */
    @Override
    public String getComment() {
        return getComment(true);
    }

    /**
     * @see org.eclipse.mylyn.team.ui.IContextChangeSet#getComment(boolean)
     */
    public String getComment(boolean checkTaskRepository) {
        return ContextChangeSet.getComment(checkTaskRepository, task,
                getResources());
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        Object adapted = null;
        if (AbstractTask.class == adapter) {
            adapted = getTask();
        } else if (AbstractTaskReference.class == adapter) {
            adapted = new LinkedTaskInfo(getTask(), this);
        } else {
            adapted = Platform.getAdapterManager().getAdapter(this, adapter);
        }
        return adapted;
    }

    /**
     * @see org.eclipse.mylyn.team.ui.IContextChangeSet#getTask()
     */
    public ITask getTask() {
        return this.task;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.P4PendingChangeSet#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof P4ContextChangeSet) {
            return this.task.equals(((P4ContextChangeSet) object).getTask())
                    && super.equals(object);
        }
        return super.equals(object);
    }

    @Override
    public int hashCode() {
    	if(this.task!=null)
    		return this.task.hashCode();
    	return super.hashCode();    				
    }
    
    /**
     * @see com.perforce.team.core.p4java.synchronize.P4PendingChangeSet#updateTitle()
     */
    @Override
    protected void updateTitle() {
        setTitle(this.task.getSummary());
    }

    /**
     * @see org.eclipse.mylyn.team.ui.IContextChangeSet#updateLabel()
     */
    public void updateLabel() {
        updateTitle();
        if (isValid() && this.locator != null) {
            this.token = this.locator.update(list, this.task, this.token);
        }
    }

    private IResource getResourceFromDiff(IDiff diff) {
        if (diff instanceof ResourceDiff) {
            return ((ResourceDiff) diff).getResource();
        } else if (diff instanceof ThreeWayDiff) {
            ThreeWayDiff threeWayDiff = (ThreeWayDiff) diff;
            return ResourcesPlugin.getWorkspace().getRoot()
                    .findMember(threeWayDiff.getPath());
        } else {
            return null;
        }
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.DiffChangeSet#add(org.eclipse.team.core.diff.IDiff)
     */
    @Override
    public void add(IDiff diff) {
        super.add(diff);
        IResource resource = getResourceFromDiff(diff);
        if (!suppressInterestContribution && resource != null) {
            Set<IResource> resources = new HashSet<IResource>();
            resources.add(resource);
            if (ResourcesUiBridgePlugin.getDefault() != null) {
                ResourcesUi.addResourceToContext(resources,
                        InteractionEvent.Kind.SELECTION);
            }
        }
    }

    /**
     * @see org.eclipse.mylyn.team.ui.IContextChangeSet#restoreResources(org.eclipse.core.resources.IResource[])
     */
    public void restoreResources(IResource[] newResources) throws CoreException {
        suppressInterestContribution = true;
        try {
            add(newResources);
            setComment(getComment(false));
        } catch (TeamException e) {
            throw e;
        } finally {
            suppressInterestContribution = false;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.P4PendingChangeSet#getPriority()
     */
    @Override
    public int getPriority() {
        return CONTEXT_PRIORITY;
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.P4PendingChangeSet#useCommentOnSubmit()
     */
    @Override
    public boolean useCommentOnSubmit() {
        return true;
    }

    private boolean recreateChangelists() {
        return PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.RECREATE_CHANGELISTS);
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.IP4ReusableChangeSet#activate(com.perforce.team.core.p4java.IP4File[])
     */
    public boolean activate(IP4File[] files) {
        if (!this.task.isActive() || !recreateChangelists()) {
            return false;
        }
        this.list = null;
        initializeChangelist();
        IP4PendingChangelist newList = this.list;
        if (newList != null) {
            new P4Collection(files).reopen(newList);
        }
        return newList != null;
    }

}
