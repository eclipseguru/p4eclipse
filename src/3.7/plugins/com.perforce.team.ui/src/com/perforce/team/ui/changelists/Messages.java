/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.changelists.messages"; //$NON-NLS-1$

    public static String ChangelistDetailsWidget_Changelist;

    public static String ChangelistDetailsWidget_Date;

    public static String ChangelistDetailsWidget_Description;

    public static String ChangelistDetailsWidget_User;

    public static String ChangelistDetailsWidget_Workspace;

    public static String ChangelistEditor_IntegrateChangelist;

    public static String ChangelistEditor_RefreshChangelist;

    public static String ChangelistEditor_ShelvedChangelist;

    public static String ChangelistEditor_SubmittedChangelist;

    public static String ChangelistEditor_Unshelve;

    public static String ChangelistEditorInput_ChangeNumber;

    public static String ChangelistFileWidget_CollapseAll;

    public static String ChangelistFileWidget_CompressedMode;

    public static String ChangelistFileWidget_ExpandAll;

    public static String ChangelistFileWidget_FlatMode;

    public static String ChangelistFileWidget_TreeMode;

    public static String ChangelistFormPage_Changelist;

    public static String ChangelistFormPage_Date;

    public static String ChangelistFormPage_Description;

    public static String ChangelistFormPage_Details;

    public static String ChangelistFormPage_Files;

    public static String ChangelistFormPage_FilesNumber;

    public static String ChangelistFormPage_Jobs;

    public static String ChangelistFormPage_JobsNumber;

    public static String ChangelistFormPage_LoadingChangelist;

    public static String ChangelistFormPage_Overview;

    public static String ChangelistFormPage_RefreshingChangelist;

    public static String ChangelistFormPage_User;

    public static String ChangelistFormPage_Workspace;

    public static String ChangelistHyperlinkDetector_ViewChangelist;

    public static String ChangelistJobsWidget_AddJob;

    public static String ChangelistJobsWidget_ConfirmJobRemoval;

    public static String ChangelistJobsWidget_RemoveJobsFromChangelist;

    public static String ChangelistJobsWidget_RemoveSelectedJobs;

    public static String ChangelistLabelProvider_Change;

    public static String ChangelistLabelProvider_ChangeNumber;

    public static String ChangelistLabelProvider_DefaultChange;

    public static String ChangelistLabelProvider_ShelvedFiles;

    public static String ChangelistSorter_Changelist;

    public static String ChangelistSorter_Date;

    public static String ChangelistSorter_Description;

    public static String ChangelistSorter_User;

    public static String ChangelistSorter_Workspace;

    public static String ChangelistWidget_Details;

    public static String ChangelistWidget_Files;

    public static String ChangelistWidget_Jobs;

    public static String DescriptionViewer_GeneratingContentAssist;

    public static String DescriptionViewer_UpdateEditorFont;

    public static String MoveChangeDialog_Description;

    public static String MoveChangeDialog_MoveDefaultDescription;

    public static String MoveChangeDialog_MoveFileToChangelist;

    public static String MoveChangeDialog_MoveToPendingChangelist;

    public static String MoveChangeDialog_NewPendingChangelist;

    public static String OpenChangelistDialog_ChangelistNumber;

    public static String OpenChangelistDialog_MustEnterPositiveInteger;

    public static String OpenChangelistDialog_MustEnterValidInteger;

    /**
     * PendingCombo_Change
     */
    public static String PendingCombo_Change;

    /**
     * PendingCombo_Default
     */
    public static String PendingCombo_Default;

    /**
     * PendingCombo_Description
     */
    public static String PendingCombo_Description;

    /**
     * PendingCombo_EnterChangelistDescription
     */
    public static String PendingCombo_EnterChangelistDescription;

    /**
     * PendingCombo_New
     */
    public static String PendingCombo_New;

    /**
     * PendingCombo_PendingChangelist
     */
    public static String PendingCombo_PendingChangelist;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
