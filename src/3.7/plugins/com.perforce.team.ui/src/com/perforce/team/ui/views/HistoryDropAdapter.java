package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.history.P4HistoryPage;
import com.perforce.team.ui.p4java.actions.ShowHistoryAction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Handle drops on revision history view
 */
public class HistoryDropAdapter extends ViewerDropAdapter {

    // The current transfer data
    private TransferData currentTransfer;
    private P4HistoryPage view;

    /**
     * Constructor
     * 
     * @param view
     */
    public HistoryDropAdapter(P4HistoryPage view) {
        super(view.getViewer());
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
                IResource resource = P4CoreUtils.convert(object,
                        IResource.class);
                if (resource != null) {
                    resources.add(resource);
                } else if (object instanceof IFileRevision) {
                    // Revision drop if one revision is being dragged
                    if (selection.size() == 1) {
                        return doHistoryDrop((IFileRevision) object);
                    }
                }
            }
            return doResourceDrop(resources.toArray(new IResource[resources
                    .size()]));
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
        if (resource != null && resource instanceof IP4File) {
            showHistory(resource);
            return true;
        }
        return false;
    }

    private void showHistory(final Object resource) {
        if (resource == null) {
            return;
        }
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                ShowHistoryAction show = new ShowHistoryAction();
                show.selectionChanged(null, new StructuredSelection(resource));
                show.run(null);
            }
        });
    }

    private boolean doHistoryDrop(IFileRevision revision) {
        if (revision != null) {
            Object firstRevision = getCurrentTarget();
            if (firstRevision instanceof IFileRevision) {
                this.view.compare((IFileRevision) firstRevision, revision);
                return true;
            }
        }
        return false;
    }

    private boolean doResourceDrop(IResource[] resources) {
        if (resources == null || resources.length != 1) {
            return false;
        }
        showHistory(resources[0]);

        return true;
    }
}
