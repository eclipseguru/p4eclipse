/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.synchronize.P4PendingChangeSet;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;
import com.perforce.team.ui.changelists.ISubmitMessageProvider;
import com.perforce.team.ui.p4java.actions.SubmitAction;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CommitModelOperation extends PerforceSyncModelOperation {

    private boolean showDialog = true;
    private boolean reopen = false;
    private String description = null;
    private IStructuredSelection selection = null;

    /**
     * @param configuration
     * @param elements
     */
    public CommitModelOperation(ISynchronizePageConfiguration configuration,
            IDiffElement[] elements) {
        this(configuration, elements, null);
    }

    /**
     * @param configuration
     * @param elements
     * @param selection
     */
    public CommitModelOperation(ISynchronizePageConfiguration configuration,
            IDiffElement[] elements, IStructuredSelection selection) {
        super(configuration, elements);
        this.selection = selection;
    }

    private ISubmitMessageProvider generateMessageProvider() {
        if (selection != null) {
            final Map<IP4PendingChangelist, P4PendingChangeSet> changesets = new HashMap<IP4PendingChangelist, P4PendingChangeSet>();
            ChangeSet set = null;
            for (Object element : selection.toArray()) {
                set = P4CoreUtils.convert(element, ChangeSet.class);
                if (set instanceof P4PendingChangeSet) {
                    P4PendingChangeSet pendingSet = (P4PendingChangeSet) set;
                    if (pendingSet.useCommentOnSubmit()) {
                        IP4PendingChangelist list = pendingSet.getChangelist();
                        if (list != null) {
                            changesets.put(list, pendingSet);
                        }
                    }
                }
            }
            if (changesets.size() > 0) {
                return new ISubmitMessageProvider() {

                    public String getDescription(IP4PendingChangelist list) {
                        P4PendingChangeSet set = changesets.get(list);
                        return set != null ? set.getComment() : null;
                    }
                };
            }
        }
        return null;
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
        HashSet<IP4Connection> connections = new HashSet<IP4Connection>();
        for (IP4Resource resource : collection.members()) {
            IP4Connection connection = resource.getConnection();
            if (connection != null) {
                connections.add(connection);
            }
        }

        SubmitAction action = new SubmitAction();
        action.setAsync(false);
        action.setCollection(collection);
        action.setDescription(description);
        action.setReopen(reopen);
        action.setMonitor(monitor);
        action.setMessageProvider(generateMessageProvider());
        action.runAction(showDialog);

        P4Collection selectionCollection = action.getSelected();
        if (selectionCollection != null) {
            for (SyncInfo element : getSyncInfoSet().getSyncInfos()) {
                if (element instanceof PerforceSyncInfo) {
                    IP4File file = ((PerforceSyncInfo) element).getP4File();
                    if (file != null && !file.isOpened()
                            && selectionCollection.contains(file)) {
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

    /**
     * @return the reopen
     */
    public boolean isReopen() {
        return this.reopen;
    }

    /**
     * @param reopen
     *            the reopen to set
     */
    public void setReopen(boolean reopen) {
        this.reopen = reopen;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
