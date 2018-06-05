/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.editor.BranchGraphEditor;
import com.perforce.team.ui.mergequest.editor.BranchGraphInput;
import com.perforce.team.ui.p4java.actions.P4Action;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class OpenBranchGraphAction extends P4Action {

    /**
     * BRANCH_GRAPH_SERVER_VERSION
     */
    public static final int BRANCH_GRAPH_SERVER_VERSION = 20101;

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return P4ConnectionManager.getManager().size() == 1
                || getSingleOnlineResourceSelection() != null;
    }

    /**
     * Is the specified non-null connection supported? This method should not be
     * called from the UI-thread.
     * 
     * @param connection
     * @return true if supported, false otherwise
     */
    protected boolean connectionSupported(IP4Connection connection) {
        if (!connection.isConnected()) {
            connection.connect();
        }
        return connection.getIntVersion() >= BRANCH_GRAPH_SERVER_VERSION;
    }

    /**
     * Schedule an async open of the branch graph editor for the specified
     * non-null connection.
     * 
     * @param connection
     */
    protected void scheduleOpen(final IP4Connection connection) {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                BranchGraphInput input = new BranchGraphInput(connection);
                try {
                    IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                            BranchGraphEditor.ID);
                } catch (PartInitException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        });
    }

    /**
     * Schedule an async display of the not supported dialog
     */
    protected void showNotSupported() {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                P4ConnectionManager.getManager().openInformation(
                        P4UIUtils.getDialogShell(),
                        Messages.OpenBranchGraphAction_NotSupportedTitle,
                        Messages.OpenBranchGraphAction_NotSupportedDescription);
            }
        });
    }

    /**
     * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        if (this.collection == null
                && (this.getSelection() == null || this.getSelection().isEmpty())) {
            IP4Connection[] connections = P4ConnectionManager.getManager()
                    .getConnections();
            if (connections.length == 1) {
//                this.selection = new StructuredSelection(connections[0]);
            	selectionChanged(action, new StructuredSelection(connections[0]));
            }
        }
        super.run(action);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource != null) {
            final IP4Connection connection = resource.getConnection();
            if (connection != null) {
                IP4Runnable runnable = new P4Runnable() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        if (connectionSupported(connection)) {
                            scheduleOpen(connection);
                        } else {
                            showNotSupported();
                        }
                    }

                    @Override
                    public String getTitle() {
                        return MessageFormat.format(
                                Messages.OpenBranchGraphAction_OpenJobTitle,
                                connection.getParameters().getPort());
                    }
                };
                runRunnable(runnable);
            }
        }
    }

}
