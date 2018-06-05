/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.actions;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;
import com.perforce.team.ui.patch.P4PatchUiPlugin;
import com.perforce.team.ui.patch.model.ErrorCollector;
import com.perforce.team.ui.patch.model.IErrorCollector;
import com.perforce.team.ui.patch.model.IPatchStream;
import com.perforce.team.ui.patch.model.P4Patch;
import com.perforce.team.ui.patch.wizard.CreatePatchWizard;
import com.perforce.team.ui.patch.wizard.CreatePatchWizardDialog;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CreatePatchAction extends P4Action {

    /**
     * PATCH_SERVER_VERSION - version that added patch friendly support for diff
     * -du
     */
    public static final int PATCH_SERVER_VERSION = 20092;

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
        return connection.getIntVersion() >= PATCH_SERVER_VERSION;
    }

    /**
     * Schedule an async display of the not supported dialog
     */
    protected void showNotSupported() {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                P4ConnectionManager.getManager().openInformation(
                        P4UIUtils.getDialogShell(),
                        Messages.CreatePatchAction_NotSupported_Title,
                        Messages.CreatePatchAction_NotSupported_Description);
            }
        });
    }

    /**
     * Schedule wizard open for collection
     * 
     * @param collection
     */
    protected void scheduleOpen(final P4Collection collection) {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                CreatePatchWizard wizard = new CreatePatchWizard(collection);
                WizardDialog dialog = new CreatePatchWizardDialog(P4UIUtils
                        .getDialogShell(), wizard);
                if (WizardDialog.OK == dialog.open()) {
                    IResource[] resources = wizard.getResources();
                    IPatchStream stream = wizard.getStream();
                    P4Patch patch = new P4Patch(stream, resources);
                    IErrorCollector collector = new ErrorCollector() {

                        @Override
                        public void done() {
                            if (getErrorCount() > 0) {
                                final String message;
                                if (getErrorCount() > 1) {
                                    message = Messages.CreatePatchAction_MultipleErrorsMessage;
                                } else {
                                    message = Messages.CreatePatchAction_SingleErrorMessage;
                                }
                                final MultiStatus status = new MultiStatus(
                                        P4PatchUiPlugin.PLUGIN_ID,
                                        IStatus.ERROR, message, null);
                                for (Throwable throwable : getErrors()) {
                                    status.add(new Status(
                                            IStatus.ERROR,
                                            P4PatchUiPlugin.PLUGIN_ID,
                                            Messages.CreatePatchAction_PatchErrorMessage,
                                            throwable));
                                }

                                PerforceUIPlugin.syncExec(new Runnable() {

                                    public void run() {
                                        ErrorDialog.openError(
                                                P4UIUtils.getDialogShell(),
                                                Messages.CreatePatchAction_PatchFailedTitle,
                                                Messages.CreatePatchAction_PatchFailedMessage,
                                                status);
                                    }
                                });
                            }
                        }

                    };
                    patch.generate(collector);
                }
            }
        });
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final P4Collection collection = getResourceSelection();
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                boolean supported = true;
                // Fix for job042372, disallow patching if any connection in
                // selection is pre-2009.2
                for (IP4Resource resource : collection.members()) {
                    if (!connectionSupported(resource.getConnection())) {
                        supported = false;
                        break;
                    }
                }
                if (supported) {
                    scheduleOpen(collection);
                } else {
                    showNotSupported();
                }
            }

            @Override
            public String getTitle() {
                return Messages.CreatePatchAction_OpeningWizard_Titlte;
            }
        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return !getResourceSelection().isEmpty();
    }

}
