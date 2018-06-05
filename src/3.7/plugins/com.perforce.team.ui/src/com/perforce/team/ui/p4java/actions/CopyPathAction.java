/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4UIUtils;

import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CopyPathAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return getSingleResourceSelection() != null;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleResourceSelection();
        if (resource != null) {
            String path = resource.getRemotePath();
            if (path != null) {
                P4UIUtils.copyToClipboard(path);
            }
        }
    }

}
