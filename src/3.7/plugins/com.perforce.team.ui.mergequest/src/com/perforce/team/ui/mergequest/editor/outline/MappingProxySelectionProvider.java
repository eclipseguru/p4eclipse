/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.outline;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingProxySelectionProvider implements ISelectionProvider,
        ISelectionChangedListener {

    private ListenerList listeners;
    private ISelectionProvider provider;
    private ISelection selection = StructuredSelection.EMPTY;

    /**
     * Create a new mapping proxy selection provider
     * 
     * @param provider
     */
    public MappingProxySelectionProvider(ISelectionProvider provider) {
        this.provider = provider;
        this.listeners = new ListenerList();
        this.provider.addSelectionChangedListener(this);
        selectionChanged(null);
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    public ISelection getSelection() {
        return this.selection;
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection selection) {
        // Ignore direct selection updates
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection newSelection = (IStructuredSelection) provider
                .getSelection();
        Object[] selected = newSelection.toArray();
        for (int i = 0; i < selected.length; i++) {
            if (selected[i] instanceof MappingProxy) {
                selected[i] = ((MappingProxy) selected[i]).getMapping();
            }
        }
        this.selection = new StructuredSelection(selected);
        for (Object listener : this.listeners.getListeners()) {
            ((ISelectionChangedListener) listener)
                    .selectionChanged(new SelectionChangedEvent(this,
                            this.selection));
        }
    }

}
