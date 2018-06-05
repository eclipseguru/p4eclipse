/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.pending;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.P4ConnectionManager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingListener implements IP4Listener {

    /**
     * Viewer
     */
    protected TreeViewer viewer;

    /**
     * Create a listener for a tree viewer that displays pending changelists
     * 
     * @param viewer
     */
    public PendingListener(TreeViewer viewer) {
        this.viewer = viewer;
        this.viewer.getTree().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                P4ConnectionManager.getManager().removeListener(
                        PendingListener.this);
            }
        });
        P4ConnectionManager.getManager().addListener(this);
    }

    /**
     * Is the viewer created and not disposed?
     * 
     * @return - true if ok to use, false if disposed or not created
     */
    public boolean okToUse() {
        return viewer != null && viewer.getTree() != null
                && !viewer.getTree().isDisposed();
    }

    /**
     * Add specified lists to the view
     * 
     * @param resources
     */
    protected void addLists(IP4Resource[] resources) {
        for (IP4Resource resource : resources) {
            if (resource instanceof IP4PendingChangelist
                    && ((IP4PendingChangelist) resource).getId() > 0) {
                viewer.add(viewer.getInput(), resource);
            }
        }
    }

    /**
     * Update specified elements in the viewer
     * 
     * @param elements
     */
    protected void handleUpdateEvent(Object[] elements) {
        viewer.update(elements, null);
    }

    /**
     * Refresh and reveal job elements
     * 
     * @param resources
     * @param reveal
     * @param type
     */
    protected void handleJobEvent(IP4Resource[] resources, boolean reveal,
            EventType type) {
        List<IP4PendingChangelist> processed = new ArrayList<IP4PendingChangelist>();
        if (resources.length > 0) {
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4Job) {
                    IP4Container parent = resource.getParent();
                    if (parent instanceof IP4PendingChangelist
                            && !processed.contains(parent)) {
                        viewer.refresh(parent);
                        processed.add((IP4PendingChangelist) parent);
                        if (reveal) {
                            viewer.reveal(resource);
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove specified elements from the viewer
     * 
     * @param elements
     */
    protected void handleRemoveEvent(Object[] elements) {
        viewer.remove(elements);
    }

    /**
     * Update viewer for submitted changelist
     * 
     * @param lists
     */
    protected void handleSubmitChangelistEvent(IP4PendingChangelist[] lists) {
        if (lists.length > 0) {
            viewer.remove(lists);
            List<IP4Changelist> processed = new ArrayList<IP4Changelist>();
            for (IP4PendingChangelist list : lists) {
                IP4Connection connection = list.getConnection();
                if (connection != null) {
                    IP4Changelist defaultChangelist = connection
                            .getPendingChangelist(0);
                    if (defaultChangelist != null
                            && !processed.contains(defaultChangelist)) {
                        defaultChangelist.markForRefresh();
                        viewer.refresh(defaultChangelist);
                        processed.add(defaultChangelist);
                    }
                }
            }
        }
    }

    /**
     * Refresh list containing specified files
     * 
     * @param files
     */
    protected void handleOpened(IP4File[] files) {
        List<IP4PendingChangelist> processed = new ArrayList<IP4PendingChangelist>();
        for (IP4File file : files) {
            IP4PendingChangelist list = file.getChangelist();
            if (list != null && !processed.contains(list)) {
                viewer.refresh(list);
                viewer.reveal(file);
                processed.add(list);
            }
        }
    }

    /**
     * Refresh specified resources in the viewer
     * 
     * @param resources
     */
    protected void handleRefresh(IP4Resource[] resources) {
        List<IP4PendingChangelist> processed = new ArrayList<IP4PendingChangelist>();
        for (IP4Resource resource : resources) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                if (!file.isOpened()) {
                    viewer.remove(file);
                } else {
                    IP4PendingChangelist list = file.getChangelist();
                    if (list != null && !processed.contains(list)) {
                        viewer.refresh(list);
                        handleExpand(list);
                        processed.add(list);
                    }
                }
            } else if (resource instanceof IP4PendingChangelist) {
                if (!processed.contains(resource)) {
                    viewer.refresh(resource);
                    handleExpand(resource);
                    processed.add((IP4PendingChangelist) resource);
                }
            }
        }
    }

    /**
     * Handle auto-expansion of pending changelists
     * 
     * @param resource
     */
    protected void handleExpand(IP4Resource resource) {
        if (viewer.getExpandedState(resource)) {
            viewer.expandToLevel(resource, 2);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
     */
    public void resoureChanged(final P4Event event) {
        EventType type = event.getType();
        if (type != EventType.CREATE_CHANGELIST
                && type != EventType.DELETE_CHANGELIST
                && type != EventType.CHANGED && type != EventType.FIXED
                && type != EventType.UNFIXED && type != EventType.REFRESHED
                && type != EventType.OPENED && type != EventType.REVERTED
                && type != EventType.SUBMITTED
                && type != EventType.SUBMIT_CHANGELIST
                && type != EventType.DELETE_SHELVE
                && type != EventType.UPDATE_SHELVE
                && type != EventType.CREATE_SHELVE) {
            return;
        }
        UIJob job = new UIJob(
                Messages.PendingListener_UpdatingPendingChangelistView) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (okToUse() && viewer.getInput() != null) {
                    EventType type = event.getType();
                    switch (type) {
                    case REFRESHED:
                    case DELETE_SHELVE:
                    case UPDATE_SHELVE:
                    case CREATE_SHELVE:
                        handleRefresh(event.getResources());
                        break;
                    case CHANGED:
                        handleUpdateEvent(event.getPending());
                        break;
                    case FIXED:
                        handleJobEvent(event.getResources(), true, type);
                        break;
                    case UNFIXED:
                        handleJobEvent(event.getResources(), false, type);
                        break;
                    case CREATE_CHANGELIST:
                        addLists(event.getResources());
                        break;
                    case DELETE_CHANGELIST:
                        handleRemoveEvent(event.getPending());
                        break;
                    case SUBMIT_CHANGELIST:
                        handleSubmitChangelistEvent(event.getPending());
                        break;
                    case SUBMITTED:
                        handleRemoveEvent(event.getUnopenedFiles());
                        break;
                    case REVERTED:
                        handleRemoveEvent(event.getFiles());
                        break;
                    case OPENED:
                        handleOpened(event.getFiles());
                        break;
                    default:
                        break;
                    }
                }
                return Status.OK_STATUS;
            }

        };
        job.setSystem(true);
        job.schedule();
    }

	public String getName() {
		return getClass().getSimpleName();
	}

}
