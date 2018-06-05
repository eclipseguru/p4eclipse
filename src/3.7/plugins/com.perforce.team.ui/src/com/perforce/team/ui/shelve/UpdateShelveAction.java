/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UpdateShelveAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        return resource instanceof IP4ShelvedChangelist
                && !((IP4ShelvedChangelist) resource).isReadOnly();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4ShelvedChangelist) {
            final IP4ShelvedChangelist list = (IP4ShelvedChangelist) resource;
            if (!list.isReadOnly()) {
                IP4Runnable runnable = new P4Runnable() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        IP4PendingChangelist pending = list.getConnection()
                                .getPendingChangelist(list.getId(), true);
                        ShelveChangelistAction action = new ShelveChangelistAction();
                        action.setAsync(isAsync());
                        action.shelve(pending);
                    }

                };
                runRunnable(runnable);
            }
        }
    }

}
