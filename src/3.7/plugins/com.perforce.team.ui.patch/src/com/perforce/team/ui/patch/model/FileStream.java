/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class FileStream implements IPatchStream {

    private boolean validate = true;

    /**
     * Validate file is writable and overwritable
     * 
     * @param file
     * @param monitor
     */
    protected void validateFile(File file, final IProgressMonitor monitor) {
        if (!validate || file == null || monitor == null) {
            return;
        }
        if (file.exists()) {
            if (!file.canWrite()) {
                monitor.setCanceled(true);
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        P4ConnectionManager.getManager().openError(
                                P4UIUtils.getDialogShell(),
                                Messages.FileStream_ReadOnlyTitle,
                                Messages.FileStream_ReadOnlyMessage);
                    }
                });
            } else {
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        boolean confirmed = P4ConnectionManager.getManager()
                                .openConfirm(P4UIUtils.getDialogShell(),
                                        Messages.FileStream_OverwriteTitle,
                                        Messages.FileStream_OverwriteMessage);
                        if (!confirmed) {
                            monitor.setCanceled(true);
                        }
                    }
                });
            }
        }
    }

    /**
     * Set file validation
     * 
     * @param validate
     */
    public void setValidateFile(boolean validate) {
        this.validate = validate;
    }

}
