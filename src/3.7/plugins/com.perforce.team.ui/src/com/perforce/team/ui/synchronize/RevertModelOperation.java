/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.RevertAction;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevertModelOperation extends PerforceSyncModelOperation {

    private boolean showDialog = true;

    /**
     * @param configuration
     * @param elements
     */
    public RevertModelOperation(ISynchronizePageConfiguration configuration,
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

        final RevertAction action = new RevertAction();
        action.setAsync(false);
        P4UIUtils.getDisplay().syncExec(new Runnable() {

            public void run() {
//                action.setShell(P4UIUtils.getShell());
                action.setCollection(collection);
                action.runAction(showDialog);
            }
        });

        P4Collection selectionCollection = action.getSelected();
        if (selectionCollection != null) {
            for (IP4Resource resource : selectionCollection.members()) {
                if (resource instanceof IP4File) {
                    IFile[] files = ((IP4File) resource).getLocalFiles();
                    for (IFile file : files) {
                        if (file != null) {
                            try {
                                file.refreshLocal(IResource.DEPTH_ONE, null);
                            } catch (CoreException e) {
                                PerforceProviderPlugin.logError(e);
                            }
                            updateSyncState(file);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the showDialog
     */
    public boolean isShowDialog() {
        return this.showDialog;
    }

    /**
     * @param showDialog
     *            the showDialog to set
     */
    public void setShowDialog(boolean showDialog) {
        this.showDialog = showDialog;
    }

}
