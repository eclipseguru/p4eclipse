/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UnshelveAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        boolean enabled = false;
        if (this.getSelection() != null) {
            for (Object select : this.getSelection().toArray()) {
                if (select instanceof IP4ShelvedChangelist) {
                    IP4ShelvedChangelist shelveList = (IP4ShelvedChangelist) select;
                    if (shelveList.needsRefresh()) {
                        shelveList.refresh();
                    }
                    enabled = shelveList.members().length > 0;
                } else if (select instanceof IP4ShelveFile) {
                    enabled = true;
                }
                if (enabled) {
                    break;
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
        final List<IP4Resource> resources = new ArrayList<IP4Resource>();
        if (this.getSelection() != null) {
            for (Object select : this.getSelection().toArray()) {
                if (select instanceof IP4Resource) {
                    resources.add((IP4Resource) select);
                }
            }
        }
        if (!resources.isEmpty()) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public String getTitle() {
                    return Messages.UnshelveAction_Unshelving;
                }

                @Override
                public void run(IProgressMonitor monitor) {
                    Map<IP4ShelvedChangelist, List<IP4Resource>> listMappings = new HashMap<IP4ShelvedChangelist, List<IP4Resource>>();
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4ShelveFile) {
                            IP4ShelvedChangelist list = ((IP4ShelveFile) resource)
                                    .getChangelist();
                            List<IP4Resource> listFiles = listMappings
                                    .get(list);
                            if (listFiles == null) {
                                listFiles = new ArrayList<IP4Resource>();
                                listMappings.put(list, listFiles);
                            }
                            listFiles.add(resource);
                        }
                    }
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4ShelvedChangelist) {
                            IP4ShelvedChangelist list = (IP4ShelvedChangelist) resource;
                            list.refresh();
                            listMappings.put(list,
                                    Arrays.asList(list.members()));
                        }
                    }

                    monitor.beginTask(Messages.UnshelveAction_Unshelving,
                            listMappings.size() + 1);
                    P4Collection refreshCollection = createCollection();
                    for (Map.Entry<IP4ShelvedChangelist, List<IP4Resource>> entry : listMappings.entrySet()) {
                    	IP4ShelvedChangelist list = entry.getKey();
                        List<IP4Resource> files = entry.getValue();
                        monitor.subTask(MessageFormat.format(
                                Messages.UnshelveAction_ChangelistNum,
                                list.getId()));
                        IFileSpec[] specs = unshelve(list,
                                files.toArray(new IP4Resource[files.size()]),
                                monitor);
                        P4Collection listCollection = P4Collection
                                .getValidCollection(list.getConnection(),
                                        Arrays.asList(specs),
                                        refreshCollection.getType());
                        refreshCollection.addAll(listCollection);
                        monitor.worked(1);
                    }

                    monitor.subTask(Messages.UnshelveAction_RefreshingUnshelvedFiles);
                    if (!refreshCollection.isEmpty()) {
                        refreshCollection.refresh();
                        refreshCollection
                                .refreshLocalResources(IResource.DEPTH_ONE);
                    }
                    monitor.worked(1);

                    monitor.done();
                }

            };
            runRunnable(runnable);

        }
    }

    private int createChangelist(IP4Connection connection, String description) {
        IP4PendingChangelist newPending = connection.createChangelist(
                description, null);
        return newPending != null ? newPending.getId() : -1;
    }

    private void revert(IP4Resource[] resources, IProgressMonitor monitor) {
        monitor.subTask(Messages.UnshelveAction_RevertingFiles);
        P4Collection collection = createCollection();
        for (IP4Resource resource : resources) {
            if (resource instanceof IP4File) {
                String path = resource.getRemotePath();
                if (path != null) {
                    resource = resource.getConnection().getFile(
                            resource.getRemotePath());
                    collection.add(resource);
                }
            }
        }
        if (!collection.isEmpty()) {
            collection.revert();
        }
    }

    private IFileSpec[] unshelve(final IP4ShelvedChangelist list,
            final IP4Resource[] files, IProgressMonitor monitor) {
        if (files != null && files.length > 0) {
            final int[] id = new int[] { -1 };
            final IP4Connection connection = list.getConnection();
            IP4PendingChangelist activeList = connection
                    .getActivePendingChangelist();
            if (activeList != null) {
                id[0] = activeList.getId();
            }
            final boolean[] active = new boolean[] { false };
            final boolean[] overwrite = new boolean[] { false };
            final boolean[] revert = new boolean[] { false };
            final String[] comment = new String[] { null };

            List<IP4Resource> p4Files = new ArrayList<IP4Resource>();
            for (IP4Resource resource : files) {
                if (resource instanceof IP4ShelveFile) {
                    resource = ((IP4ShelveFile) resource).getFile();
                }
                p4Files.add(resource);
            }

            final IP4Resource[][] selected = new IP4Resource[][] { p4Files
                    .toArray(new IP4File[p4Files.size()]), };
            if (id[0] < 0) {
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        UnshelveDialog dialog = new UnshelveDialog(
                                P4UIUtils.getDialogShell(),
                                selected[0],
                                connection,
                                list.getId(),
                                MessageFormat
                                        .format(Messages.UnshelveAction_UnshelvingFilesFromChangelist,
                                                list.getId()));
                        if (UnshelveDialog.OK == dialog.open()) {
                            id[0] = dialog.getSelectedChangeId();
                            active[0] = dialog.useSelected();
                            selected[0] = dialog.getSelectedFiles();
                            overwrite[0] = dialog.isForceWritable();
                            revert[0] = dialog.isRevert();
                            comment[0] = dialog.getDescription();
                        } else {
                            selected[0] = null;
                        }
                    }
                });
            }
            if (selected[0] != null) {
                if (revert[0]) {
                    revert(selected[0], monitor);
                }
                if (id[0] == IP4PendingChangelist.NEW) {
                    monitor.subTask(Messages.UnshelveAction_CreatingNewPendingChangelist);
                    id[0] = createChangelist(connection, comment[0]);
                }
                if (id[0] >= 0) {
                    if (active[0]) {
                        connection.setActivePendingChangelist(id[0]);
                    }
                    monitor.subTask(Messages.UnshelveAction_UnshelvingFiles);
                    return list.unshelve(selected[0], id[0], overwrite[0]);
                }
            }
        } else {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    P4ConnectionManager
                            .getManager()
                            .openInformation(
                                    P4UIUtils.getDialogShell(),
                                    Messages.UnshelveAction_NoShelvedFiles,
                                    MessageFormat
                                            .format(Messages.UnshelveAction_ChangelistContainsNoShelvedFiles,
                                                    list.getId()));
                }
            });
        }
        return new IFileSpec[0];
    }

}
