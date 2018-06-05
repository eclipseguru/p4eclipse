/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseResolveAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = false;
        if (containsOnlineConnection()) {
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = getResourceSelection();
                for (IP4Resource resource : collection.members()) {
                    if (resource.isContainer()) {
                        enabled = true;
                        break;
                    } else if (resource instanceof IP4File) {
                        IP4File file = (IP4File) resource;
                        if (enableFor(file)) {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

    /**
     * Returns true if the action should be enabled for the current file
     * 
     * @param file
     * @return - true to enable the action, false to disable
     */
    protected boolean enableFor(IP4File file) {
        return file.isUnresolved();
    }

}
