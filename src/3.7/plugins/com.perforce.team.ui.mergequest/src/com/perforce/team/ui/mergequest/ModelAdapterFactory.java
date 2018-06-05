/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.Mapping;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ModelAdapterFactory implements IAdapterFactory {

    private BranchWorkbenchAdapter branchAdapter = new BranchWorkbenchAdapter();
    private MappingWorkbenchAdapter mappingAdapter = new MappingWorkbenchAdapter();

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (IWorkbenchAdapter.class == adapterType) {
            if (adaptableObject instanceof Branch) {
                return branchAdapter;
            } else if (adaptableObject instanceof Mapping) {
                return mappingAdapter;
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class };
    }

}
