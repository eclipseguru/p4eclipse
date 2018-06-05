/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Event {

    /**
     * Different event types
     */
    public enum EventType {

        /**
         * P4 resource move added
         */
        MOVE_ADDED,
        /**
         * P4 resource added
         */
        ADDED,
        /**
         * P4 resource removed
         */
        REMOVED,

        /**
         * P4 resource changed
         */
        CHANGED,

        /**
         * P4 resource opened
         */
        OPENED,

        /**
         * P4 resource submitted
         */
        SUBMITTED,

        /**
         * P4 resource submitting failed
         */
        SUBMIT_FAILED,

        /**
         * P4 resource reverted
         */
        REVERTED,

        /**
         * P4 resource refreshed
         */
        REFRESHED,

        /**
         * P4 resource resolved
         */
        RESOLVED,

        /**
         * P4 resource now available
         */
        AVAILABLE,

        /**
         * P4 resource locked
         */
        LOCKED,

        /**
         * P4 resource unlocked
         */
        UNLOCKED,

        /**
         * P4 resource fixed
         */
        FIXED,

        /**
         * P4 resource unfixed
         */
        UNFIXED,

        /**
         * P4 pending changelist created
         */
        CREATE_CHANGELIST,

        /**
         * P4 pending changelist deleted
         */
        DELETE_CHANGELIST,

        /**
         * P4 changelist submitted
         */
        SUBMIT_CHANGELIST,

        /**
         * Resources added to a .p4ignore file
         */
        IGNORED,

        /**
         * P4 job create
         */
        CREATE_JOB,

        /**
         * P4 branch create
         */
        CREATE_BRANCH,

        /**
         * P4 shelved changelist created
         */
        CREATE_SHELVE,

        /**
         * P4 shelved changelist deleted
         */
        DELETE_SHELVE,

        /**
         * P4 shelved changelist updated
         */
        UPDATE_SHELVE,

        /**
         * P4 active pending changelist changed for a connection
         */
        ACTIVE_CHANGELIST,

        /**
         * P4 active pending changelist cleared for a connection
         */
        INACTIVE_CHANGELIST,

        /**
         * Changelist submitted with jobs in event
         */
        SUBMIT_JOB,
        
        /**
         * Shelved changeslist is submitted
         */
        SUBMIT_SHELVEDCHANGELIST

    }

    private IP4Resource resource;
    private IP4Resource[] resources;
    private EventType type;

    /**
     * Creates a new p4 event for a given type and collection
     * 
     * @param type
     * @param collection
     */
    public P4Event(EventType type, P4Collection collection) {
        this.type = type;
        if (collection != null) {
            this.resources = collection.members();
        }
    }

    /**
     * Creates a new p4 event for a given type and resource
     * 
     * @param type
     * @param resource
     */
    public P4Event(EventType type, IP4Resource resource) {
        this.type = type;
        this.resource = resource;
    }

    /**
     * Creates a new p4 event for a given type and array of resources
     * 
     * @param type
     * @param resources
     */
    public P4Event(EventType type, IP4Resource[] resources) {
        this.type = type;
        this.resources = resources;
    }

    /**
     * Gets the type of this event
     * 
     * @return - even type
     */
    public EventType getType() {
        return this.type;
    }

    /**
     * Gets p4 files in this event
     * 
     * @return - array of p4 files
     */
    public IP4File[] getFiles() {
        Set<IP4File> files = new HashSet<IP4File>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4File) {
                files.add((IP4File) resource);
            }
        }
        return files.toArray(new IP4File[files.size()]);
    }

    /**
     * Gets the opened p4 files in this event
     * 
     * @return - array of p4 files
     */
    public IP4File[] getOpenedFiles() {
        Set<IP4File> files = new HashSet<IP4File>();
        IP4File file = null;
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4File) {
                file = (IP4File) resource;
                if (file.isOpened()) {
                    files.add(file);
                }
            }
        }
        return files.toArray(new IP4File[files.size()]);
    }

    /**
     * Gets the unopened p4 files in this event
     * 
     * @return - array of p4 files
     */
    public IP4File[] getUnopenedFiles() {
        Set<IP4File> files = new HashSet<IP4File>();
        IP4File file = null;
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4File) {
                file = (IP4File) resource;
                if (!file.isOpened()) {
                    files.add(file);
                }
            }
        }
        return files.toArray(new IP4File[files.size()]);
    }

    /**
     * Gets the local files in this event
     * 
     * @return - array of local files
     */
    public IFile[] getLocalFiles() {
        Set<IFile> files = new HashSet<IFile>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4File) {
                IFile[] locals = ((IP4File) resource).getLocalFiles();
                for (IFile local : locals) {
                    if (local != null) {
                        files.add(local);
                    }
                }
            }
        }
        return files.toArray(new IFile[files.size()]);
    }

    /**
     * Gets the local containers in this event
     * 
     * @return - array of local containers
     */
    public IContainer[] getLocalContainers() {
        Set<IContainer> containers = new HashSet<IContainer>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4Folder) {
                IContainer[] locals = ((IP4Folder) resource)
                        .getLocalContainers();
                if (locals != null) {
                    for (IContainer local : locals) {
                        containers.add(local);
                    }
                }
            } else if (resource instanceof IP4Connection) {
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                        .getProjects();
                for (IProject project : projects) {
                    if (resource.equals(P4Workspace.getWorkspace()
                            .getConnection(project))) {
                        containers.add(project);
                    }
                }
            }
        }
        return containers.toArray(new IContainer[containers.size()]);
    }

    /**
     * Gets the local resources in this event
     * 
     * @return - array of local resources
     */
    public IResource[] getLocalResources() {
        Set<IResource> resources = new HashSet<IResource>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4File) {
                IFile[] locals = ((IP4File) resource).getLocalFiles();
                for (IFile localFile : locals) {
                    if (localFile != null) {
                        resources.add(localFile);
                    }
                }
            } else if (resource instanceof IP4Folder) {
                IContainer[] locals = ((IP4Folder) resource)
                        .getLocalContainers();
                for (IContainer localFolder : locals) {
                    if (localFolder != null) {
                        resources.add(localFolder);
                    }
                }
            } else if (resource instanceof IP4Connection) {
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                        .getProjects();
                for (IProject project : projects) {
                    if (resource.equals(P4Workspace.getWorkspace()
                            .getConnection(project))) {
                        resources.add(project);
                    }
                }
            }
        }
        return resources.toArray(new IResource[resources.size()]);
    }

    /**
     * Gets the p4 containers in this event
     * 
     * @return - array of p4 containers
     */
    public IP4Container[] getContainers() {
        Set<IP4Container> containers = new HashSet<IP4Container>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4Container) {
                containers.add((IP4Container) resource);
            } else if (resource instanceof IP4File) {
                IP4Container parent = ((IP4File) resource).getParent();
                if (parent != null) {
                    containers.add(parent);
                }
            }
        }
        return containers.toArray(new IP4Container[containers.size()]);
    }

    /**
     * Gets the p4 changelists in this event
     * 
     * @return - array of p4 changelists
     */
    public IP4Changelist[] getChangelists() {
        Set<IP4Changelist> changelists = new HashSet<IP4Changelist>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4Changelist) {
                changelists.add((IP4Changelist) resource);
            } else if (resource instanceof IP4File) {
                IP4Changelist parent = ((IP4File) resource).getChangelist();
                if (parent != null) {
                    changelists.add(parent);
                }
            }
        }
        return changelists.toArray(new IP4Changelist[changelists.size()]);
    }

    /**
     * Get the p4 jobs in this event
     * 
     * @return - array of p4 jobs
     */
    public IP4Job[] getJobs() {
        Set<IP4Job> jobs = new HashSet<IP4Job>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4Job) {
                jobs.add((IP4Job) resource);
            }
        }
        return jobs.toArray(new IP4Job[jobs.size()]);
    }

    /**
     * Get the p4 branches in this event
     * 
     * @return - array of p4 branches
     */
    public IP4Branch[] getBranches() {
        Set<IP4Branch> branches = new HashSet<IP4Branch>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4Branch) {
                branches.add((IP4Branch) resource);
            }
        }
        return branches.toArray(new IP4Branch[branches.size()]);
    }

    /**
     * Get the p4 pending changelists in this event
     * 
     * @return - array of p4 pending changelists
     */
    public IP4PendingChangelist[] getPending() {
        Set<IP4PendingChangelist> lists = new HashSet<IP4PendingChangelist>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4PendingChangelist) {
                lists.add((IP4PendingChangelist) resource);
            }
        }
        return lists.toArray(new IP4PendingChangelist[lists.size()]);
    }

    /**
     * Gets the p4 resources in this event
     * 
     * @return - array of p4 resources
     */
    public IP4Resource[] getResources() {
        if (this.resource != null) {
            return new IP4Resource[] { this.resource };
        } else if (this.resources != null) {
            return this.resources;
        }
        return new IP4Resource[0];
    }

    /**
     * Gets the p4 connections in this event
     * 
     * @return - array of p4 connections
     */
    public IP4Connection[] getCommonConnections() {
        Set<IP4Connection> connections = new HashSet<IP4Connection>();
        for (IP4Resource resource : getResources()) {
            IP4Connection connection = resource.getConnection();
            if (connection != null) {
                connections.add(connection);
            }
        }
        return connections.toArray(new IP4Connection[connections.size()]);
    }

    /**
     * Gets the p4 connections in this event
     * 
     * @return - array of p4 connections
     */
    public IP4Connection[] getConnections() {
        Set<IP4Connection> connections = new HashSet<IP4Connection>();
        for (IP4Resource resource : getResources()) {
            if (resource instanceof IP4Connection) {
                connections.add((IP4Connection) resource);
            }
        }
        return connections.toArray(new IP4Connection[connections.size()]);
    }

    /**
     * Does this event contain the specified resource?
     * 
     * @param resource
     * @return - true if contains
     */
    public boolean contains(IP4Resource resource) {
        boolean contains = false;
        if (resource != null) {
            for (IP4Resource eventResource : getResources()) {
                if (resource.equals(eventResource)) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(type);
        buffer.append(" - "); //$NON-NLS-1$
        buffer.append("["); //$NON-NLS-1$
        IP4Resource[] resources = getResources();
        for (int i = 0; i < resources.length; i++) {
            buffer.append(resources[i].getName());
            if (i + 1 < resources.length) {
                buffer.append(", "); //$NON-NLS-1$
            }
        }
        buffer.append("]"); //$NON-NLS-1$
        return buffer.toString();
    }
}
