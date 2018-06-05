/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.synchronize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.subscribers.ISubscriberChangeEvent;
import org.eclipse.team.core.subscribers.ISubscriberChangeListener;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ChangeSet;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Workspace;

/**
 * P4 change set manager
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class P4ChangeSetManager extends SubscriberChangeSetManager {

    private static P4ChangeSetManager manager;

    /**
     * Get singleton p4 change set manager
     * 
     * @return - p4 change set manager instance
     */
    public static synchronized P4ChangeSetManager getChangeSetManager() {
        if (manager == null) {
            manager = new P4ChangeSetManager(PerforceSubscriber.getSubscriber());
        }
        return manager;
    }

    private ISubscriberChangeListener listener = new ISubscriberChangeListener() {

        public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas) {
            Map<P4PendingChangeSet, List<IResource>> setAdds = null;
            for (ISubscriberChangeEvent event : deltas) {
                if ((ISubscriberChangeEvent.SYNC_CHANGED & event.getFlags()) != 0) {
                    IResource resource = event.getResource();
                    if (resource != null) {
                        try {
                            SyncInfo info = event.getSubscriber().getSyncInfo(
                                    resource);
                            P4PendingChangeSet set = getChangeSet(info,
                                    resource);
                            if (set != null && !set.contains(resource)) {
                                if (setAdds == null) {
                                    setAdds = new HashMap<P4PendingChangeSet, List<IResource>>();
                                }
                                List<IResource> resources = setAdds.get(set);
                                if (resources == null) {
                                    resources = new ArrayList<IResource>();
                                    setAdds.put(set, resources);
                                }
                                resources.add(resource);
                            }
                        } catch (TeamException e) {
                            PerforceProviderPlugin.logError(e);
                        }
                    }
                }
            }
            if (setAdds != null) {
                for (Map.Entry<P4PendingChangeSet, List<IResource>> entry: setAdds.entrySet()) {
                	P4PendingChangeSet changeSet = entry.getKey();
                    List<IResource> resources = entry.getValue();
                    try {
                        changeSet.add(resources.toArray(new IResource[resources
                                .size()]));
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            }
        }
    };

    private IP4Listener p4Listener = new IP4Listener() {

        public void resoureChanged(P4Event event) {
            EventType type = event.getType();
            switch (type) {
            case REFRESHED:
            case CHANGED:
            case CREATE_CHANGELIST:
                handleChangeEvent(event);
                break;
            case DELETE_CHANGELIST:
            case SUBMIT_CHANGELIST:
                handleDeleteEvent(event);
                break;
            case ACTIVE_CHANGELIST:
                handleActiveEvent(event);
                break;
            case INACTIVE_CHANGELIST:
                handleInactiveEvent(event);
                break;
            case OPENED:
                handleReload(event);
                break;
            default:
                break;
            }
        }

		public String getName() {
			return P4ChangeSetManager.this.getClass().getSimpleName();
		}
    };

    /**
     * @param subscriber
     */
    private P4ChangeSetManager(Subscriber subscriber) {
        super(subscriber);
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.SubscriberChangeSetManager#initializeSets()
     */
    @Override
    protected void initializeSets() {
        super.initializeSets();
        getSubscriber().addListener(listener);
        P4Workspace.getWorkspace().addListener(p4Listener);
    }

    private P4PendingChangeSet findListChangeSet(IP4PendingChangelist list) {
        if (list == null) {
            return null;
        }
        return findListChangeSet(new IP4PendingChangelist[] { list });
    }

    private P4PendingChangeSet findListChangeSet(IP4PendingChangelist[] lists) {
        P4PendingChangeSet pendingSet = null;
        for (IP4PendingChangelist list : lists) {
            for (ChangeSet set : getSets()) {
                pendingSet = (P4PendingChangeSet) set;
                if (list.equals(pendingSet.getChangelist())) {
                    return pendingSet;
                }
            }
        }
        return null;
    }

    private P4PendingChangeSet findEventChangeSet(P4Event event) {
        return findListChangeSet(event.getPending());
    }

    private void handleChangeEvent(P4Event event) {
        P4PendingChangeSet set = findEventChangeSet(event);
        if (set != null) {
            set.refresh();
        }
    }

    private void handleDeleteEvent(P4Event event) {
        P4PendingChangeSet set = findEventChangeSet(event);
        if (set != null) {
            set.setValid(false);
            remove(set);
        }
    }

    private void handleActiveEvent(P4Event event) {
        P4PendingChangeSet set = findEventChangeSet(event);
        if (set != null) {
            syncDefaultList(set, false);
        }
    }

    private void handleInactiveEvent(P4Event event) {
        IP4Connection[] connections = event.getConnections();
        P4PendingChangeSet defaultSet = (P4PendingChangeSet) getDefaultSet();
        if (defaultSet != null && defaultSet.getConnection() != null) {
            for (IP4Connection connection : connections) {
                if (connection.equals(defaultSet.getConnection())) {
                    syncDefaultList(null, false);
                    break;
                }
            }
        }
    }

    /**
     * @param event
     */
    private void handleReload(P4Event event) {
        P4PendingChangeSet defaultSet = (P4PendingChangeSet) getDefaultSet();
        // Only reload change sets that are not currently valid
        if (defaultSet == null || defaultSet.isValid()) {
            return;
        }
        if (defaultSet instanceof IP4ReusableChangeSet) {
            if (((IP4ReusableChangeSet) defaultSet).activate(event.getFiles())) {
                defaultSet.setValid(true);
                this.add(defaultSet);
                makeDefault(defaultSet);
            }
        }
    }

    /**
     * Load the pending change sets for the specified p4 resource's connection
     * 
     * @param file
     * @return - p4 change set
     */
    public P4PendingChangeSet getPendingChangeSet(IP4File file) {
        P4PendingChangeSet changeSet = null;
        if (file != null && file.isOpened()) {
            IP4PendingChangelist list = file.getChangelist(true);
            if (list != null) {
                changeSet = findListChangeSet(list);
                if (changeSet == null) {
                    changeSet = new P4PendingChangeSet(this, list);
                    add(changeSet);
                }
            }
        }
        return changeSet;
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager#add(org.eclipse.team.internal.core.subscribers.ChangeSet)
     */
    @Override
    public void add(ChangeSet set) {
        if (set instanceof P4PendingChangeSet) {
            P4PendingChangeSet p4Set = (P4PendingChangeSet) set;
            if (p4Set.isValid() && p4Set.getConnection() != null
                    && p4Set.getChangelist() != null) {
                if (!contains(set)) {
                    super.add(set);
                } else {
                    replaceList(p4Set);
                }
            }
        }
    }

    private void replaceList(P4PendingChangeSet set) {
        if (this.contains(set)) {
            for (ChangeSet current : getSets()) {
                if (set.equals(current)) {
                    if (set.getPriority() > ((P4PendingChangeSet) current)
                            .getPriority()) {
                        remove(current);
                        try {
                            set.add(current.getResources());
                        } catch (CoreException e) {
                            PerforceProviderPlugin.logError(e);
                        }
                        add(set);
                        break;
                    }
                }
            }
        }
    }

    private void syncDefaultList(ActiveChangeSet set, boolean makeActive) {
        if (set instanceof P4PendingChangeSet) {
            replaceList((P4PendingChangeSet) set);
            super.makeDefault(set);
            if (makeActive) {
                IP4PendingChangelist list = ((P4PendingChangeSet) set)
                        .getChangelist();
                if (list != null) {
                    list.makeActive();
                }
            }
        } else {
            ActiveChangeSet current = getDefaultSet();
            super.makeDefault(null);
            if (current != null && makeActive) {
                IP4PendingChangelist list = ((P4PendingChangeSet) current)
                        .getChangelist();
                if (list != null && list.isActive()) {
                    list.getConnection().setActivePendingChangelist(-1);
                }
            }
        }
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager#makeDefault(org.eclipse.team.internal.core.subscribers.ActiveChangeSet)
     */
    @Override
    public void makeDefault(ActiveChangeSet set) {
        syncDefaultList(set, true);
    }

    /**
     * @see com.perforce.team.core.p4java.synchronize.SubscriberChangeSetManager#updateChangeSet(org.eclipse.core.resources.IResource)
     */
    @Override
    protected void updateChangeSet(IResource resource) {
        if (resource != null) {
            try {
                updateChangeSet(getSubscriber().getSyncInfo(resource), resource);
            } catch (TeamException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    private boolean isValidInfo(SyncInfo info) {
        return info instanceof PerforceSyncInfo
                && (info.getKind() & SyncInfo.OUTGOING) != 0;
    }

    /**
     * Get changeset for info and resource
     * 
     * @param info
     * @param resource
     * @return - changeset
     */
    protected P4PendingChangeSet getChangeSet(SyncInfo info, IResource resource) {
        if (isValidInfo(info)) {
            return getPendingChangeSet(((PerforceSyncInfo) info).getP4File());
        } else {
            return null;
        }
    }

    /**
     * Update changeset for sync info and specified resource
     * 
     * @param info
     * @param resource
     */
    protected void updateChangeSet(SyncInfo info, IResource resource) {
        if (isValidInfo(info)) {
            IP4File file = ((PerforceSyncInfo) info).getP4File();
            updateChangeSet(file, resource);
        }
    }

    /**
     * Update change set with specified p4 file and iresource
     * 
     * @param file
     * @param resource
     */
    protected void updateChangeSet(IP4File file, IResource resource) {
        P4PendingChangeSet changeSet = getPendingChangeSet(file);
        if (changeSet != null && !changeSet.contains(resource)) {
            try {
                changeSet.add(new IResource[] { resource });
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.SubscriberChangeSetManager#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        getSubscriber().removeListener(listener);
        P4Workspace.getWorkspace().removeListener(p4Listener);
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager#doCreateSet(java.lang.String)
     */
    @Override
    protected ActiveChangeSet doCreateSet(String name) {
        return new P4PendingChangeSet(this, name);
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager#handleAddedResources(org.eclipse.team.internal.core.subscribers.ChangeSet,
     *      org.eclipse.team.core.diff.IDiff[])
     */
    @Override
    protected void handleAddedResources(ChangeSet set, IDiff[] diffs) {
        if (set != null) {
            super.handleAddedResources(set, diffs);
        }
    }

}
