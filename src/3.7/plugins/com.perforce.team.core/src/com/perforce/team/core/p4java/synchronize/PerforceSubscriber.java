/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.synchronize;

import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ISubscriberChangeEvent;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberChangeEvent;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class PerforceSubscriber extends Subscriber {

    private static PerforceSubscriber subscriber;

    /**
     * Class to hold the local, base, and remote sync information
     */
    private static class InternalSyncInfo {

        PerforceSyncFile base;
        PerforceSyncFile remote;
        IP4File p4File;
    }

    private static class ResourceVisitor implements IResourceVisitor {

        Set<IResource> visited = new HashSet<IResource>();

        public boolean visit(IResource resource) throws CoreException {
            visited.add(resource);
            return true;
        }

        public void add(IResource[] resources) throws CoreException {
            for (IResource resource : resources) {
                visit(resource);
            }
        }

        public int size() {
            return this.visited.size();
        }

        public IResource[] toArray() {
            return this.visited.toArray(new IResource[this.visited.size()]);
        }

    }

    /**
     * Gets the singleton perforce subscriber
     * 
     * @return - the perforce subscriber instance
     */
    public static synchronized PerforceSubscriber getSubscriber() {
        if (subscriber == null) {
            subscriber = new PerforceSubscriber();
        }
        return subscriber;
    }

    /* Infos must be a synchronized map. 
     * Note: 
     *   http://stackoverflow.com/questions/2688629/is-a-hashmap-thread-safe-for-different-keys  
     *   A HashMap that is updated without synchronization will break even if the threads are using disjoint sets of keys. e.g.:
     *   o If one thread does a put, then another thread may see a stale value for the hashmap's size.
     *   o When a thread does a put that triggers a rebuild of the table, another thread may see transient
     *     or stale versions of the hashtable array reference, its size, its contents or the hash chains. Chaos may ensue.
     *   o When a thread does a put for a key that collides with some key used by some other thread, 
     *     and the latter thread does a put for its key, then the latter might see a stale copy of 
     *     hash chain reference. Chaos may ensue.
     *   o When one thread probes the table with a key that collides with one of some other thread's keys,
     *     it may encounter that key on the chain. It will call equals on that key, and if the threads are 
     *     not synchronized, the equals method may encounter stale state in that key.
     */
    private Map<IResource, InternalSyncInfo> infos;
    private IResourceVisitor nullVisitor = new IResourceVisitor() {

        public boolean visit(IResource resource) throws CoreException {
            return true;
        }
    };

    private IResourceVariantComparator comparator = new IResourceVariantComparator() {

        public boolean compare(IResource local, IResourceVariant remote) {
            InternalSyncInfo holder = infos.get(local);
            if (holder != null) {
                PerforceSyncFile file = holder.remote;
                if (file == null) {
                    file = holder.base;
                }
                if (file != null && file.getFile() != null) {
                    return !file.getFile().isOpened();
                }
            }
            return false;
        }

        public boolean compare(IResourceVariant base, IResourceVariant remote) {
            if (base == remote) {
                return true;
            }
            if (base == null || remote == null) {
                return false;
            }
            return base.getContentIdentifier().equals(
                    remote.getContentIdentifier());
        }

        public boolean isThreeWay() {
            return true;
        }

    };

    private IP4Listener listener = new IP4Listener() {

        public void resoureChanged(P4Event event) {
            ResourceVisitor visitor = new ResourceVisitor();
            try {
                if (EventType.REFRESHED == event.getType()) {
                    for (IP4Resource resource : event.getResources()) {
                        if (resource instanceof IP4File) {
                            updateFile((IP4File) resource, visitor);
                        } else if (resource instanceof IP4PendingChangelist) {
                            for (IP4File file : ((IP4PendingChangelist) resource)
                                    .getPendingFiles()) {
                                updateFile(file, visitor);
                            }
                        }
                    }
                }
                visitor.add(event.getLocalResources());
                if (visitor.size() > 0) {
                    fireTeamResourceChange(SubscriberChangeEvent
                            .asSyncChangedDeltas(PerforceSubscriber.this,
                                    visitor.toArray()));
                }
            } catch (CoreException e) {
                // Ignore visited errors
            }
        }

		public String getName() {
			return PerforceSubscriber.this.getClass().getSimpleName();
		}

    };

    private PerforceSubscriber() {
        infos = Collections.synchronizedMap(new HashMap<IResource, InternalSyncInfo>());
        P4Workspace.getWorkspace().addListener(listener);
    }

    /**
     * @see org.eclipse.team.core.subscribers.Subscriber#getName()
     */
    @Override
    public String getName() {
        return Messages.PerforceSynchronizations;
    }

    /**
     * @see org.eclipse.team.core.subscribers.Subscriber#getResourceComparator()
     */
    @Override
    public IResourceVariantComparator getResourceComparator() {
        return this.comparator;
    }

    /**
     * @see org.eclipse.team.core.subscribers.Subscriber#getSyncInfo(org.eclipse.core.resources.IResource)
     */
    @Override
    public SyncInfo getSyncInfo(IResource resource) throws TeamException {
        SyncInfo info = null;
        try {
            if (resource.getType() == IResource.FILE) {
                InternalSyncInfo element = infos.get(resource);
                if (element != null) {
                    info = new PerforceSyncInfo(resource, element.base,
                            element.remote, getResourceComparator(),
                            element.p4File);
                    info.init();
                }
            }
        } catch (Exception e) {
            PerforceProviderPlugin.logError(e);
        } catch (Error e) {
            PerforceProviderPlugin.logError(e);
        }
        return info;
    }

    /**
     * @see org.eclipse.team.core.subscribers.Subscriber#isSupervised(org.eclipse.core.resources.IResource)
     */
    @Override
    public boolean isSupervised(IResource resource) throws TeamException {
        return PerforceTeamProvider.getPerforceProvider(resource) != null;
    }

    /**
     * @see org.eclipse.team.core.subscribers.Subscriber#members(org.eclipse.core.resources.IResource)
     */
    @Override
    public IResource[] members(IResource resource) throws TeamException {
        IResource[] members = null;
        if (resource.getType() == IResource.FILE) {
            members = new IResource[0];
        } else if (resource instanceof IContainer) {
            List<IResource> memberList = new ArrayList<IResource>();
            try {
                memberList.addAll(Arrays.asList(((IContainer) resource)
                        .members(IContainer.EXCLUDE_DERIVED)));
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
            members = memberList.toArray(new IResource[0]);
        }
        return members;
    }

    /**
     * This method returns true when a synchronize state should be shown for the
     * specified file
     * 
     * @param file
     * @return - true if sync info should be created for the file
     */
    private boolean isOutOfSync(IP4File file) {
        if (file.getRemotePath() != null) {
            if (file.isOpened()) {
                // Check that file is opened by owner, so different user but
                // same workspace entries don't appear in synchronize view.
                // Fix for job034676,
                return file.openedByOwner();
            } else if (!file.isSynced()) {
                // Only mark as out of sync if the have revision is not zero
                // with a head action of delete
                // Fix for job032976
                return !(file.getHaveRevision() == 0 && file
                        .isHeadActionDelete());
            }
        }
        return false;
    }

    private void updateFile(IP4File file, IResourceVisitor visitor)
            throws CoreException {
        IFile[] files = file.getLocalFiles();
        String name = file.getName();
        if (isOutOfSync(file)) {
            for (IFile localFile : files) {
                visitor.visit(localFile);
                // Handle linked files by replacing name with last segment of
                // location, fix for job034089
                String fileName = localFile.getName();
                if (localFile.isLinked()) {
                    IPath location = localFile.getLocation();
                    if (location != null && location.lastSegment() != null) {
                        fileName = location.lastSegment();
                    }
                }

                // Cautious check to ensure file generated from fstat matched
                // Eclipse resource file name, this can cause issues on Windows
                // when a depot file has been renamed and only case has changed.
                // Fix for job032688
                if (fileName.equals(name)) {
                    InternalSyncInfo holder = infos.get(localFile);
                    if (holder == null) {
                        holder = new InternalSyncInfo();
                        infos.put(localFile, holder);
                    }
                    holder.remote = null;
                    holder.base = null;
                    holder.p4File = file;
                    if (!file.openedForAdd()) {

                        // Only add a remote file if this file is not opened for
                        // add and the head revision is not deleted
                        if (!file.isHeadActionDelete()) {
                            holder.remote = new PerforceSyncFile(file,
                                    PerforceSyncFile.VariantType.REMOTE);
                        }
                        // Only add a base variant if the have revision is
                        // greater than zero and the file is not opened for add
                        if (file.getHaveRevision() > 0) {
                            holder.base = new PerforceSyncFile(file,
                                    PerforceSyncFile.VariantType.BASE);
                        }
                    }
                }
            }
        } else {
            for (IFile localFile : files) {
                visitor.visit(localFile);
                // Cautious check to ensure file generated from fstat matched
                // Eclipse resource file name, this can cause issues on Windows
                // when a depot file has been renamed and only case has changed.
                // Fix for job032688
                if (localFile.getName().equals(name)) {
                    infos.remove(localFile);
                }
            }
        }
    }

    /**
     * Add linked folders to the specified collection found in the array and any
     * sub resources of containers found. Fix for job034089.
     * 
     * @param resources
     * @param withLinked
     */
    private void addLinkedFolders(IResource[] resources,
            Collection<IResource> withLinked) {
        for (IResource resource : resources) {
            if (resource.isLinked()) {
                withLinked.add(resource);
            }
            if (resource instanceof IContainer) {
                IContainer container = (IContainer) resource;
                try {
                    addLinkedFolders(
                            container.members(IContainer.EXCLUDE_DERIVED),
                            withLinked);
                } catch (CoreException e) {
                    // Ignore exception and move on to next resource
                }
            }
        }
    }

    private void refreshResources(IProgressMonitor monitor, int work,
            IResource[] resources) {
        SubProgressMonitor refreshMonitor = new SubProgressMonitor(monitor,
                work);
        refreshMonitor.beginTask("", resources.length); //$NON-NLS-1$
        try {
            for (IResource resource : resources) {
                IPath location = resource.getLocation();
                if (location != null) {
                    String path = location.makeAbsolute().toOSString();
                    if (path != null) {
                        refreshMonitor.setTaskName(MessageFormat.format(
                                Messages.Refreshing, path));
                    }
                }
                try {
                    resource.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                    // Suppress refresh errors and continue with synchronization
                }
                refreshMonitor.worked(1);
            }
        } finally {
            refreshMonitor.done();
        }
    }

    private void loadResources(IProgressMonitor monitor, int work,
            IResource[] resources, List<IP4Resource> p4Resources) {
        SubProgressMonitor loadMonitor = new SubProgressMonitor(monitor, work);
        loadMonitor.beginTask("", resources.length); //$NON-NLS-1$
        try {
            for (IResource resource : resources) {
                IPath location = resource.getLocation();
                if (location != null) {
                    String path = location.makeAbsolute().toOSString();
                    if (path != null) {
                        loadMonitor.setTaskName(MessageFormat.format(
                                Messages.Loading, path));
                    }
                }
                IP4Resource file = P4Workspace.getWorkspace().getResource(
                        resource);
                if (file != null) {
                    p4Resources.add(file);
                }
                loadMonitor.worked(1);
            }
        } finally {
            loadMonitor.done();
        }
    }

    private void statResources(IProgressMonitor monitor, int work,
            List<IP4Resource> p4Resources, List<IResource> newFiles)
            throws CoreException {
        SubProgressMonitor statMonitor = new SubProgressMonitor(monitor, work);
        statMonitor.beginTask("", p4Resources.size()); //$NON-NLS-1$
        try {
            P4Collection collection = new P4Collection(
                    p4Resources.toArray(new IP4Resource[p4Resources.size()]));
            collection.resolve(new ResolveFilesAutoOptions().setShowActionsOnly(true).setShowBase(true));
            IP4File[] localFiles = collection.getAllLocalFiles(statMonitor);

            for (IP4File resource : localFiles) {
                IP4File file = resource;
                if (file.getRemotePath() != null) {
                    IFile[] files = file.getLocalFiles();
                    for (IFile localFile : files) {
                        newFiles.add(localFile);
                    }
                    updateFile(file, nullVisitor);
                }
            }
        } finally {
            statMonitor.done();
        }
    }

    /**
     * @see org.eclipse.team.core.subscribers.Subscriber#refresh(org.eclipse.core.resources.IResource[],
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void refresh(IResource[] resources, int depth,
            IProgressMonitor monitor) throws TeamException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        // Use a set for the resources so any linked resources aren't double
        // added if they appear in the original array
        Set<IResource> original = new HashSet<IResource>();
        for (IResource resource : resources) {
            original.add(resource);
        }

        // Add all linked resources found in all containers at any depth, fix
        // for job034089
        Set<IResource> withLinked = new HashSet<IResource>();
        addLinkedFolders(resources, withLinked);
        if (withLinked.size() > 0) {
            original.addAll(withLinked);
        }

        IResource[] combined = original.toArray(new IResource[0]);

        // Remove sync info for files
        List<IFile> allFiles = P4CoreUtils.getAllFiles(combined);
        for (IFile file : allFiles) {
            infos.remove(file);
        }

        List<IResource> newFiles = new ArrayList<IResource>();
        try {
            monitor.beginTask(Messages.Synchronizing, 80);

            refreshResources(monitor, 10, combined);
            List<IP4Resource> p4Files = new ArrayList<IP4Resource>();
            loadResources(monitor, 20, combined, p4Files);
            statResources(monitor, 50, p4Files, newFiles);

            monitor.done();
        } catch (Exception e) {
            PerforceProviderPlugin.logError(e);
        } catch (Error e) {
            PerforceProviderPlugin.logError(e);
        } finally {
            if (!newFiles.isEmpty()) {
                fireTeamResourceChange(SubscriberChangeEvent
                        .asSyncChangedDeltas(this,
                                newFiles.toArray(new IResource[0])));
            }
        }
    }

    /**
     * Removes a resource from the subscriber's model
     * 
     * @param resource
     */
    public void remove(IResource resource) {
        this.infos.remove(resource);
    }

    /**
     * @see org.eclipse.team.core.subscribers.Subscriber#fireTeamResourceChange(org.eclipse.team.core.subscribers.ISubscriberChangeEvent[])
     */
    @Override
    public void fireTeamResourceChange(ISubscriberChangeEvent[] deltas) {
        super.fireTeamResourceChange(deltas);
    }

    /**
     * @see org.eclipse.team.core.subscribers.Subscriber#roots()
     */
    @Override
    public IResource[] roots() {
        List<IResource> projects = new ArrayList<IResource>();
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
                .getProjects()) {
            if (project.isAccessible()
                    && PerforceTeamProvider.getPerforceProvider(project) != null) {
                projects.add(project);
            }
        }
        return projects.toArray(new IResource[projects.size()]);
    }

}
