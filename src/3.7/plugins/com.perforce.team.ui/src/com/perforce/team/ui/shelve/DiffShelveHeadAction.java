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
public class DiffShelveHeadAction extends DiffShelveAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4ShelveFile) {
            final IP4ShelveFile shelveFile = (IP4ShelveFile) resource;
            IP4File file = getFile(shelveFile);
            if (file != null && file.getHeadRevision() > 0) {
                compareRevision(file, file.getHeadRevision(), shelveFile);
            }
        }
    }

    private IP4File getFile(IP4ShelveFile shelveFile) {
        // Use file from connection cache since it will have the latest
        // fstat output
        String path = shelveFile.getRemotePath();
        IP4File file = null;
        if (path != null) {
            file = shelveFile.getConnection().getFile(path);
        }
        if (file == null) {
            file = shelveFile.getFile();
        }
        return file;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        boolean enabled = false;
        IP4Resource resource = getSingleResourceSelection();
        if (resource instanceof IP4ShelveFile) {
            IP4File file = getFile((IP4ShelveFile) resource);
            if (file == null) {
                file = ((IP4ShelveFile) resource).getFile();
            }
            if (file != null) {
                enabled = file.getHeadRevision() > 0;
            }
        }
        return enabled;
    }

}
