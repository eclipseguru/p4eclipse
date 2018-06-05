/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.MoveToAnotherChangelistAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ReopenModelOperation extends PerforceSyncModelOperation {

    private IP4PendingChangelist moveTo = null;

    /**
     * @param configuration
     * @param elements
     */
    public ReopenModelOperation(ISynchronizePageConfiguration configuration,
            IDiffElement[] elements) {
        super(configuration, elements);
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        P4UIUtils.getDisplay().syncExec(new Runnable() {

            public void run() {
                final P4Collection collection = P4ConnectionManager
                        .getManager().createP4Collection();
                for (SyncInfo element : getSyncInfoSet().getSyncInfos()) {
                    if (element instanceof PerforceSyncInfo) {
                        collection.add(((PerforceSyncInfo) element).getP4File());
                    }
                }
                MoveToAnotherChangelistAction move = new MoveToAnotherChangelistAction();
                move.setAsync(isAsync());
                move.setCollection(collection);
                if (moveTo != null) {
                    move.move(moveTo);
                } else {
                    move.run(null);
                }
            }
        });
    }

    /**
     * Set list to move selected files into
     * 
     * @param list
     *            - pending changelist
     */
    public void setMoveToList(IP4PendingChangelist list) {
        this.moveTo = list;
    }

}
