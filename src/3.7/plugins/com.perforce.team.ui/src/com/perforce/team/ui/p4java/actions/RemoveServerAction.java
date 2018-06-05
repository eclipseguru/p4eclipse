/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RemoveServerAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final P4Collection collection = getFileSelection();
        if (!collection.isEmpty()) {
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    IP4Resource[] resources = collection.members();
                    if (resources != null && resources.length > 0) {
                        for (IP4Resource resource : resources) {
                            P4ConnectionManager.getManager().removeConnection(
                                    resource.getConnection());
                        }
                    }
                    updateActionState();
                }

            };
            runRunnable(runnable);
        }
    }

}
