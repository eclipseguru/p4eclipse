/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4merge;

import java.util.ArrayList;
import java.util.List;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.p4java.actions.P4Action;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4MergeDiffAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        runApplication();
    }

    /**
     * Run the p4 merge for each file in the selection
     * 
     * @return - array of running p4 merge application wrappers
     */
    public DiffRunner[] runApplication() {
        P4Collection collection = super.getDirectFileSelection();
        List<DiffRunner> runners = new ArrayList<DiffRunner>();
        if (!collection.isEmpty()) {
            IP4Resource[] resources = collection.members();
            for (IP4Resource resource : resources) {
                IP4File file = (IP4File) resource;
                file.refresh();
                if (file.getHeadRevision() > 0) {
                    String depotPath = file.getRemotePath();
                    depotPath += "#" + file.getHeadRevision(); //$NON-NLS-1$
                    String localPath = file.getLocalPath();
                    DiffRunner runner = new DiffRunner(file.getConnection(),
                            true, false, depotPath, depotPath, localPath
                                    + Messages.P4MergeDiffAction_WorkspaceFile,
                            localPath);
                    runner.run();
                    runners.add(runner);
                }
            }
        }
        return runners.toArray(new DiffRunner[0]);
    }

    /**
     * Diff against previous revision
     * 
     * @return - diff runner objects
     */
    public DiffRunner[] diffAgainstPrevious() {
        P4Collection collection = super.getDirectFileSelection();
        List<DiffRunner> runners = new ArrayList<DiffRunner>();
        if (!collection.isEmpty()) {
            IP4Resource[] resources = collection.members();
            for (IP4Resource resource : resources) {
                IP4File file = (IP4File) resource;
                file.refresh();
                if (file.getHeadRevision() > 1) {
                    String depotPath = file.getRemotePath();
                    depotPath += "#" + (file.getHeadRevision() - 1); //$NON-NLS-1$
                    String depotPath2 = file.getRemotePath();
                    depotPath2 += "#" + file.getHeadRevision(); //$NON-NLS-1$
                    DiffRunner runner = new DiffRunner(file.getConnection(),
                            true, true, depotPath, depotPath, depotPath2,
                            depotPath2);
                    runner.run();
                    runners.add(runner);
                }
            }
        }
        return runners.toArray(new DiffRunner[0]);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = false;
        if (containsOnlineConnection()) {
            P4Collection collection = super.getDirectFileSelection();
            IP4Resource[] resources = collection.members();
            int size = resources.length;
            if (size > 0) {
                for (IP4Resource resource : resources) {
                    if (resource instanceof IP4File) {
                        IP4File file = (IP4File) resource;
                        if (file.getAction() != null) {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }
}
