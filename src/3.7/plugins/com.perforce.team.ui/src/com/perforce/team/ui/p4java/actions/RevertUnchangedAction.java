/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.ConfirmRevertDialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevertUnchangedAction extends P4Action {

    private P4Collection collectionSelection = null;

    /**
     * Runs a revert unchanged with optional confirm and info dialogs
     * 
     * @param showDialog
     */
    public void runAction(final boolean showDialog) {
        final P4Collection collection = getResourceSelection();
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 1);
                monitor.setTaskName(generateTitle(null, collection));
                final P4Collection reverts = collection
                        .previewUnchangedRevert();
                final List<IP4File> files = new ArrayList<IP4File>();
                for (IP4Resource resource : reverts.allMembers()) {
                    if (resource instanceof IP4File) {
                        files.add((IP4File) resource);
                    }
                }
                monitor.worked(1);
                monitor.done();
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (files.size() > 0) {
                            IP4File[] selected = null;
                            if (showDialog) {
                                ConfirmRevertDialog dialog = new ConfirmRevertDialog(
                                        getShell(), files
                                                .toArray(new IP4File[0]));
                                if (dialog.open() == ConfirmRevertDialog.OK) {
                                    selected = dialog.getSelected();
                                }
                            } else {
                                selected = files.toArray(new IP4File[0]);
                            }
                            if (selected != null && selected.length > 0) {
                                collectionSelection = createCollection(selected);
                                revert(collectionSelection, collection);
                            }
                        } else if (showDialog) {
                            MessageDialog
                                    .openInformation(
                                            getShell(),
                                            Messages.RevertUnchangedAction_RevertUnchangedTitle,
                                            Messages.RevertUnchangedAction_RevertUnchangedMessage);
                        }
                    }
                });
            }

            @Override
            public String getTitle() {
                return Messages.RevertUnchangedAction_RevertingUnchanged;
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        runAction(true);
    }

    private void revert(final P4Collection collectionSelection,
            final P4Collection resourceCollection) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 2);
                monitor.setTaskName(generateTitle(null, collectionSelection));
                collectionSelection.revert();
                monitor.worked(1);
                monitor.setTaskName(Messages.RevertUnchangedAction_RefreshingLocalResources);
                resourceCollection
                        .refreshLocalResources(IResource.DEPTH_INFINITE);
                collectionSelection.resetStateValidation();
                monitor.worked(1);
                monitor.done();
                updateActionState();
            }

            @Override
            public String getTitle() {
                return Messages.RevertUnchangedAction_RevertingFiles;
            }

        };
        runRunnable(runnable);
    }

    /**
     * Gets the selected elements that were reverted
     * 
     * @return - collection of reverted resources
     */
    public P4Collection getSelected() {
        return this.collectionSelection;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = super.getResourceSelection();
                IP4Resource[] resources = collection.members();
                int size = resources.length;
                if (size > 0) {
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            if (file.getAction() != null) {
                                IP4PendingChangelist list = file.getChangelist();
                                // Only allow revert of files contained in
                                // changed owned by the current connection
                                if (list != null && list.isOnClient() && !list.isReadOnly()) {
                                    enabled = true;
                                    break;
                                }
                            }
                        } else {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

}
