/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.ui.p4java.actions.SyncAction;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class UpdateModelOperation extends PerforceSyncModelOperation {

    /**
     * @param configuration
     * @param elements
     */
    public UpdateModelOperation(ISynchronizePageConfiguration configuration,
            IDiffElement[] elements) {
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
        SyncAction action = new SyncAction();
        action.setAsync(false);
        action.setCollection(collection);
        action.runAction();
        for (SyncInfo element : getSyncInfoSet().getSyncInfos()) {
            if (element instanceof PerforceSyncInfo) {
                IP4File file = ((PerforceSyncInfo) element).getP4File();
                if (collection.contains(file)) {
                    try {
                        element.getLocal().refreshLocal(IResource.DEPTH_ONE,
                                null);
                        if (element.getLocal().getParent() != null) {
                            element.getLocal()
                                    .getProject()
                                    .refreshLocal(IResource.DEPTH_INFINITE,
                                            null);
                        }
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                    updateSyncState(element.getLocal());
                }
            }
        }
    }

}
