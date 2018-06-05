/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.core.resources.IContainer;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FolderTimeLapseAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4Folder) {
            IP4Folder folder = (IP4Folder) resource;
            IContainer[] containers = folder.getLocalContainers();
            FolderTimeLapseInput input = null;
            if (containers != null && containers.length > 0) {
                input = new FolderTimeLapseInput(folder, containers[0]);
            } else {
                input = new FolderTimeLapseInput(folder);
            }
            try {
                IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                        FolderTimeLapseEditor.ID);
            } catch (PartInitException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }
}
