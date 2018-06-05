/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.ResourceTransfer;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Resource;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobsDropAdapter extends ViewerDropAdapter {

    // The view
    private JobViewControl view;

    // The current transfer data
    private TransferData currentTransfer;

    /**
     * Constructor
     * 
     * @param view
     */
    public JobsDropAdapter(JobViewControl view) {
        super(view.getTableViewer());
        this.view = view;
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
            List<IResource> resources = new ArrayList<IResource>();
            for (Object object : selection.toArray()) {
                if (object instanceof IResource) {
                    resources.add((IResource) object);
                } else if (object instanceof IAdaptable) {
                    object = ((IAdaptable) object).getAdapter(IResource.class);
                    if (object instanceof IResource) {
                        resources.add((IResource) object);
                    }
                }
            }
            return doResourceDrop(resources.toArray(new IResource[0]));
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
            return true;
        } else {
            return false;
        }
    }

    /**
     * Drop files onto the history view
     * 
     * @param files
     * @return - true if dropped
     */
    public boolean doFileDrop(String[] files) {
        if (files == null || files.length != 1
                || DragData.getConnection() == null) {
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
            String path = resource.getActionPath();
            if (path != null) {
                view.changeConnection(con);
                view.setPath(path);
                return true;
            }
        }
        return false;
    }

    private boolean doResourceDrop(IResource[] resources) {
        if (resources == null || resources.length != 1) {
            return false;
        }
        IP4Connection con = DragData.getConnection();
        view.changeConnection(con);
        IPath path = resources[0].getLocation();
        boolean dropped = false;
        if (path != null) {
            String actionPath = path.makeAbsolute().toOSString();
            if (resources[0] instanceof IContainer) {
                actionPath += IP4Container.DIR_ELLIPSIS;
            }
            view.setPath(actionPath);
            dropped = true;
        }
        return dropped;
    }
}
