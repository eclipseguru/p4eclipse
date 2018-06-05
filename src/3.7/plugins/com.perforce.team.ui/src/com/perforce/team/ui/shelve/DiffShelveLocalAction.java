/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;

import java.io.File;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DiffShelveLocalAction extends DiffShelveAction {

    /**
     * @see com.perforce.team.ui.shelve.DiffShelveAction#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4ShelveFile) {
            IP4ShelveFile shelveFile = (IP4ShelveFile) resource;
            IP4File file = shelveFile.getFile();
            if (file != null) {
                compare(file, shelveFile);
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
                String local = file.getLocalPath();
                if (local != null) {
                    File localFile = new File(local);
                    enabled = localFile.exists();
                }
            }
        }
        return enabled;
    }

}
