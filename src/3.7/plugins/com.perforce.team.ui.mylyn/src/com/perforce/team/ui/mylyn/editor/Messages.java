/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mylyn.editor.messages"; //$NON-NLS-1$

    /**
     * ChangelistEditorPageFactory_Fixes
     */
    public static String ChangelistEditorPageFactory_Fixes;

    /**
     * ChangelistFixWidget_CollapseAll
     */
    public static String ChangelistFixWidget_CollapseAll;

    /**
     * ChangelistFixWidget_ExpandAll
     */
    public static String ChangelistFixWidget_ExpandAll;

    /**
     * ChangelistFixWidget_UpdatingChangelists
     */
    public static String ChangelistFixWidget_UpdatingChangelists;

    /**
     * FixFormPage_AddChangelist
     */
    public static String FixFormPage_AddChangelist;

    /**
     * FixFormPage_Changelist
     */
    public static String FixFormPage_Changelist;

    /**
     * FixFormPage_Changelists
     */
    public static String FixFormPage_Changelists;

    /**
     * FixFormPage_ConfirmRemoval
     */
    public static String FixFormPage_ConfirmRemoval;

    /**
     * FixFormPage_Fixes
     */
    public static String FixFormPage_Fixes;

    /**
     * FixFormPage_LoadingFixes
     */
    public static String FixFormPage_LoadingFixes;

    /**
     * FixFormPage_PendingFixes
     */
    public static String FixFormPage_PendingFixes;

    /**
     * FixFormPage_PendingFixesNumber
     */
    public static String FixFormPage_PendingFixesNumber;

    /**
     * FixFormPage_Remove
     */
    public static String FixFormPage_Remove;

    /**
     * FixFormPage_RemoveChangelists
     */
    public static String FixFormPage_RemoveChangelists;

    /**
     * FixFormPage_SubmittedFixes
     */
    public static String FixFormPage_SubmittedFixes;

    /**
     * FixFormPage_SubmittedFixesNumber
     */
    public static String FixFormPage_SubmittedFixesNumber;

    /**
     * JobFieldGroup_Job
     */
    public static String JobFieldGroup_Job;

    /**
     * JobFieldGroup_Other
     */
    public static String JobFieldGroup_Other;

    /**
     * P4JobEditorPage_ErrorRetrievingHistory
     */
    public static String P4JobEditorPage_ErrorRetrievingHistory;

    /**
     * P4JobEditorPage_HistoryNotFound
     */
    public static String P4JobEditorPage_HistoryNotFound;

    /**
     * P4JobEditorPage_Refresh
     */
    public static String P4JobEditorPage_Refresh;

    /**
     * P4JobEditorPage_ShowHistory
     */
    public static String P4JobEditorPage_ShowHistory;

    /**
     * P4JobEditorPage_ViewTimeLapse
     */
    public static String P4JobEditorPage_ViewTimeLapse;

    /**
     * P4JobEditorPage_AdvancedAttributesEmpty
     */
    public static String P4JobEditorPage_AdvancedAttributesEmpty;

	public static String P4JobEditor_RefreshJobEditor;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
