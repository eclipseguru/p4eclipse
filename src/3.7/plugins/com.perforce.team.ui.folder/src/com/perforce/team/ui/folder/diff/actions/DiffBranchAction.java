/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.diff.editor.FolderDiffEditor;
import com.perforce.team.ui.folder.diff.editor.input.BranchDiffInput;
import com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffBranchAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4Branch) {
            final IP4Branch branch = (IP4Branch) resource;
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public String getTitle() {
                    return Messages.DiffBranchAction_OpeningBranchDiff;
                }

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask("", 1); //$NON-NLS-1$
                    monitor.subTask(Messages.DiffBranchAction_OpeningDiffEditor);
                    new UIJob(Messages.DiffBranchAction_OpeningDiffEditor) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            IFolderDiffInput input = generateInput(branch);
                            try {
                                IDE.openEditor(
                                        PerforceUIPlugin.getActivePage(),
                                        input, FolderDiffEditor.ID);
                            } catch (PartInitException e) {
                                PerforceProviderPlugin.logError(e);
                            }
                            return Status.OK_STATUS;
                        }
                    }.schedule();
                    monitor.done();
                }

            };
            runRunnable(runnable);
        }
    }

    /**
     * Generate input object for editor being opened
     * 
     * @param branch
     * @return non-null folder diff input
     */
    protected IFolderDiffInput generateInput(IP4Branch branch) {
        return new BranchDiffInput(branch.getName(), branch.getConnection());
    }
}
