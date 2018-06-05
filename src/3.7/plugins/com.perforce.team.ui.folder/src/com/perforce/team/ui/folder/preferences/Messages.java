/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.folder.preferences.messages"; //$NON-NLS-1$

    /**
     * FolderDiffPreferencePage_DifferingContentColor
     */
    public static String FolderDiffPreferencePage_DifferingContentColor;

    /**
     * FolderDiffPreferencePage_DifferingFileColor
     */
    public static String FolderDiffPreferencePage_DifferingFileColor;

    /**
     * FolderDiffPreferencePage_IncludeLinkedResources
     */
    public static String FolderDiffPreferencePage_IncludeLinkedResources;

    /**
     * FolderDiffPreferencePage_UniqueContentColor
     */
    public static String FolderDiffPreferencePage_UniqueContentColor;

    /**
     * FolderDiffPreferencePage_UniqueFileColor
     */
    public static String FolderDiffPreferencePage_UniqueFileColor;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
