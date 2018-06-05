/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IContentProviderExtension extends ITreeContentProvider,
        IDoubleClickListener {

    /**
     * Modify children
     * 
     * @param parent
     * @param defaultChildren
     * @param context
     * @return - modified array or null to use original array
     */
    Object[] modifyChildren(Object parent, Object[] defaultChildren,
            Object context);

    /**
     * Load children
     * 
     * @param parent
     * @param context
     */
    void loadChildren(Object parent, Object context);

    /**
     * Children of specified parent have been loaded
     * 
     * @param parent
     * @param children
     * @param context
     */
    void childrenLoaded(Object parent, Object[] children, Object context);

}
