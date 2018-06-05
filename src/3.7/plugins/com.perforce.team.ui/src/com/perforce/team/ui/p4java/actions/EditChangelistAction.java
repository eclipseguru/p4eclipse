/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.dialogs.ChangeSpecDialog;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditChangelistAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        P4Collection collection = getResourceSelection();
        IP4Resource[] members = collection.members();
        if (members.length == 1 && members[0] instanceof IP4PendingChangelist) {
            final IP4PendingChangelist list = (IP4PendingChangelist) members[0];
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    list.refresh();
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            final ChangeSpecDialog dialog = new ChangeSpecDialog(
                                    list, null, getShell(), false);
                            if (dialog.open() == Window.OK) {
                                IP4PendingChangelist defaultChangelist = list
                                        .getConnection()
                                        .getPendingChangelist(0);
                                String description = dialog.getDescription();
                                IP4File[] uncheckedFiles = dialog
                                        .getUncheckedFiles();
                                IP4Job[] checkedJobs = dialog.getCheckedJobs();
                                IP4Job[] uncheckedJobs = dialog
                                        .getUncheckedJobs();
                                edit(uncheckedFiles, uncheckedJobs,
                                        checkedJobs, description, list,
                                        defaultChangelist);
                            }
                        }
                    });
                }

                @Override
                public String getTitle() {
                    int id = list.getId();
                    if (id != 0) {
                        return MessageFormat
                                .format(Messages.EditChangelistAction_NumberedTitle,
                                        id);
                    } else {
                        return Messages.EditChangelistAction_DefaultTitle;
                    }
                }

            };
            runRunnable(runnable);
        }
    }

    /**
     * Edit the changelist with files to be remove and jobs to be added and
     * removed
     * 
     * @param uncheckedFiles
     * @param uncheckedJobs
     * @param checkedJobs
     * @param description
     * @param list
     * @param defaultChangelist
     */
    public void edit(final IP4File[] uncheckedFiles,
            final IP4Job[] uncheckedJobs, final IP4Job[] checkedJobs,
            final String description, final IP4PendingChangelist list,
            final IP4PendingChangelist defaultChangelist) {
        if (list == null) {
            return;
        }
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                if (description != null) {
                    list.updateServerDescription(description);
                }
                if (uncheckedFiles != null && uncheckedFiles.length > 0) {
                    createCollection(uncheckedFiles).reopen(defaultChangelist);
                }
                if (uncheckedJobs != null && uncheckedJobs.length > 0) {
                    createCollection(uncheckedJobs).unfix(list);
                }
                if (checkedJobs != null && checkedJobs.length > 0) {
                    createCollection(checkedJobs).fix(list);
                }
            }

            /**
             * @see com.perforce.team.core.p4java.P4Runnable#getTitle()
             */
            @Override
            public String getTitle() {
                int id = list.getId();
                if (id != 0) {
                    return MessageFormat.format(
                            Messages.EditChangelistAction_NumberedTitle, id);
                } else {
                    return Messages.EditChangelistAction_DefaultTitle;
                }
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
        P4Collection collection = getResourceSelection();
        IP4Resource[] members = collection.members();
        if (members.length == 1 && members[0] instanceof IP4PendingChangelist) {
            IP4PendingChangelist list = (IP4PendingChangelist) members[0];
            enabled = !list.isDefault() && !list.isReadOnly()
                    && list.isOnClient();
        }
        return enabled;
    }

}
