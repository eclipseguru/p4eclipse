/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4File;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ScheduleResolveAction extends BaseResolveAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        SyncAction action = new SyncAction();
        action.setAsync(isAsync());
        action.setCollection(getResourceSelection());
        action.runAction();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.BaseResolveAction#enableFor(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected boolean enableFor(IP4File file) {
        return !file.isUnresolved();
    }

}
