/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.folder.diff.actions.messages"; //$NON-NLS-1$

    /**
     * DiffBranchAction_OpeningBranchDiff
     */
    public static String DiffBranchAction_OpeningBranchDiff;

    /**
     * DiffBranchAction_OpeningDiffEditor
     */
    public static String DiffBranchAction_OpeningDiffEditor;

    /**
     * DiffFoldersAction_OpeningEditor
     */
    public static String DiffFoldersAction_OpeningEditor;

    /**
     * DiffMappingAction_Comparing
     */
    public static String DiffMappingAction_Comparing;

    /**
     * DiffMappingAction_LoadingFolders
     */
    public static String DiffMappingAction_LoadingFolders;

    /**
     * IntegrateDiffAction_IntegratingFile
     */
    public static String IntegrateDiffAction_IntegratingFile;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
