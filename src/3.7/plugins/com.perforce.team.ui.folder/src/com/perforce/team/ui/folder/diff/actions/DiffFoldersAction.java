/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.diff.editor.FolderDiffEditor;
import com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput;
import com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffFoldersAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        P4Collection collection = getResourceSelection();
        return collection.size() == 1 || collection.size() == 2;
    }

    /**
     * Get folders to include in diff
     * 
     * @return array of folders or null if invalid selection
     */
    protected IP4Folder[] getFolders() {
        IP4Resource[] resources = getResourceSelection().members();
        if (resources.length == 1 && resources[0] instanceof IP4Folder) {
            IP4Folder folder = (IP4Folder) resources[0];
            return new IP4Folder[] { folder, folder };
        } else if (resources.length == 2
                && resources[0] instanceof IP4Folder
                && resources[1] instanceof IP4Folder
                && resources[0].getConnection().equals(
                        resources[1].getConnection())) {
            return new IP4Folder[] { (IP4Folder) resources[0],
                    (IP4Folder) resources[1] };
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final IP4Folder[] folders = getFolders();
        if (folders != null) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    compareFolders(folders[0], folders[1]);
                }

                @Override
                public String getTitle() {
                    return Messages.DiffFoldersAction_OpeningEditor;
                }
            };
            runRunnable(runnable);
        }
    }

    /**
     * Generate input
     * 
     * @param folder1
     * @param folder2
     * @return non-null folder diff input
     */
    protected IFolderDiffInput generateInput(IP4Folder folder1,
            IP4Folder folder2) {
        String action1 = folder1.getActionPath();
        String action2 = folder2.getActionPath();
        if (action1.compareToIgnoreCase(action2) > 0) {
            String temp = action1;
            action1 = action2;
            action2 = temp;
        }
        FolderDiffInput input = new FolderDiffInput(folder1.getConnection());
        input.addPaths(action1, action2);
        return input;
    }

    /**
     * Compare folders
     * 
     * @param folder1
     * @param folder2
     */
    protected void compareFolders(IP4Folder folder1, IP4Folder folder2) {
        if (!folder1.getConnection().isOffline()) {
            final IFolderDiffInput input = generateInput(folder1, folder2);
            if (input != null) {
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        try {
                            IDE.openEditor(PerforceUIPlugin.getActivePage(),
                                    input, FolderDiffEditor.ID);
                        } catch (PartInitException e) {
                            PerforceProviderPlugin.logError(e);
                        }
                    }
                });
            }
        }
    }
}
