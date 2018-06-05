/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.p4java.dialogs.MoveChangeDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MoveToAnotherChangelistAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final P4Collection collection = getFileSelection();
        IP4Resource[] members = collection.members();
        if (members.length > 0) {
            IP4Connection connection = members[0].getConnection();
            MoveChangeDialog dialog = new MoveChangeDialog(getShell(),
                    connection.getCachedPendingChangelists());
            if (dialog.open() == MoveChangeDialog.OK) {
                int newChangelist = dialog.getSelectedChange();
                String description = dialog.getDescription();
                move(collection, newChangelist, description, connection);
            }
        }
    }

    private void move(final P4Collection collection, final int changelist,
            final String description, final IP4Connection connection) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                IP4PendingChangelist list = null;
                int work = changelist == IP4PendingChangelist.NEW ? 2 : 1;
                monitor.beginTask(getTitle(), work);
                if (changelist == IP4PendingChangelist.NEW) {
                    monitor.subTask(Messages.MoveToAnotherChangelistAction_CreatingNewChangelist);
                    list = connection.createChangelist(description, null);
                    monitor.worked(1);
                } else {
                    list = connection.getPendingChangelist(changelist, true);
                }
                if (list != null) {
                    if (list.getId() == 0) {
                        monitor.subTask(Messages.MoveToAnotherChangelistAction_DefaultChangelist);
                    } else {
                        monitor.subTask(MessageFormat
                                .format(Messages.MoveToAnotherChangelistAction_ChangelistNum,
                                        list.getId()));
                    }
                    collection.reopen(list);
                    monitor.worked(1);
                }
                monitor.done();
            }

            @Override
            public String getTitle() {
                return Messages.MoveToAnotherChangelistAction_ReopeningPerforceResources;
            }

        };
        runRunnable(runnable);

    }

    /**
     * Moves the resource in the collection returned from
     * {@link #getFileSelection()} to the changelist specified
     * 
     * @param changelist
     */
    public void move(final IP4PendingChangelist changelist) {
        P4Collection collection = getFileSelection();
        if (!collection.isEmpty()) {
            move(collection, changelist);
        }
    }

    /**
     * Moves the resource in the specified collection to the changelist
     * specified
     * 
     * @param collection
     * @param changelist
     */
    public void move(final P4Collection collection,
            final IP4PendingChangelist changelist) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                collection.reopen(changelist);
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = false;
        if (containsOnlineConnection()) {
            P4Collection collection = getResourceSelection();
            for (IP4Resource resource : collection.members()) {
                if (resource instanceof IP4File) {
                    IP4File file = (IP4File) resource;
                    if (file.isOpened()) {
                        enabled = true;
                        break;
                    }
                }
            }
        }
        return enabled;
    }

}
