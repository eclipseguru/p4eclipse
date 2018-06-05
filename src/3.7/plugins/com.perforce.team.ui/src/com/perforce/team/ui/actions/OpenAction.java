package com.perforce.team.ui.actions;

/*
 * Copyright (c) 2003 - 2005 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.editor.ClientFileEditorInput;
import com.perforce.team.ui.editor.DepotFileEditorInput;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Open action
 */
public class OpenAction {

    /**
     * Opens the specified file paths
     * 
     * @param files
     */
    public static void openFiles(IP4File[] files) {
        for (int i = 0; i < files.length; i++) {
            IFile workspaceFile = PerforceProviderPlugin
                    .getWorkspaceFile(files[i].getLocalPath());
            if (workspaceFile != null) {
                openFile(workspaceFile);
            } else {
                openFile(files[i]);
            }
        }
    }

    /**
     * This method opens a read-only editor around the content of p4 printing
     * the file at the current head revision if that current head revision is
     * greater than zero. This must be called from the UI-thread.
     * 
     * @param file
     */
    public static void openDepotFile(IP4File file) {
        if (file != null && file.getHeadRevision() > 0
                && !file.isHeadActionDelete()) {
            DepotFileEditorInput input = new DepotFileEditorInput(file);
            P4UIUtils.openEditor(input);
        }
    }

    /**
     * Open a non-null depot file in a read-only default editor. This must be
     * called from the UI-thread.
     * 
     * @param file
     */
    public static void openFile(IP4File file) {
        if (file != null) {
            IFile local = file.getLocalFileForLocation();
            if (local != null) {
                openFile(local);
            } else {
                String localPath = file.getLocalPath();
                if (localPath != null) {
                    File localFile = new File(localPath);
                    if (localFile.exists()) {
                        ClientFileEditorInput input = new ClientFileEditorInput(
                                localFile);
                        P4UIUtils.openEditor(input);
                    } else {
                        openDepotFile(file);
                    }
                } else {
                    openDepotFile(file);
                }
            }
        }
    }

    /**
     * Open a non-null file path in the default editor. This must be called from
     * the UI-thread. This will prompt to open the project if the file is
     * currently in a closed project. This method will only open editors for
     * file paths that correspond to an Eclipse workspace file object.
     * 
     * @param filePath
     */
    public static void openFile(String filePath) {
        IFile workspaceFile = PerforceProviderPlugin.getWorkspaceFile(filePath);
        if (workspaceFile != null) {
            openFile(workspaceFile);
        }
    }

    /**
     * Open a non-null file in the default editor. This must be called from the
     * UI-thread. This will prompt to open the project if the file is currently
     * in a closed project.
     * 
     * @param file
     */
    public static void openFile(IFile file) {
        final boolean[] openFile = new boolean[] { false };
        // Check if file is in a project that is closed
        final IProject project = file.getProject();
        Shell shell = P4UIUtils.getShell();
        if (!project.isOpen()) {
            // Ask if user wants to open project
            if (MessageDialog.openQuestion(shell,
                    Messages.OpenAction_P4Eclipse,
                    Messages.OpenAction_AskOpenProjectMessage)) {
                try {
                    IRunnableWithProgress op = new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor) {
                            try {
                                project.open(monitor);
                                openFile[0] = true;
                            } catch (CoreException e) {
                                PerforceProviderPlugin.logError(e);
                            }
                        }
                    };
                    new ProgressMonitorDialog(shell).run(true, true, op);
                } catch (InvocationTargetException e) {
                    // handle exception
                    PerforceProviderPlugin.logError(e);
                } catch (InterruptedException e) {
                    // handle cancelation
                }
            }
        } else {
            openFile[0] = true;
        }
        if (openFile[0]) {
            try {

                IDE.openEditor(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage(), file);

            } catch (PartInitException e) {
            }
        }
    }

    /**
     * Opens the specified file paths
     * 
     * @param files
     */
    public static void openFiles(String[] files) {
        for (int i = 0; i < files.length; i++) {
            openFile(files[i]);
        }
    }
}
