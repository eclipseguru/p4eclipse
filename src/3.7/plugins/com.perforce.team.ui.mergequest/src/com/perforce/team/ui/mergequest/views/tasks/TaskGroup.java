/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import com.perforce.team.core.p4java.IP4SubmittedChangelist;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class TaskGroup extends WorkbenchAdapter implements ITaskGroup {

    private Set<IP4SubmittedChangelist> lists;
    private String name;

    /**
     * Create task group
     * 
     * @param name
     */
    public TaskGroup(String name) {
        this.name = name;
        this.lists = new HashSet<IP4SubmittedChangelist>();
    }

    /**
     * Add list to group
     * 
     * @param list
     */
    public void add(IP4SubmittedChangelist list) {
        if (list != null) {
            lists.add(list);
        }
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object object) {
        return lists.toArray();
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object object) {
        return this.name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TaskGroup) {
            return this.name.equals(((TaskGroup) obj).name);
        }
        return false;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

}
