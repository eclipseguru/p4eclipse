/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.progress.UIJob;

import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.ShowHistoryAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class HistoryModelOperation extends PerforceSyncModelOperation {

    /**
     * @param configuration
     * @param elements
     */
    public HistoryModelOperation(ISynchronizePageConfiguration configuration,
            IDiffElement[] elements) {
        super(configuration, elements);
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        UIJob job = new UIJob(Messages.HistoryModelOperation_OpeningHistoryView) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                final P4Collection collection = P4ConnectionManager
                        .getManager().createP4Collection();
                for (SyncInfo element : getSyncInfoSet().getSyncInfos()) {
                    if (element instanceof PerforceSyncInfo) {
                        collection
                                .add(((PerforceSyncInfo) element).getP4File());
                    }
                }
                ShowHistoryAction history = new ShowHistoryAction();
                history.setCollection(collection);
                history.run(null);
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

}
