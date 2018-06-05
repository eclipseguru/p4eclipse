package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4ConnectionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Handle drops onto pending view
 */
public class PendingDropAdapter extends ViewerDropAdapter {

    // The current drop target
    private IP4PendingChangelist currentTarget;

    private IP4ShelvedChangelist shelvedTarget;
    private boolean unshelve = false;

    // The current transfer data
    private TransferData currentTransfer;

    private boolean async = true;

    /**
     * PendingDropAdapter
     * 
     * @param viewer
     * @param async
     */
    public PendingDropAdapter(Viewer viewer, boolean async) {
        super(viewer);
        setScrollExpandEnabled(false);
        this.async = async;
    }

    /**
     * Constructor
     * 
     * @param viewer
     */
    public PendingDropAdapter(Viewer viewer) {
        this(viewer, true);
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#drop(org.eclipse.swt.dnd.DropTargetEvent)
     */
    @Override
    public void drop(DropTargetEvent event) {
        super.drop(event);
        event.detail = DND.DROP_LINK;
    }

    /**
     * Check if we can do a drop
     * 
     * @param target
     * @param operation
     * @param transferType
     * @return - true if valid
     */
    @Override
    public boolean validateDrop(Object target, int operation,
            TransferData transferType) {
        if (transferType != null) {
            currentTransfer = transferType;
        }
        currentTarget = null;
        shelvedTarget = null;
        unshelve = false;
        if (target != null) {
            if (target instanceof IP4PendingChangelist) {
                // Don't allow drops on other clients changelists
                if (!((IP4PendingChangelist) target).isReadOnly()) {
                    currentTarget = (IP4PendingChangelist) target;
                    return true;
                }
            } else if (target instanceof IP4File) {
                // Don't allow drops on other clients files
                IP4File file = (IP4File) target;
                IP4PendingChangelist list = file.getChangelist();
                if (list != null && !list.isReadOnly()) {
                    currentTarget = list;
                    return true;
                }
            } else if (target instanceof IP4ShelvedChangelist) {
                if (!((IP4ShelvedChangelist) target).isReadOnly()) {
                    shelvedTarget = (IP4ShelvedChangelist) target;
                    return true;
                }
            } else if (target instanceof IP4ShelveFile) {
                IP4ShelvedChangelist list = ((IP4ShelveFile) target)
                        .getChangelist();
                if (list != null && !list.isReadOnly()) {
                    shelvedTarget = list;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Do the drop action
     * 
     * @param data
     * @return - true for drop
     */
    @Override
    public boolean performDrop(Object data) {
        if (data == null) {
            return false;
        } else if (ResourceTransfer.getInstance().isSupportedType(
                currentTransfer)) {
            if (data instanceof IResource[]) {
                return doResourceDrop((IResource[]) data);
            }
        } else if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
            if (data instanceof String[]) {
                return doFileDrop((String[]) data);
            }
        } else if (data instanceof IStructuredSelection) {
            return doSelectionDrop((IStructuredSelection) data);
        }
        return false;
    }

    private void unshelveDrop(IStructuredSelection selection) {
        List<IP4ShelveFile> resources = new ArrayList<IP4ShelveFile>();
        for (Object object : selection.toArray()) {
            if (object instanceof IP4ShelveFile) {
                resources.add((IP4ShelveFile) object);
            }
        }
        IP4PendingChangelist currentList = currentTarget;
        if (currentList != null && !resources.isEmpty()) {
            unshelve(resources, currentList);
        }
    }

    private boolean doSelectionDrop(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }
        P4Collection collection = P4ConnectionManager.getManager()
                .createP4Collection();
        List<IResource> resources = new ArrayList<IResource>();
        for (Object object : selection.toArray()) {
            if (object instanceof IP4File) {
                collection.add((IP4File) object);
            } else if (object instanceof IP4Job) {
                collection.add((IP4Job) object);
            } else if (object instanceof IP4ShelveFile) {
                // If selection contains any shelved files then it must be an
                // unshelve
                unshelve = true;
                unshelveDrop(selection);
                break;
            } else if (object instanceof IResource) {
                resources.add((IResource) object);
            } else if (object instanceof IAdaptable) {
                object = ((IAdaptable) object).getAdapter(IResource.class);
                if (object instanceof IResource) {
                    resources.add((IResource) object);
                }
            }
        }
        if (!unshelve) {
            addP4Resources(collection, resources.toArray(new IResource[0]));
            dropCollection(collection);
        }
        return true;
    }

    private void addP4Resources(P4Collection collection, IResource[] resources) {
        IFile[] files = P4CoreUtils.getResourceFiles(resources);
        for (IFile file : files) {
            collection.add(P4ConnectionManager.getManager().getResource(file));
        }
    }

    /**
     * Do the drop action for resources
     */
    private boolean doResourceDrop(IResource[] resources) {
        if (resources.length == 0) {
            return false;
        }
        IFile[] files = P4CoreUtils.getResourceFiles(resources);
        P4Collection collection = new P4Collection();
        for (IFile file : files) {
            collection.add(P4ConnectionManager.getManager().getResource(file));
        }
        dropCollection(collection);
        return true;
    }

    private void shelve(final P4Collection collection,
            final IP4ShelvedChangelist currentShelve) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(
                        Messages.PendingDropAdapter_ShelvingPerforceResources,
                        1);
                IP4PendingChangelist pending = currentShelve.getConnection()
                        .getPendingChangelist(currentShelve.getId(), true);
                if (pending != null) {
                    pending.updateShelvedFiles(collection.members());
                }
            }

            @Override
            public String getTitle() {
                return Messages.PendingDropAdapter_ShelvingPerforceResources;
            }
        };
        if (async) {
            P4Runner.schedule(runnable);
        } else {
            runnable.run(new NullProgressMonitor());
        }
    }

