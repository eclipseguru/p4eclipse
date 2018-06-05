/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DeleteChangelistAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        P4Collection collection = getResourceSelection();
        if (!collection.isEmpty()) {
            final IP4Resource[] resources = collection.members();
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    List<IP4PendingChangelist> pendings = new ArrayList<IP4PendingChangelist>();
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4PendingChangelist) {
                            pendings.add((IP4PendingChangelist) resource);
                        }
                    }
                    monitor.beginTask(
                            Messages.DeleteChangelistAction_DeletingChangelistTask,
                            pendings.size());
                    List<IP4PendingChangelist> deleted = new ArrayList<IP4PendingChangelist>();
                    for (final IP4PendingChangelist list : pendings) {
                        monitor.subTask(Integer.toString(list.getId()));
                        if (list.needsRefresh()) {
                            list.refresh();
                        }
                        final boolean[] delete = new boolean[] { true };
                        if (list.isShelved()) {
                            PerforceUIPlugin.syncExec(new Runnable() {

                                public void run() {
                                    delete[0] = P4ConnectionManager
                                            .getManager()
                                            .openQuestion(
                                                    P4UIUtils.getDialogShell(),
                                                    Messages.DeleteChangelistAction_ChangeContainsShelvedFilesTitle,
                                                    MessageFormat
                                                            .format(Messages.DeleteChangelistAction_ChangeContainsShelvedFilesMessage,
                                                                    list.getId()));
                                }
                            });
                            if (delete[0]) {
                                list.deleteShelved();
                            }
                        }
                        if (delete[0]) {
                            list.delete();
                        }
                        monitor.worked(1);
                        if (list.getStatus() == null
                                && list.getChangelist() == null) {
                            deleted.add(list);
                        }
                    }
                    monitor.done();
                    if (deleted.size() > 0) {
                        P4ConnectionManager
                                .getManager()
                                .notifyListeners(
                                        new P4Event(
                                                EventType.DELETE_CHANGELIST,
                                                deleted.toArray(new IP4PendingChangelist[0])));
                    }
                }

                @Override
                public String getTitle() {
                    return MessageFormat
                            .format(Messages.DeleteChangelistAction_DeletingChangelists,
                                    resources.length);
                }

            };
            runRunnable(runnable);
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        P4Collection collection = getResourceSelection();
        IP4Resource[] resources = collection.members();
        boolean enabled = false;
        for (IP4Resource resource : resources) {
            if (resource instanceof IP4PendingChangelist) {
                IP4PendingChangelist list = (IP4PendingChangelist) resource;
                if (list.needsRefresh()) {
                    list.refresh();
                }
                enabled = list.isDeleteable();
            } else {
                enabled = false;
            }
            if (!enabled) {
                break;
            }
        }
        return enabled;
    }

}
