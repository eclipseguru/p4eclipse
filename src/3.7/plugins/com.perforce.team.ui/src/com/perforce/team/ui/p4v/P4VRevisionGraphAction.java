/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4v;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4VRevisionGraphAction extends P4VAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        runApplication();
    }

    /**
     * Run the p4v time lapse view for each file in the selection
     * 
     * @return - array of running p4v time lapse view application wrappers
     */
    public RevisionGraphRunner[] runApplication() {
        P4Collection collection = super.getDirectFileSelection();
        List<RevisionGraphRunner> runners = new ArrayList<RevisionGraphRunner>();
        if (!collection.isEmpty()) {
            IP4Resource[] resources = collection.members();
            for (IP4Resource resource : resources) {
                IP4File file = (IP4File) resource;
                if (enableFor(file)) {
                    String path = file.getActionPath(IP4Resource.Type.REMOTE);
                    RevisionGraphRunner runner = new RevisionGraphRunner(
                            file.getConnection(), path);
                    runner.run();
                    runners.add(runner);
                }
            }
        }
        return runners.toArray(new RevisionGraphRunner[0]);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = false;
        if (containsOnlineConnection()) {
            P4Collection collection = super.getDirectFileSelection();
            if (windows) {
                enabled = isEnabledWindows(collection, IP4Resource.Type.REMOTE);
            } else {
                IP4Resource[] resources = collection.members();
                int size = resources.length;
                if (size > 0) {
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            if (enableFor(file)) {
                                enabled = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return enabled;
    }

    /**
     * @see com.perforce.team.ui.p4v.P4VAction#enableFor(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected boolean enableFor(IP4File file) {
        return revisionExists(file);
    }

}
