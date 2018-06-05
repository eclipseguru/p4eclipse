/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4ShelvedChangelist;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;
import com.perforce.team.ui.shelve.UpdateShelveDialog.Option;
import com.perforce.team.ui.shelve.UpdateShelveDialog.ShelveChange;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelveChangelistAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        boolean enabled = false;
        if (resource instanceof IP4PendingChangelist) {
            IP4PendingChangelist list = (IP4PendingChangelist) resource;
            enabled = list.isOnClient()
                    && list.getConnection().isShelvingSupported();
        }
        return enabled;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4PendingChangelist) {
            shelve((IP4PendingChangelist) resource);
        }
    }

    private void shelveDefault(final IP4PendingChangelist list) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                list.refresh();
                ShelveAction action = new ShelveAction();
                action.selectionChanged(null,
                        new StructuredSelection(list.members()));
                action.setAsync(isAsync());
                action.run(null);
            }

        };
        runRunnable(runnable);
    }

    /**
     * Update the shelved changelist
     * 
     * @param list
     * @param selected
     */
    public void updateShelveNumbered(final IP4PendingChangelist list,
            final IP4Resource[] selected) {
        updateShelveNumbered(list, selected, Option.UPDATE);
    }

    /**
     * Update the shelved changelist
     * 
     * @param list
     * @param selected
     * @param option
     */
    public void updateShelveNumbered(final IP4PendingChangelist list,
            final IP4Resource[] selected, final Option option) {
        final IP4ShelvedChangelist shelvedList = new P4ShelvedChangelist(
                list.getConnection(), list.getChangelist(), true);
        shelvedList.refresh();
        final IP4Resource[] shelvedFiles = shelvedList.members();
        final IP4Resource[] pendingFiles = list.members();
        final ShelveChange[][] changes = new ShelveChange[][] { null };
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                UpdateShelveDialog dialog = new UpdateShelveDialog(P4UIUtils
                        .getDialogShell(), list, selected, shelvedFiles,
                        pendingFiles, option);
                if (UpdateShelveDialog.OK == dialog.open()) {
                    changes[0] = dialog.getChanges();
                }
            }
        });
        if (changes[0] != null && changes[0].length > 0) {
            ShelveChange[] shelveUpdates = changes[0];
            if (shelveUpdates.length > 0) {
                if (shelveUpdates[0].option == Option.REPLACE) {
                    list.replaceShelvedFiles();
                } else {
                    List<IP4File> deletes = new ArrayList<IP4File>();
                    List<IP4File> updates = new ArrayList<IP4File>();
                    for (ShelveChange change : shelveUpdates) {
                        if (change.option == Option.DELETE) {
                            deletes.add(change.file);
                        } else if (change.option == Option.ADD
                                || change.option == Option.UPDATE) {
                            updates.add(change.file);
                        }
                    }
                    if (deletes.size() > 0) {
                        list.deleteShelve(deletes
                                .toArray(new IP4Resource[deletes.size()]));
                    }
                    if (updates.size() > 0) {
                        list.updateShelvedFiles(updates
                                .toArray(new IP4Resource[updates.size()]));
                    }
                }
            }
        }
    }

    private void shelveNumbered(final IP4PendingChangelist list) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                list.refresh();
                if (list.isShelved()) {
                    updateShelveNumbered(list, null);
                } else {
                    ShelveAction action = new ShelveAction();
                    action.selectionChanged(null,
                            new StructuredSelection(list.members()));
                    action.setAsync(isAsync());
                    action.run(null);
                }
            }

            @Override
            public String getTitle() {
                return MessageFormat
                        .format(Messages.ShelveChangelistAction_UpdatingShelvedChangelist,
                                list.getId());
            }

        };
        runRunnable(runnable);
    }

    /**
     * Shelve the specified changelist
     * 
     * @param list
     */
    public void shelve(IP4PendingChangelist list) {
        if (list != null) {
            if (list.isDefault()) {
                shelveDefault(list);
            } else {
                shelveNumbered(list);
            }
        }
    }
}
