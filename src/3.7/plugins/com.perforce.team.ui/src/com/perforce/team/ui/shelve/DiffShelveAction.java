/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.editor.CompareUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DiffShelveAction extends BaseShelveAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4File) {
            final IP4File file = (IP4File) resource;
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    if (file.getConnection().isShelvingSupported()) {
                        final IP4ShelveFile[] shelves = file
                                .getShelvedVersions();
                        PerforceUIPlugin.syncExec(new Runnable() {

                            public void run() {
                                if (shelves.length > 0) {
                                    DiffShelveDialog dialog = new DiffShelveDialog(
                                            P4UIUtils.getDialogShell(), file,
                                            shelves);
                                    if (DiffShelveDialog.OK == dialog.open()) {
                                        compare(file, dialog.getSelected());
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

    /**
     * Open the compare editor for a file and a shelve file
     * 
     * @param file
     * @param shelveFile
     */
    public void compare(final IP4File file, final IP4ShelveFile shelveFile) {
        if (file != null && shelveFile != null) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    File shelveLocalFile = P4CoreUtils.createFile(shelveFile
                            .getRemoteContents());
                    CompareUtils.openLocalCompare(file, shelveLocalFile,
                            MessageFormat.format(
                                    Messages.DiffShelveAction_ShelvedFile,
                                    shelveFile.getName()
                                            + shelveFile.getRevision()));
                }

                @Override
                public String getTitle() {
                    return Messages.DiffShelveAction_OpeningCompareEditor;
                }

            };
            runRunnable(runnable);
        }
    }

    /**
     * Open the compare editor for a revision of file and a shelve file
     * 
     * @param file
     * @param revision
     * @param shelveFile
     */
    public void compareRevision(final IP4File file, final int revision,
            final IP4ShelveFile shelveFile) {
        if (file != null && shelveFile != null) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    File shelveLocalFile = P4CoreUtils.createFile(shelveFile
                            .getRemoteContents());
                    CompareUtils.openLocalCompare(file, revision,
                            shelveLocalFile, MessageFormat.format(
                                    Messages.DiffShelveAction_ShelvedFile,
                                    file.getName() + shelveFile.getRevision()));
                }

                @Override
                public String getTitle() {
                    return Messages.DiffShelveAction_OpeningCompareEditor;
                }

            };
            runRunnable(runnable);
        }
    }

}
