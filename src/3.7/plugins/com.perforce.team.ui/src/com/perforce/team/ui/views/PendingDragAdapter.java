package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4ShelveFile;

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
 * Pending drag adapter
 */
public class PendingDragAdapter extends DragSourceAdapter {

    private ISelectionProvider selectionProvider;
    private IStructuredSelection currentSelection;

    /**
     * Pending drag adapter
     * 
     * @param provider
     */
    public PendingDragAdapter(ISelectionProvider provider) {
        selectionProvider = provider;
    }

    /**
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragStart(DragSourceEvent event) {
        ISelection selection = selectionProvider.getSelection();
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            this.currentSelection = getValidSelection((IStructuredSelection) selection);
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
        IP4File[] files = getSelectedFiles();
        if (files == null || files.length == 0) {
            return;
        }

        // resort to a file transfer
        if (!FileTransfer.getInstance().isSupportedType(event.dataType)) {
            return;
        }

        // Get the path of each file and set as the drag data
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getLocalPath();
        }
        event.data = fileNames;
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
        if (element instanceof IP4File) {
            return ((IP4File) element).getConnection();
        } else {
            return null;
        }
    }

    private IStructuredSelection getValidSelection(
            IStructuredSelection selection) {
        IStructuredSelection structuredSelection = null;
        if (selection != null) {
            structuredSelection = selection;
            // Check that selection only contains jobs, p4 files, or shelved
            // files
            for (Object obj : structuredSelection.toArray()) {
                if (!(obj instanceof IP4Job) && !(obj instanceof IP4File)
                        && !(obj instanceof IP4ShelveFile)) {
                    structuredSelection = null;
                    break;
                }
            }
        }
        return structuredSelection;
    }

    private IP4File[] getSelectedFiles() {
        List<IP4File> files = new ArrayList<IP4File>();

        ISelection selection = selectionProvider.getSelection();
        if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
            return null;
        }
        IStructuredSelection structuredSelection = (IStructuredSelection) selection;

        // loop through list and look for matching items
        for (Object obj : structuredSelection.toArray()) {
            if (obj instanceof IP4File) {
                files.add((IP4File) obj);
            }
        }
        return files.toArray(new IP4File[files.size()]);
    }
}
