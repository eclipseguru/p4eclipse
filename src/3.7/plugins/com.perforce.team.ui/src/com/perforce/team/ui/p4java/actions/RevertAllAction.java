/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.dialogs.ConfirmRevertDialog;

import java.util.List;

import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevertAllAction extends RevertAction {

    /**
     * Create dialog
     * 
     * @param files
     * @return confirm revert dialog
     */
    @Override
    protected ConfirmRevertDialog createDialog(List<IP4File> files) {
        return new ConfirmRevertDialog(getShell(),
                files.toArray(new IP4File[files.size()]), true);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.RevertAction#getCollection(com.perforce.team.core.p4java.P4Collection)
     */
    @Override
    protected P4Collection getCollection(P4Collection collection) {
        P4Collection changelistCollection = createCollection();
        for (IP4Resource resource : collection.members()) {
            if (resource instanceof IP4PendingChangelist) {
                IP4PendingChangelist list = (IP4PendingChangelist) resource;
                if (list.needsRefresh()) {
                    list.refresh();
                }
                changelistCollection.add(list);
            }
        }
        return changelistCollection;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        P4Collection collection = super.getResourceSelection();
        IP4Resource[] resources = collection.members();
        int size = resources.length;
        boolean enabled = false;
        if (size > 0) {
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4PendingChangelist) {
                    if (((IP4PendingChangelist) resource).isOnClient()) {
                        enabled = true;
                        break;
                    }
                }
            }
        }
        return enabled;
    }

}
