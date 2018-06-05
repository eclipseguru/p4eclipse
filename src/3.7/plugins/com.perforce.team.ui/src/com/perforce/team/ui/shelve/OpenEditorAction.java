/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.editor.DepotFileEditorInput;
import com.perforce.team.ui.p4java.actions.P4Action;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class OpenEditorAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4ShelveFile) {
            IP4ShelveFile shelve = (IP4ShelveFile) resource;
            IP4File file = shelve.getFile();
            // Fix for job036726, don't open files that are shelved deletes
            // since a p4 print on them will always return empty string.
            if (file != null && !file.openedForDelete()) {
                DepotFileEditorInput input = new DepotFileEditorInput(file,
                        shelve.getRevision());
                P4UIUtils.openEditor(input);
            }
        }
    }

}