    private void unshelve(final List<IP4ShelveFile> collection,
            final IP4PendingChangelist currentList) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(
                        Messages.PendingDropAdapter_UnshelvingPerforceResources,
                        1);
                int id = currentList.getId();
                P4Collection refreshCollection = new P4Collection();
                for (IP4ShelveFile resource : collection) {
                    if (resource != null) {
                        if (P4CoreUtils.equals(resource.getConnection(),
                                currentList.getConnection())) {
                            IP4ShelvedChangelist shelved = resource
                                    .getChangelist();
                            IFileSpec[] specs = shelved.unshelve(
                                    new IP4Resource[] { resource }, id);
                            P4Collection listCollection = P4Collection
                                    .getValidCollection(
                                            currentList.getConnection(),
                                            Arrays.asList(specs),
                                            refreshCollection.getType());
                            refreshCollection.addAll(listCollection);
                        }
                    }
                }
                if (!refreshCollection.isEmpty()) {
                    refreshCollection.refresh();
                    refreshCollection
                            .refreshLocalResources(IResource.DEPTH_ONE);
                }
            }

            @Override
            public String getTitle() {
                return Messages.PendingDropAdapter_UnshelvingPerforceResources;
            }
        };
        if (async) {
            P4Runner.schedule(runnable);
        } else {
            runnable.run(new NullProgressMonitor());
        }
    }

    private void open(final P4Collection collection,
            final IP4PendingChangelist currentList) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(
                        Messages.PendingDropAdapter_OpeningPerforceResources, 4);
                collection.add(currentList.getId());
                monitor.worked(1);
                collection.edit(currentList.getId());
                monitor.worked(1);
                collection.reopen(currentList);
                monitor.worked(1);
                collection.fix(currentList);
                monitor.worked(1);
                monitor.done();
            }

            @Override
            public String getTitle() {
                return Messages.PendingDropAdapter_OpeningPerforceResources;
            }
        };
        if (async) {
            P4Runner.schedule(runnable);
        } else {
            runnable.run(new NullProgressMonitor());
        }
    }

    private void dropCollection(final P4Collection collection) {
        if (!unshelve && collection != null && !collection.isEmpty()) {
            IP4PendingChangelist currentList = currentTarget;
            IP4ShelvedChangelist currentShelve = shelvedTarget;
            if (currentList != null) {
                open(collection, currentList);
            } else if (currentShelve != null) {
                shelve(collection, currentShelve);
            }
        }
    }

    /**
     * Do the drop action for files dragged from other changelists
     * 
     * @param files
     * @return - true if dropped, false otherwise
     */
    public boolean doFileDrop(final String[] files) {
        if (files.length == 0) {
            return false;
        }
        if (DragData.getSource() instanceof PendingDragAdapter) {
            IP4Connection connection = DragData.getConnection();
            P4Collection collection = new P4Collection();
            for (String file : files) {
                collection.add(connection.getFile(file));
            }
            dropCollection(collection);
        }
        DragData.clear();
        return true;
    }

}
