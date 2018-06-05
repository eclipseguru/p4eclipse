/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.IChangelist;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manager class for resources attached to pending changelists.
 * 
 * This will be used on a per-connection basis to store files and jobs attached
 * to pending changelists that are not read-only.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingResourceManager {

    private Map<Integer, Set<IP4Resource>> openedResources = null;

    /**
     *
     */
    public PendingResourceManager() {
        this.openedResources = Collections
                .synchronizedMap(new HashMap<Integer, Set<IP4Resource>>());
    }

    /**
     * Add file opened in specified changelist id
     * 
     * @param id
     * @param file
     * @return - true if file was added, false otherwise
     */
    public boolean addFile(int id, IP4File file) {
        boolean added = false;
        if (file != null && id >= IChangelist.DEFAULT && file.isOpened()) {
            synchronized (openedResources) {
                Set<IP4Resource> files = openedResources.get(id);
                if (files == null) {
                    files = new HashSet<IP4Resource>();
                    this.openedResources.put(id, files);
                }
                files.remove(file);
                added = files.add(file);
            }
        }
        return added;
    }

    /**
     * Replace the set of resources associated with the specified changelist id
     * with the specified collection of resources
     * 
     * @param id
     * @param resources
     * @return - true if replace, false otherwise
     */
    public boolean replaceResources(int id, Collection<IP4Resource> resources) {
        boolean added = false;
        if (resources != null && id >= IChangelist.DEFAULT) {
            synchronized (openedResources) {
                Set<IP4Resource> resourceSet = new HashSet<IP4Resource>();
                added = resourceSet.addAll(resources);
                this.openedResources.put(id, resourceSet);
            }
        }
        return added;
    }

    /**
     * 
     * @param id
     * @param job
     * @return - true if job was added, false otherwise
     */
    public boolean addJob(int id, IP4Job job) {
        boolean added = false;
        if (job != null && id >= IChangelist.DEFAULT) {
            synchronized (openedResources) {
                Set<IP4Resource> files = openedResources.get(id);
                if (files == null) {
                    files = new HashSet<IP4Resource>();
                    this.openedResources.put(id, files);
                }
                files.remove(job);
                added = files.add(job);
            }
        }
        return added;
    }

    /**
     * 
     * @param id
     * @param resource
     * @return - true if resource was removed, false otherwise
     */
    public boolean remove(int id, IP4Resource resource) {
        boolean removed = false;
        if (resource != null && id >= IChangelist.DEFAULT) {
            synchronized (openedResources) {
                Set<IP4Resource> resources = openedResources.get(id);
                if (resources != null) {
                    removed = resources.remove(resource);
                }
            }
        }
        return removed;
    }

    /**
     * Get files opened in the specified changelist id
     * 
     * @param id
     * @return - non-null but possibly empty collection of {@link IP4Resource}
     *         objects
     */
    public IP4Resource[] getResources(int id) {
        IP4Resource[] resources = IP4Resource.EMPTY;
        synchronized (openedResources) {
            Set<IP4Resource> files = this.openedResources.get(id);
            if (files != null) {
                resources = files.toArray(new IP4Resource[files.size()]);
            }
        }
        return resources;
    }

    /**
     * Get number of resources for specified changelist id
     * 
     * @param id
     * @return number of resources or zero if none or changelist by id not found
     */
    public int getSize(int id) {
        int size = 0;
        synchronized (openedResources) {
            Set<IP4Resource> files = this.openedResources.get(id);
            if (files != null) {
                size = files.size();
            }
        }
        return size;
    }

    /**
     * Clear all resources from this manager
     */
    public void clear() {
    	synchronized (openedResources) {
    		this.openedResources.clear();
    	}
    }
}
