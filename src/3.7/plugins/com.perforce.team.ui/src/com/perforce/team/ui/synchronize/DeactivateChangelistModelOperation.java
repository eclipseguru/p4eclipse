/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.synchronize.P4PendingChangeSet;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DeactivateChangelistModelOperation extends
        PerforceSyncModelOperation {

    private P4PendingChangeSet set;

    /**
     * @param configuration
     * @param set
     */
    public DeactivateChangelistModelOperation(
            ISynchronizePageConfiguration configuration, P4PendingChangeSet set) {
        super(configuration, new IDiffElement[0]);
        this.set = set;
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        if (set != null) {
            IP4PendingChangelist list = set.getChangelist();
            if (list != null && list.isActive()) {
                list.getConnection().setActivePendingChangelist(-1);
            }
        }

    }

}
