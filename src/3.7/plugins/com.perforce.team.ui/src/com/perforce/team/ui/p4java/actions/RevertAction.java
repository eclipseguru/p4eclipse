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
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.dialogs.ConfirmRevertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevertAction extends P4Action {

    private P4Collection collectionSelection = null;

    /**
     * Revert action constructor
     */
    public RevertAction() {
        // Fix for job032028
        setAsync(false);
    }

    /**
     * Gets the collection to revert from a collection of resources
     * 
     * @param collection
     *            - collection of resources
     * @return - collection to revertion
     */
    protected P4Collection getCollection(P4Collection collection) {
        P4Collection changelistCollection = createCollection();
        for (IP4Resource resource : collection.members()) {
            if (resource instanceof IP4PendingChangelist) {
                IP4PendingChangelist list = (IP4PendingChangelist) resource;
                if (list.needsRefresh()) {
                    list.refresh();
                }
                IP4Resource[] resources = list.members();
                for (IP4Resource file : resources) {
                    changelistCollection.add(file);
                }
            } else {
                changelistCollection.add(resource);
            }
        }
        return changelistCollection;
    }

    /**
     * Create dialog
     * 
     * @param files
     * @return confirm revert dialog
     */
    protected ConfirmRevertDialog createDialog(List<IP4File> files) {
        return new ConfirmRevertDialog(getShell(),
                files.toArray(new IP4File[files.size()]), false);
    }

    /**
     * Delete empty changelists in the collection
     * 
     * @param collection
     */
    protected void deleteEmpties(final P4Collection collection) {
        // Run always async since finding empty changelist might require
        // refreshing
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.RevertAction_DeletingEmptyChangelists;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                P4Collection delete = createCollection();
                for (IP4Resource resource : collection.members()) {
                    if (resource instanceof IP4PendingChangelist) {
                        IP4PendingChangelist list = (IP4PendingChangelist) resource;
                        if (list.needsRefresh()) {
                            list.refresh();
                        }
                        if (list.isDeleteable() && !list.isShelved()) {
                            delete.add(resource);
                        }
                    }
                }
                DeleteChangelistAction deleteAction = new DeleteChangelistAction();
                // Run sync since we are async here
                deleteAction.setAsync(false);
                deleteAction.setCollection(delete);
                deleteAction.run(null);
            }
        });
    }

    /**
     * Runs a revert and optional shows a confirmation dialog where files can be
     * de-selected
     * 
     * @param showDialog
     */
    public void runAction(boolean showDialog) {
        final P4Collection collection = getResourceSelection();
        final P4Collection changelistCollection = getCollection(collection);
        final P4Collection reverts = changelistCollection.previewRevert();
        List<IP4File> files = new ArrayList<IP4File>();
        for (IP4Resource resource : reverts.allMembers()) {
            if (resource instanceof IP4File) {
                files.add((IP4File) resource);
            }
        }
        if (files.size() > 0) {
            IP4File[] selected = null;
            P4Collection deleteCollection = null;
            boolean deleteShelved = false;
            boolean deleteEmpties = false;
            if (showDialog) {
                ConfirmRevertDialog dialog = createDialog(files);
                if (dialog.open() == ConfirmRevertDialog.OK) {
                    selected = dialog.getSelected();
                    deleteShelved = dialog.deleteShelvedFiles();
                    deleteEmpties = dialog.deleteEmptyChangelists();
                    if (deleteShelved || deleteEmpties) {
                        deleteCollection = changelistCollection;
                    }
                }
            } else {
                selected = files.toArray(new IP4File[0]);
            }
            if (selected != null && selected.length > 0) {
                collectionSelection = createCollection(selected);
                collectionSelection.setType(collection.getType());
                revert(collectionSelection, deleteCollection, deleteEmpties,
                        deleteShelved);
            }
        } else if (showDialog) {
            MessageDialog.openInformation(getShell(),
                    Messages.RevertAction_NoFilesToRevertTitle,
                    Messages.RevertAction_NoFilesToRevertMessage);
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        runAction(true);
    }

    private void deleteShelved(
            Map<IP4PendingChangelist, List<IP4Resource>> shelvedCollection) {
        for (Map.Entry<IP4PendingChangelist, List<IP4Resource>> entry : shelvedCollection.entrySet()) {
            List<IP4Resource> files = entry.getValue();
            if (files != null && files.size() > 0) {
                entry.getKey().deleteShelve(files.toArray(new IP4Resource[files.size()]));
            }
        }
    }

    private void revert(final P4Collection collectionSelection,
            final P4Collection deleteCollection, final boolean deleteEmpties,
            final boolean deleteShelved) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.RevertAction_Reverting;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                int work = 1;
                if (deleteCollection != null) {
                    if (deleteShelved) {
                        work++;
                    }
                    if (deleteEmpties) {
                        work++;
                    }
                }

                monitor.beginTask(getTitle(), work);
                monitor.subTask(generateTitle(null, collectionSelection));

                Map<IP4PendingChangelist, List<IP4Resource>> deleteShelvedCollection = null;
                if (deleteShelved) {
                    deleteShelvedCollection = new HashMap<IP4PendingChangelist, List<IP4Resource>>();
                    for (IP4Resource resource : collectionSelection.members()) {
                        if (resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            IP4PendingChangelist list = file.getChangelist();
                            if (list != null) {
                                List<IP4Resource> files = null;
                                if (deleteShelvedCollection.containsKey(list)) {
                                    files = deleteShelvedCollection.get(list);
                                } else {
                                    files = new ArrayList<IP4Resource>();
                                    deleteShelvedCollection.put(list, files);
                                }
                                files.add(file);
                            }
                        }
                    }
                }

                collectionSelection.revert();
                monitor.worked(1);

                if (deleteCollection != null) {
                    if (deleteShelved) {
                        monitor.subTask(Messages.RevertAction_DeletingShelvedFilesSubtask);
                        deleteShelved(deleteShelvedCollection);
                        monitor.worked(1);
                    }
                    if (deleteEmpties) {
                        monitor.subTask(Messages.RevertAction_DeletingEmptyChangelistsSubtask);
                        deleteEmpties(deleteCollection);
                        monitor.worked(1);
                    }

                    // Refresh any changelists need refreshing but weren't
                    // deleted
                    for (IP4Resource resource : deleteCollection.members()) {
                        if (resource instanceof IP4PendingChangelist) {
                            IP4PendingChangelist list = (IP4PendingChangelist) resource;
                            if (list.getId() > 0 && list.needsRefresh()) {
                                list.refresh();
                            }
                        }
                    }
                }

                monitor.done();

                collectionSelection
                        .refreshLocalResources(IResource.DEPTH_INFINITE);
                collectionSelection.resetStateValidation();
                updateActionState();
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
