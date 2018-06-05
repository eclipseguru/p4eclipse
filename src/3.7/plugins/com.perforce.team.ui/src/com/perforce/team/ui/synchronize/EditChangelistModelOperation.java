/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.synchronize.P4PendingChangeSet;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.EditChangelistAction;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditChangelistModelOperation extends PerforceSyncModelOperation {

    private P4PendingChangeSet set;

    /**
     * @param configuration
     * @param set
     */
    public EditChangelistModelOperation(
            ISynchronizePageConfiguration configuration, P4PendingChangeSet set) {
        super(configuration, new IDiffElement[0]);
        this.set = set;
    }

    private void editChangelists(IProgressMonitor monitor) {
        IP4PendingChangelist list = this.set.getChangelist();
        if (list != null) {
            EditChangelistAction editAction = new EditChangelistAction();
            editAction.selectionChanged(null, new StructuredSelection(list));
            editAction.setMonitor(monitor);
            editAction.run(null);
        }
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(final IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        if (this.set != null) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    editChangelists(monitor);
                }
            });
        }
    }

}
