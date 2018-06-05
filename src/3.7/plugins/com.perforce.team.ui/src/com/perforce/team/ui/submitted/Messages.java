/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.submitted.messages"; //$NON-NLS-1$

    public static String SubmittedChangelistDialog_SelectSubmittedChangelist;
    public static String SubmittedChangelistTable_Changelist;
    public static String SubmittedChangelistTable_ClearFolderFileFilter;
    public static String SubmittedChangelistTable_ClearUserFilter;
    public static String SubmittedChangelistTable_ClearWorkspaceFilter;
    public static String SubmittedChangelistTable_Date;
    public static String SubmittedChangelistTable_DateSubmitted;
    public static String SubmittedChangelistTable_Description;
    public static String SubmittedChangelistTable_FetchingSubmittedChangelist;
    public static String SubmittedChangelistTable_FolderFile;
    public static String SubmittedChangelistTable_Loading;
    public static String SubmittedChangelistTable_LoadingSubmittedChangelists;
    public static String SubmittedChangelistTable_ShowMore;
    public static String SubmittedChangelistTable_ShowNumMore;
    public static String SubmittedChangelistTable_SubmittedBy;
    public static String SubmittedChangelistTable_UpdatingSubmittedChangelistJobsAndFiles;
    public static String SubmittedChangelistTable_UpdatingSubmittedChangelistView;
    public static String SubmittedChangelistTable_User;
    public static String SubmittedChangelistTable_UserLabel;
    public static String SubmittedChangelistTable_Workspace;
    public static String SubmittedChangelistTable_WorkspaceLabel;
    public static String SubmittedFormPage_DateSubmitted;
    public static String SubmittedFormPage_SubmittedBy;
    public static String SubmittedSorter_Changelist;
    public static String SubmittedSorter_Date;
    public static String SubmittedSorter_Description;
    public static String SubmittedSorter_User;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
