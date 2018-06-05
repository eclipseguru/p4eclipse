package com.perforce.team.core.p4java.synchronize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.core.BackgroundEventHandler;
import org.eclipse.team.internal.core.Messages;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector;
import org.osgi.service.prefs.Preferences;

/**
 * Modeled closely after
 * {@link org.eclipse.team.internal.core.subscribers.SubscriberChangeSetManager}
 * 
 * but with changes to event handling of changed resources
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubscriberChangeSetManager extends ActiveChangeSetManager {

    /**
     * PREF_CHANGE_SETS
     */
    public static final String PREF_CHANGE_SETS = "changeSets"; //$NON-NLS-1$

    /**
     * RESOURCE_REMOVAL
     */
    public static final int RESOURCE_REMOVAL = 1;

    /**
     * RESOURCE_CHANGE
     */
    public static final int RESOURCE_CHANGE = 2;

    private EventHandler handler;
    private ResourceCollector collector;

    /**
     * Background event handler for serializing and batching change set changes
     */
    private class EventHandler extends BackgroundEventHandler {

        private List<Event> dispatchEvents = new ArrayList<Event>();

        protected EventHandler(String jobName, String errorTitle) {
            super(jobName, errorTitle);
        }

        /**
         * @see org.eclipse.team.internal.core.BackgroundEventHandler#processEvent
         *      (org.eclipse.team.internal.core.BackgroundEventHandler.Event,
         *      org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        protected void processEvent(Event event, IProgressMonitor monitor)
                throws CoreException {
            // Handle everything in the dispatch
            if (isShutdown()) {
                throw new OperationCanceledException();
            }
            dispatchEvents.add(event);
        }

        /**
         * @see org.eclipse.team.internal.core.BackgroundEventHandler#doDispatchEvents
         *      (org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        protected boolean doDispatchEvents(IProgressMonitor monitor)
                throws TeamException {
            if (dispatchEvents.isEmpty()) {
                return false;
            }
            if (isShutdown()) {
                throw new OperationCanceledException();
            }
            ResourceDiffTree[] locked = null;
            try {
                locked = beginDispath();
                for (Event event : dispatchEvents) {
                    switch (event.getType()) {
                    case RESOURCE_REMOVAL:
                        handleRemove(event.getResource());
                        break;
                    case RESOURCE_CHANGE:
                        handleChange(event.getResource(),
                                ((ResourceEvent) event).getDepth());
                        break;
                    default:
                        break;
                    }
                    if (isShutdown())
                        throw new OperationCanceledException();
                }
            } catch (CoreException e) {
                throw TeamException.asTeamException(e);
            } finally {
                try {
                    endDispatch(locked, monitor);
                } finally {
                    dispatchEvents.clear();
                }
            }
            return true;
        }

        /**
         * Begin input on all the sets and return the sync sets that were
         * locked. If this method throws an exception then the client can assume
         * that no sets were locked
         */
        private ResourceDiffTree[] beginDispath() {
            ChangeSet[] sets = getSets();
            List<ResourceDiffTree> lockedSets = new ArrayList<ResourceDiffTree>();
            try {
                for (int i = 0; i < sets.length; i++) {
                    ActiveChangeSet set = (ActiveChangeSet) sets[i];
                    IResourceDiffTree diffTree = set.getDiffTree();
                    if (diffTree instanceof ResourceDiffTree) {
                        ResourceDiffTree tree = (ResourceDiffTree) diffTree;
                        lockedSets.add(tree);
                        tree.beginInput();
                    }
                }
                return lockedSets.toArray(new ResourceDiffTree[lockedSets
                        .size()]);
            } catch (RuntimeException e) {
                try {
                    for (ResourceDiffTree tree : lockedSets) {
                        try {
                            tree.endInput(null);
                        } catch (Throwable e1) {
                            // Ignore so that original exception is not masked
                        }
                    }
                } catch (Throwable e1) {
                    // Ignore so that original exception is not masked
                }
                throw e;
            }
        }

        private void endDispatch(ResourceDiffTree[] locked,
                IProgressMonitor monitor) {
            if (locked == null) {
                // The begin failed so there's nothing to unlock
                return;
            }
            monitor.beginTask(null, 100 * locked.length);
            for (int i = 0; i < locked.length; i++) {
                ResourceDiffTree tree = locked[i];
                try {
                    tree.endInput(Policy.subMonitorFor(monitor, 100));
                } catch (RuntimeException e) {
                    // Don't worry about ending every set if an error occurs.
                    // Instead, log the error and suggest a restart.
                    TeamPlugin.log(IStatus.ERROR,
                            Messages.SubscriberChangeSetCollector_0, e);
                    throw e;
                }
            }
            monitor.done();
        }

        /**
         * @see org.eclipse.team.internal.core.BackgroundEventHandler#queueEvent(org.eclipse.team.internal.core.BackgroundEventHandler.Event,
         *      boolean)
         */
        @Override
        protected synchronized void queueEvent(Event event, boolean front) {
            // Override to allow access from enclosing class
            super.queueEvent(event, front);
        }

        /**
         * Handle the removal
         */
        private void handleRemove(IResource resource) {
            ChangeSet[] sets = getSets();
            for (int i = 0; i < sets.length; i++) {
                ChangeSet set = sets[i];
                // This will remove any descendants from the set and callback to
                // resourcesChanged which will batch changes
                if (!set.isEmpty()) {
                    set.rootRemoved(resource, IResource.DEPTH_INFINITE);
                    if (set.isEmpty()) {
                        remove(set);
                    }
                }
            }
        }

        /**
         * Handle the change
         */
        private void handleChange(IResource resource, int depth)
                throws CoreException {
            IDiff diff = getDiff(resource);
            if (isModified(diff)) {
                ActiveChangeSet[] containingSets = getContainingSets(resource);
                if (containingSets.length == 0) {
                    updateChangeSet(resource);
                } else {
                    for (int i = 0; i < containingSets.length; i++) {
                        ActiveChangeSet set = containingSets[i];
                        // Update the sync info in the set
                        set.add(diff);
                    }
                }
            } else {
                removeFromAllSets(resource);
            }
            if (depth != IResource.DEPTH_ZERO) {
                IResource[] members = getSubscriber().members(resource);
                for (IResource member : members) {
                    handleChange(member, depth == IResource.DEPTH_ONE
                            ? IResource.DEPTH_ZERO
                            : IResource.DEPTH_INFINITE);
                }
            }
        }

        private void removeFromAllSets(IResource resource) {
            List<ActiveChangeSet> toRemove = new ArrayList<ActiveChangeSet>();
            ChangeSet[] sets = getSets();
            for (int i = 0; i < sets.length; i++) {
                ChangeSet set = sets[i];
                if (set.contains(resource)) {
                    set.remove(resource);
                    if (set.isEmpty()) {
                        toRemove.add((ActiveChangeSet) set);
                    }
                }
            }
            for (ActiveChangeSet set : toRemove) {
                remove(set);
            }
        }

        private ActiveChangeSet[] getContainingSets(IResource resource) {
            Set<ActiveChangeSet> result = new HashSet<ActiveChangeSet>();
            ChangeSet[] sets = getSets();
            for (int i = 0; i < sets.length; i++) {
                ChangeSet set = sets[i];
                if (set.contains(resource)) {
                    result.add((ActiveChangeSet) set);
                }
            }
            return result.toArray(new ActiveChangeSet[result.size()]);
        }
    }

    private class ResourceCollector extends SubscriberResourceCollector {

        public ResourceCollector(Subscriber subscriber) {
            super(subscriber);
        }

        /**
         * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector
         *      #remove(org.eclipse.core.resources.IResource)
         */
        @Override
        protected void remove(IResource resource) {
            if (handler != null)
                handler.queueEvent(new BackgroundEventHandler.ResourceEvent(
                        resource, RESOURCE_REMOVAL, IResource.DEPTH_INFINITE),
                        false);
        }

        /**
         * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector
         *      #change(org.eclipse.core.resources.IResource, int)
         */
        @Override
        protected void change(IResource resource, int depth) {
            if (handler != null)
                handler.queueEvent(new BackgroundEventHandler.ResourceEvent(
                        resource, RESOURCE_CHANGE, depth), false);
        }

        @Override
        protected boolean hasMembers(IResource resource) {
            return SubscriberChangeSetManager.this.hasMembers(resource);
        }
    }

    /**
     * Create a new subcriber-based change set manager
     * 
     * @param subscriber
     */
    public SubscriberChangeSetManager(Subscriber subscriber) {
        collector = new ResourceCollector(subscriber);
        handler = new EventHandler(NLS.bind(
                Messages.SubscriberChangeSetCollector_1,
                new String[] { subscriber.getName() }), NLS.bind(
                Messages.SubscriberChangeSetCollector_2,
                new String[] { subscriber.getName() })); //
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ChangeSetManager#initializeSets
     *      ()
     */
    @Override
    protected void initializeSets() {
        load(getPreferences());
    }

    /**
     * @param resource
     * @return - true if has members
     */
    public boolean hasMembers(IResource resource) {
        ChangeSet[] sets = getSets();
        for (int i = 0; i < sets.length; i++) {
            ActiveChangeSet set = (ActiveChangeSet) sets[i];
            if (set.getDiffTree().getChildren(resource.getFullPath()).length > 0)
                return true;
        }
        if (getDefaultSet() != null)
            return (getDefaultSet().getDiffTree().getChildren(
                    resource.getFullPath()).length > 0);
        return false;
    }

    /**
     * Return the sync info for the given resource obtained from the subscriber.
     * 
     * @param resource
     *            the resource
     * @return the sync info for the resource
     * @throws CoreException
     */
    @Override
    public IDiff getDiff(IResource resource) throws CoreException {
        Subscriber subscriber = getSubscriber();
        return subscriber.getDiff(resource);
    }

    /**
     * Return the subscriber associated with this collector.
     * 
     * @return the subscriber associated with this collector
     */
    public Subscriber getSubscriber() {
        return collector.getSubscriber();
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector
     *      #dispose()
     */
    @Override
    public void dispose() {
        handler.shutdown();
        collector.dispose();
        super.dispose();
        save(getPreferences());
    }

    private Preferences getPreferences() {
        return getParentPreferences().node(getSubscriberIdentifier());
    }

    private static Preferences getParentPreferences() {
        return getTeamPreferences().node(PREF_CHANGE_SETS);
    }

    private static Preferences getTeamPreferences() {
        return new InstanceScope().getNode(TeamPlugin.getPlugin().getBundle()
                .getSymbolicName());
    }

    /**
     * Return the id that will uniquely identify the subscriber across restarts.
     * 
     * @return the id that will uniquely identify the subscriber across
     */
    protected String getSubscriberIdentifier() {
        return getSubscriber().getName();
    }

    /**
     * Wait until the collector is done processing any events. This method is
     * for testing purposes only.
     * 
     * @param monitor
     */
    public void waitUntilDone(IProgressMonitor monitor) {
        monitor.worked(1);
        // wait for the event handler to process changes.
        while (handler.getEventHandlerJob().getState() != Job.NONE) {
            monitor.worked(1);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            Policy.checkCanceled(monitor);
        }
        monitor.worked(1);
    }

    /**
     * Update changeset with resource that contains a diff
     * 
     * @param resource
     */
    protected void updateChangeSet(IResource resource) {
        // Does nothing
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager#getName
     *      ()
     */
    @Override
    protected String getName() {
        return getSubscriber().getName();
    }
}
