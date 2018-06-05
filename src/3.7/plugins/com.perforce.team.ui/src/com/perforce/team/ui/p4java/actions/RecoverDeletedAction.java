/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RecoverDeletedAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final P4Collection collection = getResourceSelection();
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                for (IP4Resource resource : collection.members()) {
                    if (resource instanceof IP4File) {
                        IP4File p4File = (IP4File) resource;
                        if (canRecover(p4File)) {
                            if (p4File.getHaveRevision() == 0
                                    && p4File.getHeadRevision() > 1) {
                                int previous = p4File.getHeadRevision() - 1;
                                SyncRevisionAction sync = new SyncRevisionAction();
                                sync.setAsync(false);
                                sync.selectionChanged(null,
                                        new StructuredSelection(p4File));
                                sync.runAction("#" + previous); //$NON-NLS-1$
                            }
                        }
                    }
                    updateActionState();
                }
            }

        };
        runRunnable(runnable);
    }

    private boolean canRecover(IP4File file) {
        return file != null && !file.isOpened() && file.isHeadActionDelete();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        boolean enabled = false;
        if (containsOnlineConnection()) {
            P4Collection collection = getResourceSelection();
            for (IP4Resource resource : collection.members()) {
                if (resource instanceof IP4File) {
                    if (canRecover((IP4File) resource)) {
                        enabled = true;
                        break;
                    }
                }
            }
        }
        return enabled;
    }

}
