/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.AsyncEnablementAction;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelveAction extends AsyncEnablementAction {

    /**
     * Show shelving not support dialog for specified connection
     * 
     * @param connection
     */
    public static void showNotSupported(final IP4Connection connection) {
        if (connection != null) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    P4ConnectionManager.getManager().openInformation(
                            P4UIUtils.getDialogShell(),
                            Messages.ShelveAction_NoShelvingTitle,
                            MessageFormat.format(
                                    Messages.ShelveAction_NoShelvingMessage,
                                    connection.getParameters().getPort()));
                }
            });
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = false;
            P4Collection collection = getResourceSelection();
            for (IP4Resource resource : collection.members()) {
                if (resource instanceof IP4Container) {
                    enabled = resource.getConnection().isShelvingSupported();
                    if (enabled) {
                        break;
                    }
                } else if (resource instanceof IP4File) {
                    IP4File file = (IP4File) resource;
                    if (file.isOpened()) {
                        enabled = file.getConnection().isShelvingSupported();
                        if (enabled) {
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                P4Collection collection = getFileSelection();
                Map<IP4PendingChangelist, P4Collection> listMappings = new HashMap<IP4PendingChangelist, P4Collection>();
                for (IP4Resource resource : collection.members()) {
                    if (resource instanceof IP4File) {
                        IP4File file = (IP4File) resource;
                        if (file.isOpened()) {
                            IP4PendingChangelist list = file
                                    .getChangelist(true);
                            if (list != null) {
                                if (list.needsRefresh()) {
                                    list.refresh();
                                }
                                P4Collection fileCollection = listMappings
                                        .get(list);
                                if (fileCollection == null) {
                                    fileCollection = createCollection();
                                    listMappings.put(list, fileCollection);
                                }
                                fileCollection.add(file);
                            }
                        }
                    }
                }
                if (!listMappings.isEmpty()) {
                    for (Map.Entry<IP4PendingChangelist, P4Collection> entry : listMappings.entrySet()) {
                    	IP4PendingChangelist list=entry.getKey();
                        P4Collection files = entry.getValue();
                        if (list.getConnection().isShelvingSupported()) {
                            if (!list.isDefault()) {
                                showShelveDialog(list, files.members());
                            } else {
                                showDialog(list, files);
                            }
                        } else {
                            showNotSupported(list.getConnection());
                        }
                    }
                } else {
                    showNoOpenedFilesDialog();
                }
            }

            @Override
            public String getTitle() {
                return Messages.ShelveAction_ShelvingFiles;
            }

        };
        runRunnable(runnable);
    }

    private void showNoOpenedFilesDialog() {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                P4ConnectionManager.getManager().openInformation(
                        P4UIUtils.getDialogShell(),
                        Messages.ShelveAction_NoFilesToShelveTitle,
                        Messages.ShelveAction_NoFilesToShelveMessage);
            }
        });
    }

    private void shelveNew(final IP4PendingChangelist list,
            final IP4File[] files, final boolean newChangelist,
            final String initialDescription) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                int work = 1;
                if (newChangelist) {
                    work += 2;
                }
                monitor.beginTask(
                        generateTitle(Messages.ShelveAction_Shelving, files,
                                IP4Resource.Type.REMOTE), work);
                IP4PendingChangelist pending = null;
                if (newChangelist) {
                    monitor.subTask(Messages.ShelveAction_CreatingNewChangelist);
                    String description = initialDescription;
                    if (description == null) {
                        description = Messages.ShelveAction_NewShelveChangelistDescription;
                    }
                    pending = list.getConnection().createChangelist(
                            description, null);
                    monitor.worked(1);
                    if (pending != null) {
                        monitor.subTask(MessageFormat
                                .format(Messages.ShelveAction_ReopeningFilesInNewChangelist,
                                        pending.getId()));
                        createCollection(files).reopen(pending);
                    }
                    monitor.worked(1);

                } else {
                    pending = list;
                }
                if (pending != null) {
                    pending.shelve(files);
                    monitor.worked(1);
                }
                monitor.done();
            }

            @Override
            public String getTitle() {
                return Messages.ShelveAction_ShelvingFiles;
            }

        };
        runRunnable(runnable);
    }

    private void shelveUpdate(final IP4PendingChangelist pending,
            final IP4Resource[] files) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(
                        generateTitle(Messages.ShelveAction_Shelving, files,
                                IP4Resource.Type.REMOTE), 1);
                if (pending != null) {
                    pending.updateShelvedFiles(files);
                    monitor.worked(1);
                }
                monitor.done();
            }

            @Override
            public String getTitle() {
                return Messages.ShelveAction_ShelvingFiles;
            }

        };
        runRunnable(runnable);
    }

    private void showShelveDialog(final IP4PendingChangelist list,
            final IP4Resource[] selected) {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                ShelveFileDialog dialog = new ShelveFileDialog(P4UIUtils
                        .getDialogShell(), list, list.members(), selected);
                if (ShelveFileDialog.OK == dialog.open()) {
                    IP4Resource[] selection = dialog.getSelectedFiles();
                    if (selection.length > 0) {
                        shelveUpdate(list, selection);
                    }
                }
            }
        });
    }

    private void showDialog(final IP4PendingChangelist list, P4Collection files) {
        IP4Resource[] members = files.members();
        final IP4File[] fileMembers = new IP4File[members.length];
        System.arraycopy(members, 0, fileMembers, 0, fileMembers.length);
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                ConfirmShelveDialog dialog = new ConfirmShelveDialog(P4UIUtils
                        .getDialogShell(), list, fileMembers);
                if (ConfirmShelveDialog.OK == dialog.open()) {
                    IP4File[] selection = dialog.getSelectedFiles();
                    if (selection.length > 0) {
                        String description = dialog.getDescription();
                        shelveNew(list, selection, true, description);
                    }
                }
            }
        });
    }
}
