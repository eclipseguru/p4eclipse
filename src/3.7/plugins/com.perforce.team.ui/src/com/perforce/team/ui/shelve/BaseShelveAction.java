/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.P4Action;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class BaseShelveAction extends P4Action {

    /**
     * Show the no shelved versions dialog. This method must be called from the
     * UI-thread.
     * 
     * @param file
     */
    protected static void showNoVersions(IP4File file) {
        String message;
        if (file != null) {
            message = MessageFormat.format(
                    Messages.BaseShelveAction_NoShelvedVersionsOfFile,
                    file.getName());
        } else {
            message = Messages.BaseShelveAction_NoShelvedVersionsOfSelectedFile;
        }
        P4ConnectionManager.getManager()
                .openInformation(P4UIUtils.getDialogShell(),
                        Messages.BaseShelveAction_NoShelvedVersions,
                        message.toString());
    }

}
