/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4ShelvedChangelist;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;
import com.perforce.team.ui.shelve.UpdateShelveDialog.Option;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DeleteShelveFileAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = false;
        final IStructuredSelection fileSelection = this.getSelection();
        enabled = fileSelection != null && !fileSelection.isEmpty();
        if (enabled) {
            for (Object resource : fileSelection.toArray()) {
                if (resource instanceof IP4ShelveFile) {
                    if (((IP4ShelveFile) resource).isReadOnly()) {
                        enabled = false;
                        break;
                    }
                } else if (resource instanceof IP4File) {
                    if (((IP4File) resource).isReadOnly()) {
                        enabled = false;
                        break;
                    }
                } else {
                    enabled = false;
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
        final IStructuredSelection fileSelection = this.getSelection();
        if (fileSelection != null && !fileSelection.isEmpty()) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    Map<IP4PendingChangelist, List<IP4File>> listMappings = new HashMap<IP4PendingChangelist, List<IP4File>>();
                    for (Object resource : fileSelection.toArray()) {
                        if (resource instanceof IP4ShelveFile) {
                            resource = ((IP4ShelveFile) resource).getFile();
                        }
                        if (resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            IP4PendingChangelist list = file
                                    .getChangelist(true);
                            if (list != null && !list.isReadOnly()) {
                                List<IP4File> listFiles = listMappings
                                        .get(list);
                                if (listFiles == null) {
                                    listFiles = new ArrayList<IP4File>();
                                    listMappings.put(list, listFiles);
                                }
                                listFiles.add(file);
                            }
                        }
                    }
                    if (!listMappings.isEmpty()) {
                        for (Map.Entry<IP4PendingChangelist, List<IP4File>> entry : listMappings.entrySet()) {
                        	final IP4PendingChangelist list=entry.getKey();
                        	final List<IP4File> listFiles = entry.getValue();
                            final IP4ShelvedChangelist shelved = new P4ShelvedChangelist(
                                    list.getConnection(), list.getChangelist(),
                                    list.isReadOnly());
                            shelved.refresh();
                            final List<IP4Resource> allFiles = new ArrayList<IP4Resource>();
                            for (IP4Resource file : shelved.members()) {
                                if (file instanceof IP4ShelveFile) {
                                    allFiles.add(((IP4ShelveFile) file)
                                            .getFile());
                                }
                            }
                            PerforceUIPlugin.syncExec(new Runnable() {

                                public void run() {
                                    ShelveFileDialog dialog = new ShelveFileDialog(
                                            P4UIUtils.getDialogShell(),
                                            list,
                                            allFiles.toArray(new IP4Resource[allFiles
                                                    .size()]),
                                            listFiles
                                                    .toArray(new IP4Resource[listFiles
                                                            .size()]),
                                            Option.DELETE);
                                    if (ShelveFileDialog.OK == dialog.open()) {
                                        shelveDelete(list,
                                                dialog.getSelectedFiles());
                                    }
                                }
                            });
                        }
                    }
                }

            };
            runRunnable(runnable);
        }
    }

    private void shelveDelete(final IP4PendingChangelist pending,
            final IP4Resource[] files) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(
                        generateTitle(
                                Messages.DeleteShelveFileAction_DeletingShelvedFiles,
                                files, IP4Resource.Type.REMOTE), 1);
                if (pending != null) {
                    pending.deleteShelve(files);
                    monitor.worked(1);
                }
                monitor.done();
            }

            @Override
            public String getTitle() {
                return MessageFormat
                        .format(Messages.DeleteShelveFileAction_DeletingShelvedFilesFromChangelist,
                                pending.getId());
            }

        };
        runRunnable(runnable);
    }
}
