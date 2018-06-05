/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.p4java.actions.P4Action;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DeleteShelveAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        return resource != null && !resource.isReadOnly();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4ShelvedChangelist && !resource.isReadOnly()) {
            final IP4ShelvedChangelist list = (IP4ShelvedChangelist) resource;
            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    IP4PendingChangelist pending = list.getConnection()
                            .getPendingChangelist(list.getId(), true);
                    if (pending != null) {
                        pending.deleteShelved();
                        if (pending.needsRefresh()) {
                            pending.refresh();
                        }
                    }
                }

                @Override
                public String getTitle() {
                    return MessageFormat
                            .format(Messages.DeleteShelveAction_DeletingShelvedChangelist,
                                    list.getId());
                }

            };
            runRunnable(runnable);
        }
    }

}
