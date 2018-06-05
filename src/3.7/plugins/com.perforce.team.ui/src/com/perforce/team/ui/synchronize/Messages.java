package com.perforce.team.ui.synchronize;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.synchronize.messages"; //$NON-NLS-1$
    public static String HistoryModelOperation_OpeningHistoryView;
    public static String P4ChangeSetActionGroup_ClearAsActivePendingChangelist;
    public static String P4ChangeSetActionGroup_EditChangelist;
    public static String P4ChangeSetActionGroup_MakeActivePendingChangelist;
    public static String P4ChangeSetActionGroup_ViewChangelist;
    public static String P4ChangeSetCollector_LoadingChange;
    public static String P4ChangeSetCollector_LoadingSubmittedChangeSets;
    public static String PerforceSyncActionGroup_CheckConsistency;
    public static String PerforceSyncActionGroup_ExpandAll;
    public static String PerforceSyncActionGroup_MoveToChangelist;
    public static String PerforceSyncActionGroup_Resolve;
    public static String PerforceSyncActionGroup_Revert;
    public static String PerforceSyncActionGroup_RevertUnchanged;
    public static String PerforceSyncActionGroup_RevisionHistory;
    public static String PerforceSyncActionGroup_Shelve;
    public static String PerforceSyncActionGroup_Submit;
    public static String PerforceSyncActionGroup_SubmitAll;
    public static String PerforceSyncActionGroup_TimelapseView;
    public static String PerforceSyncActionGroup_Update;
    public static String PerforceSyncActionGroup_UpdateAll;
    public static String PerforceSynchronizeWizard_PageTitle;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
