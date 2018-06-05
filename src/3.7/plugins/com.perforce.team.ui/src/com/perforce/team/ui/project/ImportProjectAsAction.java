/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceProjectSetSerializer;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ImportProjectAsAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4Container) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    ProjectNameDialog dialog = new ProjectNameDialog(P4UIUtils
                            .getShell(), resource.getName(),
                            Messages.ImportProjectAsAction_EnterProjectName,
                            false);
                    if (dialog.open() == ProjectNameDialog.OK) {
                        String name = dialog.getEnteredName();
                        PerforceProjectSetSerializer.createProject(
                                (IP4Folder) resource, name, getShell(), true,
                                isAsync());
                    }
                }
            });
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return getSingleOnlineResourceSelection() != null;
    }

}
