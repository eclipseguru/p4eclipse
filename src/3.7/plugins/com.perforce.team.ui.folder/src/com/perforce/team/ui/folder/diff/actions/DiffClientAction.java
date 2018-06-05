/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.diff.editor.FolderDiffEditor;
import com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput;
import com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffClientAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return getSingleConnectionSelection() != null;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final IP4Connection connection = getSingleConnectionSelection();
        if (connection != null) {
            new UIJob(Messages.DiffFoldersAction_OpeningEditor) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    compareClient(connection);
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }

    /**
     * Generate input
     * 
     * @param connection
     * @return non-null folder diff input
     */
    protected IFolderDiffInput generateInput(IP4Connection connection) {
        String action = connection.getRootSpec();
        FolderDiffInput input = new FolderDiffInput(connection);
        input.addPaths(action, action);
        return input;
    }

    /**
     * Compare client
     * 
     * @param connection
     */
    protected void compareClient(IP4Connection connection) {
        if (!connection.isOffline()) {
            IFolderDiffInput input = generateInput(connection);
            try {
                IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                        FolderDiffEditor.ID);
            } catch (PartInitException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }
}