/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RefreshAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        return containsOnlineConnection();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        final P4Collection collection = getResourceSelection();
        if (!collection.isEmpty()) {
            refresh(collection);
        }
    }

    private void refresh(final P4Collection collection) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.RefreshAction_RefreshingPerforceResources;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 2);
                collection.refresh(IResource.DEPTH_INFINITE);
                monitor.worked(1);
                collection.refreshLocalResources(IResource.DEPTH_INFINITE);
                monitor.worked(1);
                updateActionState();
                monitor.done();
            }

        };
        runRunnable(runnable);
    }

}
