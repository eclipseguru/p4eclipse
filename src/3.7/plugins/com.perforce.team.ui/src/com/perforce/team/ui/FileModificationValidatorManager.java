package com.perforce.team.ui;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.team.FileModificationValidationContext;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.p4java.actions.EditAction;

/**
 * File modification validator manager
 */
public class FileModificationValidatorManager extends FileModificationValidator {

    private static final Status OK_STATUS = new Status(IStatus.OK,
            PerforceTeamProvider.ID, IStatus.OK, "", null); //$NON-NLS-1$

    private boolean editRefactorOn() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_REFACTOR_SUPPORT);
    }

    private boolean saveRefactorOn() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_REFACTOR_SAVE_SUPPORT);
    }

    /**
     * @see org.eclipse.core.resources.IFileModificationValidator#validateEdit(org.eclipse.core.resources.IFile[],
     *      java.lang.Object)
     */
    @Override
    public IStatus validateEdit(IFile[] files,
			FileModificationValidationContext context) {
        if (files != null && files.length > 0 && editRefactorOn()) {
            final IP4Resource p4Resource = P4ConnectionManager.getManager()
                    .getResource(files[0]);
            IP4Connection connection = getConnection(p4Resource, files[0]);
            if (connection != null && !connection.isOffline()) {
                if (!editFile(files)) {
                    return Status.CANCEL_STATUS;
                }
            } else {
                final Display currentDisplay = PerforceUIPlugin.getDisplay();
                // handle offsite mode
                for (int i = 0; i < files.length; i++) {
                    final IFile file = files[i];
                    if (file.isReadOnly()) {
                        if (MessageDialog
                                .openQuestion(
                                        currentDisplay.getActiveShell(),
                                        Messages.FileModificationValidatorManager_confirm0,
                                        MessageFormat
                                                .format(Messages.FileModificationValidatorManager_Overwrite,
                                                        new Object[] { file
                                                                .getName(), }))) {
                            makeWritrable(file);
                        }
                    }
                }
            }
            return OK_STATUS;
        }
        return Status.CANCEL_STATUS;
    }

	private void makeWritrable(final IFile file) {
		SafeRunner.run(new ISafeRunnable() {

		    public void handleException(Throwable exception) {
		        PerforceUIPlugin.log(new Status(
		                IStatus.ERROR, PerforceUIPlugin.ID,
		                IStatus.ERROR, exception
		                        .getMessage(), exception));

		    }

		    public void run() throws Exception {
		        ResourceAttributes attr = file
		                .getResourceAttributes();
		        attr.setReadOnly(false);
		        file.setResourceAttributes(attr);

		    }
		});
	}

    private IP4Connection getConnection(IP4Resource resource, IFile file) {
        if (resource != null) {
            return resource.getConnection();
        } else if (file != null) {
            return P4Workspace.getWorkspace().getConnection(file.getProject());
        }
        return null;
    }

    /**
     * Edit the file
     *
     * @param files
     * @return - false if the dialog was cancelled or no files were selected
     *         from the dialog, true otherwise
     */
    private boolean editFile(IFile[] files) {
    	// make them writeable first
        for (IFile file : files) {
        	makeWritrable(file);
		}
        // do the remaining work async
        EditAction action = new EditAction();
        action.setAsync(true);
        action.selectionChanged(null, new StructuredSelection(files));
        action.run(null);
        return !action.wasDialogCancelled();
    }

    /**
     * @see org.eclipse.core.resources.IFileModificationValidator#validateSave(org.eclipse.core.resources.IFile)
     */
    public IStatus validateSave(final IFile file) {
        if (file != null && saveRefactorOn()) {
            IP4Resource p4Resource = P4ConnectionManager.getManager()
                    .getResource(file);
            IP4Connection connection = getConnection(p4Resource, file);
            if (connection != null && !connection.isOffline()) {
                if (p4Resource instanceof IP4File
                        && ((IP4File) p4Resource).isRemote()
                        && !((IP4File) p4Resource).isOpened()) {
                    editFile(new IFile[] {file});
                }
            }
        }
        return OK_STATUS;
    }

}
