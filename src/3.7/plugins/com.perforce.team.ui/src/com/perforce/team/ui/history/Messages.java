package com.perforce.team.ui.history;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.history.messages"; //$NON-NLS-1$
    public static String P4HistoryPage_Action;
    public static String P4HistoryPage_Changelist;
    public static String P4HistoryPage_CompareMode;
    public static String P4HistoryPage_CompareModeTooltip;
    public static String P4HistoryPage_Date;
    public static String P4HistoryPage_Description;
    public static String P4HistoryPage_DiffTwoRevisions;
    public static String P4HistoryPage_DisplayBranchingHistory;
    public static String P4HistoryPage_FileName;
    public static String P4HistoryPage_GetRevision;
    public static String P4HistoryPage_GroupRevisionsByDate;
    public static String P4HistoryPage_LatestWorkspaceRevision;
    public static String P4HistoryPage_LoadingHistoryFor;
    public static String P4HistoryPage_OlderThanThisMonth;
    public static String P4HistoryPage_OpenInEditor;
    public static String P4HistoryPage_RefreshingRevisionHistory;
    public static String P4HistoryPage_Revision;
    public static String P4HistoryPage_Search;
    public static String P4HistoryPage_ShowDescriptionViewer;
    public static String P4HistoryPage_ShowSearchField;
    public static String P4HistoryPage_ShowSearchFieldTooltip;
    public static String P4HistoryPage_ThisMonth;
    public static String P4HistoryPage_User;
    public static String P4HistoryPage_WrapDescriptionViewer;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
