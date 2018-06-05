/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class BaseContentProviderExtension implements IContentProviderExtension {

    /**
     * @see com.perforce.team.ui.IContentProviderExtension#modifyChildren(java.lang.Object,
     *      java.lang.Object[], java.lang.Object)
     */
    public Object[] modifyChildren(Object parent, Object[] defaultChildren,
            Object context) {
        return null;
    }

    /**
     * @see com.perforce.team.ui.IContentProviderExtension#loadChildren(java.lang.Object,
     *      java.lang.Object)
     */
    public void loadChildren(Object parent, Object context) {
        // Does nothing by default
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        return PerforceContentProvider.EMPTY;
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        return null;
    }

    /**
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        return PerforceContentProvider.EMPTY;
    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {

    }

    /**
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

    /**
     * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
     */
    public void doubleClick(DoubleClickEvent event) {

    }

    /**
     * @see com.perforce.team.ui.IContentProviderExtension#childrenLoaded(java.lang.Object,
     *      java.lang.Object[], java.lang.Object)
     */
    public void childrenLoaded(Object parent, Object[] children, Object context) {

    }

}
