/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.outline;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.editor.IBranchGraphPage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SelectionSynchronizer implements ISelectionChangedListener {

    private StructuredViewer base;
    private EditPartViewer extension;
    private boolean inUpdate = false;

    /**
     * Create new synchronizer with selection provider base
     * 
     * @param base
     */
    public SelectionSynchronizer(StructuredViewer base) {
        this.base = base;
        this.base.addSelectionChangedListener(this);
    }

    /**
     * Dispose of synchronizer
     */
    public void dispose() {
        this.base.removeSelectionChangedListener(this);
    }

    /**
     * Register page with synchronizer
     * 
     * @param page
     */
    public void register(IBranchGraphPage page) {
        if (this.extension != null) {
            this.extension.removeSelectionChangedListener(this);
        }
        this.extension = P4CoreUtils.convert(page, EditPartViewer.class);
        if (this.extension != null) {
            this.extension.addSelectionChangedListener(this);
        }
    }

    private Object[] convertToParts(Object[] selection) {
        List<Object> converted = new ArrayList<Object>();
        for (Object selected : selection) {
            IBranchGraphElement element = P4CoreUtils.convert(selected,
                    IBranchGraphElement.class);
            if (element != null) {
                EditPart part = (EditPart) extension.getEditPartRegistry().get(
                        element);
                if (part != null) {
                    converted.add(part);
                }
            }
        }
        return converted.toArray();
    }

    private Object[] convertToElements(Object[] selection) {
        List<Object> converted = new ArrayList<Object>();
        for (Object selected : selection) {
            IBranchGraphElement element = P4CoreUtils.convert(selected,
                    IBranchGraphElement.class);
            if (element != null) {
                if (element instanceof Mapping) {
                    MappingProxy.addProxies((Mapping) element, converted);
                } else {
                    converted.add(element);
                }
            }
        }
        return converted.toArray();
    }

    private void sync(ISelectionProvider provider,
            IStructuredSelection newSelection) {
        inUpdate = true;
        try {
            if (provider == base) {
                IStructuredSelection converted = new StructuredSelection(
                        convertToParts(newSelection.toArray()));
                extension.setSelection(converted);
            } else if (provider == extension) {
                IStructuredSelection converted = new StructuredSelection(
                        convertToElements(newSelection.toArray()));
                base.setSelection(converted, true);
            }
        } finally {
            inUpdate = false;
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if (inUpdate || this.extension == null) {
            return;
        }
        sync(event.getSelectionProvider(),
                (IStructuredSelection) event.getSelection());
    }

}
