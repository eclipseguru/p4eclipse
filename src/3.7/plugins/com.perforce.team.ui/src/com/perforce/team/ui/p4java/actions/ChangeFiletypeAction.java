/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.dialogs.FileTypeDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangeFiletypeAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final P4Collection collection = super.getDirectFileSelection();
        IP4Resource[] resources = collection.members();
        int size = resources.length;
        if (size > 0) {
            IP4File first = (IP4File) resources[0];
            String type = first.getOpenedType();
            if (type == null) {
                type = ""; //$NON-NLS-1$
            }
            FileTypeDialog dlg = new FileTypeDialog(getShell(), type);
            if (dlg.open() == Window.OK) {
                changeType(collection, dlg.getFileType());
            }
        }
    }

    /**
     * Changes the type of the collection returned from
     * {@link #getDirectFileSelection()} to the newType specified
     * 
     * @param newType
     */
    public void changeType(final String newType) {
        P4Collection collection = super.getDirectFileSelection();
        if (!collection.isEmpty()) {
            changeType(collection, newType);
        }
    }

    /**
     * Change the type of the files in the collection to the specified new type
     * 
     * @param collection
     * @param newType
     */
    public void changeType(final P4Collection collection, final String newType) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 1);
                monitor.subTask(generateTitle(null, collection));
                collection.changeType(newType);
                monitor.worked(1);
                monitor.done();
            }

            @Override
            public String getTitle() {
                return Messages.ChangeFiletypeAction_ChangingFileType;
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
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = super.getResourceSelection();
                IP4Resource[] resources = collection.members();
                int size = resources.length;
                if (size > 0) {
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            if (file.isOpened()) {
                                enabled = true;
                                break;
                            }
                        } else {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

}
