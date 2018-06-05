/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.ResolveAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ResolveModelOperation extends PerforceSyncModelOperation {

    private ResolveFilesAutoOptions options = null;
    private boolean showDialog = true;

    /**
     * @param configuration
     * @param elements
     */
    public ResolveModelOperation(ISynchronizePageConfiguration configuration,
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
        final ResolveAction action = new ResolveAction();
        action.setCollection(collection);
        final List<IP4File> events = new ArrayList<IP4File>();
        IP4Listener listener = new IP4Listener() {

            public void resoureChanged(P4Event event) {
                if (P4Event.EventType.RESOLVED == event.getType()) {
                    for (IP4File file : event.getFiles()) {
                        events.add(file);
                    }
                }
            }
    		public String getName() {
    			return ResolveModelOperation.this.getClass().getSimpleName();
    		}
        };

        // Sync before showing resolve dialog since that is how things get
        // marked as unresolved
        collection.sync(monitor);

        P4ConnectionManager.getManager().addListener(listener);
        if (showDialog) {
            P4UIUtils.getDisplay().syncExec(new Runnable() {

                public void run() {
//                    action.setShell(P4UIUtils.getShell());
                    action.runAction();
                }
            });
        } else if (options != null) {
            action.resolve(options);
        }
        P4ConnectionManager.getManager().removeListener(listener);

        if (!events.isEmpty()) {
            for (SyncInfo element : getSyncInfoSet().getSyncInfos()) {
                if (element instanceof PerforceSyncInfo) {
                    IP4File file = ((PerforceSyncInfo) element).getP4File();
                    if (file != null && events.contains(file)) {
                        try {
                            element.getLocal().refreshLocal(
                                    IResource.DEPTH_ONE, null);

                        } catch (CoreException e) {
                            PerforceProviderPlugin.logError(e);
                        }
                        updateSyncState(element.getLocal());
                    }
                }
            }
        }
    }

    /**
     * @return the options
     */
    public ResolveFilesAutoOptions getOptions() {
        return this.options;
    }

    /**
     * @param options
     *            the options to set
     */
    public void setOptions(ResolveFilesAutoOptions options) {
        this.options = options;
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
