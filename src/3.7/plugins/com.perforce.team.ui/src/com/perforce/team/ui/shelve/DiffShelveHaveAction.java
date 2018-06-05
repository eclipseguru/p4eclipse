/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DiffShelveHaveAction extends DiffShelveAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4ShelveFile) {
            IP4ShelveFile shelveFile = (IP4ShelveFile) resource;
            IP4File file = shelveFile.getFile();
            if (file != null) {
                int revision = file.getHaveRevision();
                if (revision > 0) {
                    compareRevision(file, revision, shelveFile);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        boolean enabled = false;
        IP4Resource resource = getSingleResourceSelection();
        if (resource instanceof IP4ShelveFile) {
            IP4File file = ((IP4ShelveFile) resource).getFile();
            if (file != null) {
                enabled = file.getHaveRevision() > 0;
            }
        }
        return enabled;
    }

}
