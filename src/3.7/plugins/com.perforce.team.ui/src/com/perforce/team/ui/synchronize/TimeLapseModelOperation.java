/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.ui.timelapse.TimeLapseAction;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapseModelOperation extends PerforceSyncModelOperation {

    /**
     * @param configuration
     * @param elements
     */
    protected TimeLapseModelOperation(
            ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
        super(configuration, elements);
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        P4Collection collection = createCollection();
        for (SyncInfo element : getSyncInfoSet().getSyncInfos()) {
            if (element instanceof PerforceSyncInfo) {
                collection.add(((PerforceSyncInfo) element).getP4File());
            }
        }

        TimeLapseAction action = new TimeLapseAction();
        action.setAsync(false);
        action.setCollection(collection);
        action.run(null);
    }

}
