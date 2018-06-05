/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.editor.CompareUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DepotDiffPreviousAction extends P4Action {

    private static void showDiffDeleted() {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                MessageDialog.openError(P4UIUtils.getShell(),
                        Messages.DepotDiffPreviousAction_CantDiffDeletedTitle,
                        Messages.DepotDiffPreviousAction_CantDiffDeletedMessage);
            }
        });
    }

    private static void showDiffAdded() {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                MessageDialog.openError(P4UIUtils.getShell(),
                        Messages.DepotDiffPreviousAction_CantDiffAddedTitle,
                        Messages.DepotDiffPreviousAction_CantDiffAddedMessage);
            }
        });
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleResourceSelection();
        if (resource instanceof IP4SubmittedFile) {
            final IP4File file = ((IP4SubmittedFile) resource).getFile();
            if (file != null) {
                IP4Runnable runnable = new P4Runnable() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        IFileSpec spec = file.getP4JFile();
                        if (spec != null) {
                            if (file.openedForDelete()) {
                                showDiffDeleted();
                            } else {
                                String depot = file.getRemotePath();
                                int end = spec.getEndRevision();
                                if (end > 1) {
                                    CompareUtils.doCompare(file, depot, depot,
                                            end, end - 1);
                                } else {
                                    showDiffAdded();
                                }
                            }
                        }
                    }

                    @Override
                    public String getTitle() {
                        return Messages.DepotDiffPreviousAction_GeneratingDiff;
                    }
                };
                runRunnable(runnable);
            }
        }
    }
}
