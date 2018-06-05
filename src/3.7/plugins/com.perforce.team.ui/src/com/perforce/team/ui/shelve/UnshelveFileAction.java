/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UnshelveFileAction extends BaseShelveAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = false;
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4File) {
            enabled = resource.getConnection().isShelvingSupported();
        }
        return enabled;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final IP4File file = getSingleFileSelection();
        if (file != null) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    if (file.getConnection().isShelvingSupported()) {
                        final IP4ShelveFile[] shelves = file
                                .getShelvedVersions();
                        PerforceUIPlugin.syncExec(new Runnable() {

                            public void run() {
                                if (shelves.length > 0) {
                                    FileShelfDialog dialog = new FileShelfDialog(
                                            P4UIUtils.getDialogShell(), file,
                                            shelves);
                                    if (FileShelfDialog.OK == dialog.open()) {
                                        unshelve(file, dialog.getSelected(),
                                                dialog.revert(),
                                                dialog.overwrite(),
                                                dialog.getChangelist(),
                                                dialog.getDescription());
                                    }
                                } else {
                                    showNoVersions(file);
                                }
                            }
                        });
                    } else {
                        ShelveAction.showNotSupported(file.getConnection());
                    }
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

    private void unshelve(final IP4File file, final IP4ShelveFile shelveFile,
            final boolean revert, final boolean overwrite,
            final int toChangelist, final String description) {
        if (file != null && shelveFile != null) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public String getTitle() {
                    return Messages.UnshelveFileAction_UnshelvingFile;
                }

                @Override
                public void run(IProgressMonitor monitor) {
                    int work = 2;
                    if (revert) {
                        work++;
                    }
                    if (toChangelist == IP4PendingChangelist.NEW) {
                        work++;
                    }
                    P4Collection collection = createCollection();
                    monitor.beginTask(MessageFormat.format(
                            Messages.UnshelveFileAction_Unshelving,
                            file.getActionPath()), work);

                    if (revert) {
                        monitor.subTask(MessageFormat.format(
                                Messages.UnshelveFileAction_Reverting,
                                file.getActionPath()));
                        createCollection(new IP4Resource[] { file }).revert();
                        monitor.worked(1);
                    }

                    int changelist = toChangelist;

                    if (changelist == IP4PendingChangelist.NEW) {
                        monitor.subTask(Messages.UnshelveFileAction_CreatingNewChangelist);
                        changelist = createChangelist(file.getConnection(),
                                description);
                        monitor.worked(1);
                    }

                    monitor.subTask(MessageFormat.format(
                            Messages.UnshelveFileAction_Unshelving,
                            file.getActionPath()));
                    IFileSpec[] specs = shelveFile.unshelve(changelist,
                            overwrite);
                    collection.add(P4Collection.getValidCollection(
                            file.getConnection(), Arrays.asList(specs),
                            collection.getType()));
                    monitor.worked(1);

                    // Refresh unshelved file
                    monitor.subTask(MessageFormat
                            .format(Messages.UnshelveFileAction_RefreshingUnshelvedFile,
                                    file.getActionPath()));
                    if (!collection.isEmpty()) {
                        collection.refresh();
                        collection.refreshLocalResources(IResource.DEPTH_ONE);
                    }
                    monitor.worked(1);

                    monitor.done();
                }

            };
            runRunnable(runnable);
        }
    }
}
