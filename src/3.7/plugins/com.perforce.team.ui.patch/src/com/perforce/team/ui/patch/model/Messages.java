/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.patch.model.messages"; //$NON-NLS-1$

    /**
     * FileStream_OverwriteMessage
     */
    public static String FileStream_OverwriteMessage;

    /**
     * FileStream_OverwriteTitle
     */
    public static String FileStream_OverwriteTitle;

    /**
     * FileStream_ReadOnlyMessage
     */
    public static String FileStream_ReadOnlyMessage;

    /**
     * FileStream_ReadOnlyTitle
     */
    public static String FileStream_ReadOnlyTitle;

    /**
     * P4Patch_AddingPerforceMetadata
     */
    public static String P4Patch_AddingPerforceMetadata;

    /**
     * P4Patch_GeneratingPatch
     */
    public static String P4Patch_GeneratingPatch;

    /**
     * P4Patch_RefreshingFiles
     */
    public static String P4Patch_RefreshingFiles;

	public static String PatchDiffRunner_DeleteFileError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
