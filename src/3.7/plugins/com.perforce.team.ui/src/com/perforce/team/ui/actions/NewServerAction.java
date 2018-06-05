package com.perforce.team.ui.actions;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.connection.ConnectionWizard;
import com.perforce.team.ui.connection.ConnectionWizardDialog;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.p4java.actions.P4Action;

/**
 * Creates a new p4 connection
 */
public class NewServerAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        runAction();
    }

    /**
     * Adds a connection with the specified params
     * 
     * @param params
     */
    public void add(ConnectionParameters params) {
        if (params != null) {
            P4ConnectionManager.getManager().add(params);
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        return getSelection() != null && getSelection().getFirstElement() != null;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Connection initial = null;
        if (this.getSelection() != null) {
            P4Collection collection = getResourceSelection();
            IP4Resource[] members = collection.members();
            if (members.length > 0 && members[0] instanceof IP4Connection) {
                initial = (IP4Connection) members[0];
            }
        }
        ConnectionWizardDialog dialog = new ConnectionWizardDialog(
                P4UIUtils.getShell(), new ConnectionWizard(initial));
        dialog.create();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IHelpContextIds.P4_NEW_CONNECTION);
        dialog.open();
    }
}
