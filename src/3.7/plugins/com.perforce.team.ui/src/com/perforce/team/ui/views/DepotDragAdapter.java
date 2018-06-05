package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;

/**
 * Depot drag adapter
 */
public class DepotDragAdapter extends DragSourceAdapter {

    private IStructuredSelection currentSelection;
    private ISelectionProvider selectionProvider;

    /**
     * Create a depot drag adapter
     * 
     * @param provider
     */
    public DepotDragAdapter(ISelectionProvider provider) {
        selectionProvider = provider;
    }

    /**
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragStart(DragSourceEvent event) {
        ISelection selection = selectionProvider.getSelection();
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            this.currentSelection = (IStructuredSelection) selection;
            LocalSelectionTransfer.getTransfer().setSelection(
                    this.currentSelection);
        } else {
            this.currentSelection = null;
        }
        event.doit = true;
    }

    /**
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragSetData(DragSourceEvent event) {
        if (this.currentSelection != null
                && LocalSelectionTransfer.getTransfer().isSupportedType(
                        event.dataType)) {
            event.data = this.currentSelection;
            DragData.setConnection(getConnection());
            DragData.setSource(this);
            return;
        }

        String[] paths = getSelectedPaths();
        if (paths == null || paths.length == 0) {
            return;
        }

        // resort to a file transfer
        if (!FileTransfer.getInstance().isSupportedType(event.dataType)) {
            return;
        }

        event.data = paths;
        DragData.setConnection(getConnection());
        DragData.setSource(this);
    }

    /**
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragFinished(DragSourceEvent event) {
        this.currentSelection = null;
    }

    private IP4Connection getConnection() {
        IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider
                .getSelection();
        Object element = structuredSelection.getFirstElement();
        if (element instanceof IP4Resource) {
            return ((IP4Resource) element).getConnection();
        }
        return null;
    }

    private String[] getSelectedPaths() {
        List<String> paths = new ArrayList<String>();
        ISelection selection = selectionProvider.getSelection();
        if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
            return null;
        }
        IStructuredSelection structuredSelection = (IStructuredSelection) selection;

        // loop through list and look for matching items
        for (Object obj : structuredSelection.toArray()) {
            if (obj instanceof IP4Resource) {
                String path = ((IP4Resource) obj).getLocalPath();
                if (path == null) {
                    path = ((IP4Resource) obj).getRemotePath();
                }
                if (path != null) {
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }
}
