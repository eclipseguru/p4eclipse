/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.history;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.team.core.history.IFileRevision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class HistoryDragAdapter extends DragSourceAdapter {

    private ISelectionProvider provider = null;
    private IStructuredSelection selection = null;

    /**
     * @param provider
     */
    public HistoryDragAdapter(ISelectionProvider provider) {
        this.provider = provider;
    }

    /**
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragFinished(DragSourceEvent event) {
        this.selection = null;
    }

    /**
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragSetData(DragSourceEvent event) {
        if (this.selection != null
                && LocalSelectionTransfer.getTransfer().isSupportedType(
                        event.dataType)) {
            event.data = this.selection;
        }
    }

    /**
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragStart(DragSourceEvent event) {
        if (this.provider != null) {
            IStructuredSelection selection = ((IStructuredSelection) this.provider
                    .getSelection());
            if (selection != null && selection.size() == 1
                    && selection.getFirstElement() instanceof IFileRevision) {
                this.selection = selection;
                LocalSelectionTransfer.getTransfer().setSelection(
                        this.selection);
                event.doit = true;
            } else {
                this.selection = null;
                event.doit = false;
            }

        } else {
            event.doit = false;
        }
    }

}
