/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.actions;

import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.synchronize.PerforceSyncModelOperation;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CreatePatchModelOperation extends PerforceSyncModelOperation {

    /**
     * @param configuration
     * @param elements
     */
    public CreatePatchModelOperation(
            ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
        super(configuration, elements);
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        final P4Collection collection = createCollection();
        for (SyncInfo element : getSyncInfoSet().getSyncInfos()) {
            if (element instanceof PerforceSyncInfo) {
                collection.add(((PerforceSyncInfo) element).getP4File());
            }
        }

        if (!collection.isEmpty()) {
            P4UIUtils.getDisplay().syncExec(new Runnable() {

                public void run() {
                    CreatePatchAction action = new CreatePatchAction();
//                    action.setShell(P4UIUtils.getShell());
                    action.setCollection(collection);
                    action.run(null);
                }
            });
        }
    }

}
