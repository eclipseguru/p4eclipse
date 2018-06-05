/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.properties;

import com.perforce.team.core.mergequest.model.Branch;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchPropertySourceFactory implements IAdapterFactory {

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        Object adapted = null;
        if (adapterType == IPropertySource.class
                && adaptableObject instanceof Branch) {
            adapted = new BranchPropertySource((Branch) adaptableObject);
        }
        return adapted;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class<?>[] getAdapterList() {
        return new Class[] { IPropertySource.class };
    }

}
