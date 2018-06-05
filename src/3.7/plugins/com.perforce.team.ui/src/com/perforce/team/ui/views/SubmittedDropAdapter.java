package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003, 2005 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4ConnectionManager;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Handle drops on submitted changelists view
 */
public class SubmittedDropAdapter extends ViewerDropAdapter {

    private SubmittedViewControl view;

    // The current transfer data
    private TransferData currentTransfer;

    /**
     * Constructor
     * 
     * @param view
     */
    public SubmittedDropAdapter(SubmittedViewControl view) {
        super(view.getViewer());
        this.view = view;
        setScrollExpandEnabled(false);
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
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    @Override
    public boolean performDrop(Object data) {
        if (data == null) {
            return false;
        } else if (ResourceTransfer.getInstance().isSupportedType(
                currentTransfer)) {
            return doResourceDrop((IResource[]) data);
        } else if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
            return doFileDrop((String[]) data);
        } else if (data instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) data;
            if (selection.size() == 1) {
                Object first = selection.getFirstElement();
                if (first instanceof IResource) {
                    return doResourceDrop(new IResource[] { (IResource) first });
                } else if (first instanceof IP4Resource) {
                    return doP4ResourceDrop(new IP4Resource[] { (IP4Resource) first });
                } else if (first instanceof IAdaptable) {
                    Object adapted = ((IAdaptable) first)
                            .getAdapter(IResource.class);
                    if (adapted instanceof IResource) {
                        return doResourceDrop(new IResource[] { (IResource) adapted });
                    } else {
                        adapted = ((IAdaptable) first)
                                .getAdapter(IP4Resource.class);
                        if (adapted instanceof IP4Resource) {
                            return doP4ResourceDrop(new IP4Resource[] { (IP4Resource) adapted });
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object,
     *      int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object target, int operation,
            TransferData transferType) {
        currentTransfer = transferType;
        if (ResourceTransfer.getInstance().isSupportedType(currentTransfer)) {
            return true;
        } else if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
            return true;
        } else if (LocalSelectionTransfer.getTransfer().isSupportedType(
                currentTransfer)) {
            ISelection selection = LocalSelectionTransfer.getTransfer()
                    .getSelection();
            // Only single filter currently supported to ensure single selection
            return selection instanceof IStructuredSelection
                    && ((IStructuredSelection) selection).size() == 1;
        } else {
            return false;
        }
    }

    /**
     * Drops the selected file path to the submitted view
     * 
     * @param files
     * @return - true if dropped
     */
    public boolean doFileDrop(String[] files) {
        if (files.length != 1 || DragData.getConnection() == null) {
            return false;
        }
        IP4Connection con = DragData.getConnection();
        DragData.clear();
        IP4Resource resource = con.getResource(files[0]);
        if (resource == null) {
            IP4Resource[] depots = con.members();
            if (depots != null) {
                for (IP4Resource depot : depots) {
                    if (files[0].equals(depot.getRemotePath())) {
                        resource = depot;
                        break;
                    }
                }
            }
        }
        if (resource != null) {
            view.showChangelists(resource);
            return true;
        }
        return false;
    }

    private boolean doP4ResourceDrop(IP4Resource[] resources) {
        if (resources.length != 1) {
            return false;
        }
        DragData.clear();
        if (resources[0] != null) {
            view.showChangelists(resources[0]);
            return true;
        }
        return false;
    }

    private boolean doResourceDrop(IResource[] resources) {
        if (resources.length != 1) {
            return false;
        }
        DragData.clear();
        IP4Resource resource = P4ConnectionManager.getManager().getResource(
                resources[0]);
        if (resource != null) {
            view.showChangelists(resource);
            return true;
        }
        return false;
    }
}
