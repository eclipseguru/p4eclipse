/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.synchronize.P4SubmittedChangeSet;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.ViewChangelistAction;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ViewChangelistModelOperation extends PerforceSyncModelOperation {

    private P4SubmittedChangeSet[] sets;

    /**
     * @param configuration
     * @param sets
     */
    public ViewChangelistModelOperation(
            ISynchronizePageConfiguration configuration,
            P4SubmittedChangeSet[] sets) {
        super(configuration, new IDiffElement[0]);
        this.sets = sets;
    }

    private void viewChangelists(IProgressMonitor monitor) {
        for (P4SubmittedChangeSet set : sets) {
            IP4Changelist list = set.getChangelist();
            if (list != null) {
                ViewChangelistAction view = new ViewChangelistAction();
                view.selectionChanged(null, new StructuredSelection(list));
                view.setMonitor(monitor);
                view.run(null);
            }
        }
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(final IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        if (this.sets != null) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    viewChangelists(monitor);
                }
            });
        }
    }
}
