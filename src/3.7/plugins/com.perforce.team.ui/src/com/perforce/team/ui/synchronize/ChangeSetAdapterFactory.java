/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.p4java.synchronize.IP4ChangeSet;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.ui.synchronize.ChangeSetDiffNode;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ChangeSetAdapterFactory implements IAdapterFactory {

    private static final Class[] CLASSES = new Class[] { IP4ChangeSet.class };

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
     *      java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (IP4ChangeSet.class == adapterType) {
            if (adaptableObject instanceof ChangeSetDiffNode) {
                ChangeSet set = ((ChangeSetDiffNode) adaptableObject).getSet();
                if (set instanceof IP4ChangeSet) {
                    return set;
                }
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return CLASSES;
    }

}
