/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.ui.P4ConnectionManager;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.subscribers.SubscriberChangeEvent;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class PerforceSyncModelOperation extends
        SynchronizeModelOperation {

    private boolean async = true;

    /**
     * @param configuration
     * @param elements
     */
    public PerforceSyncModelOperation(
            ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
        super(configuration, elements);
    }

    /**
     * Create correct location, should be called instead of {@link P4Collection}
     * constructor.
     * 
     * @return - empty collection
     */
    protected P4Collection createCollection() {
        return P4ConnectionManager.getManager().createP4Collection();
    }

    /**
     * Updates the sync state for the resource specified
     * 
     * @param resource
     */
    protected void updateSyncState(IResource resource) {
        PerforceSubscriber.getSubscriber().fireTeamResourceChange(
                SubscriberChangeEvent.asSyncChangedDeltas(
                        PerforceSubscriber.getSubscriber(),
                        new IResource[] { resource }));
    }

    /**
     * @see org.eclipse.team.ui.TeamOperation#canRunAsJob()
     */
    @Override
    protected boolean canRunAsJob() {
        return async;
    }

    /**
     * @return the async
     */
    public boolean isAsync() {
        return this.async;
    }

    /**
     * @param async
     *            the async to set
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

}
