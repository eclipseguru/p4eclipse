/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.folder.timelapse.messages"; //$NON-NLS-1$

    public static String FolderLabelDecorator_ChangelistSuffix;
    public static String FolderLabelProvider_From;
    public static String FolderTimeLapseEditor_Changelist;
    public static String FolderTimeLapseEditor_ChangelistNum;
    public static String FolderTimeLapseEditor_ClearSelection;
    public static String FolderTimeLapseEditor_DisplayActionIcons;
    public static String FolderTimeLapseEditor_DisplayRevisionDetails;
    public static String FolderTimeLapseEditor_GoToRootFolder;
    public static String FolderTimeLapseEditor_GoUpOneLevel;
    public static String FolderTimeLapseEditor_IncrementalFolderComparison;
    public static String FolderTimeLapseEditor_LoadingHistory;
    public static String FolderTimeLapseEditor_LoadingTimelapseView;
    public static String FolderTimeLapseEditor_LoadingTimelapseViewFor;
    public static String FolderTimeLapseInput_Tooltip;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
