/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.wizard;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.patch.wizard.messages"; //$NON-NLS-1$

    /**
     * CreatePatchWizard_Title
     */
    public static String CreatePatchWizard_Title;

    /**
     * LocationPage_AbsolutePath
     */
    public static String LocationPage_AbsolutePath;

    /**
     * LocationPage_Browse
     */
    public static String LocationPage_Browse;

    /**
     * LocationPage_Clipboard
     */
    public static String LocationPage_Clipboard;

    /**
     * LocationPage_Description
     */
    public static String LocationPage_Description;

    /**
     * LocationPage_DirectoryP4Path
     */
    public static String LocationPage_DirectoryP4Path;

    /**
     * LocationPage_DirectoryPath
     */
    public static String LocationPage_DirectoryPath;

    /**
     * LocationPage_EnterAbsoluteP4Path
     */
    public static String LocationPage_EnterAbsoluteP4Path;

    /**
     * LocationPage_EnterP4Path
     */
    public static String LocationPage_EnterP4Path;

    /**
     * LocationPage_FileNotWritable
     */
    public static String LocationPage_FileNotWritable;

    /**
     * LocationPage_FilePath
     */
    public static String LocationPage_FilePath;

    /**
     * LocationPage_FolderDoesNotExist
     */
    public static String LocationPage_FolderDoesNotExist;

    /**
     * LocationPage_NonExecutableP4Path
     */
    public static String LocationPage_NonExecutableP4Path;

    /**
     * LocationPage_P4Path
     */
    public static String LocationPage_P4Path;

    /**
     * LocationPage_PendingProjectFiles
     */
    public static String LocationPage_PendingProjectFiles;

    /**
     * LocationPage_ProjectDoesNotExist
     */
    public static String LocationPage_ProjectDoesNotExist;

    /**
     * LocationPage_SelectExportLocation
     */
    public static String LocationPage_SelectExportLocation;

    /**
     * LocationPage_SelectResource
     */
    public static String LocationPage_SelectResource;

    /**
     * LocationPage_Title
     */
    public static String LocationPage_Title;

    /**
     * LocationPage_ValidWorkspacePath
     */
    public static String LocationPage_ValidWorkspacePath;

    /**
     * LocationPage_WorkspaceDialogMessage
     */
    public static String LocationPage_WorkspaceDialogMessage;

    /**
     * LocationPage_WorkspaceDialogTitle
     */
    public static String LocationPage_WorkspaceDialogTitle;

    /**
     * LocationPage_WorkspacePath
     */
    public static String LocationPage_WorkspacePath;

    /**
     * PendingViewer_LoadingChangelists
     */
    public static String PendingViewer_LoadingChangelists;

    /**
     * WorkspaceDialog_FileName
     */
    public static String WorkspaceDialog_FileName;

    /**
     * WorkspaceDialog_FileNameInvalid
     */
    public static String WorkspaceDialog_FileNameInvalid;

    /**
     * WorkspaceDialog_MustSpecifyFileName
     */
    public static String WorkspaceDialog_MustSpecifyFileName;

    /**
     * WorkspaceDialog_SelectFolder
     */
    public static String WorkspaceDialog_SelectFolder;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
