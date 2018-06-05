package com.perforce.team.ui.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.views.messages"; //$NON-NLS-1$
    public static String ConsoleView_Clear;
    public static String ConsoleView_ClearConsole;
    public static String ConsoleView_Command;
    public static String ConsoleView_Connection;
    public static String ConsoleView_Copy;
    public static String ConsoleView_Executing;
    public static String ConsoleView_OpenConsolePreferences;
    public static String ConsoleView_RefreshChangedResources;
    public static String ConsoleView_RefreshConnections;
    public static String ConsoleView_RefreshingLogConsoleConnections;
    public static String ConsoleView_Run;
    public static String ConsoleView_SelectAll;
    public static String ConsoleView_ShowInputArea;
    public static String ConsoleView_Version;
    public static String ConsoleView_VersionUnknown;
    public static String DepotView_CollapseAll;
    public static String DepotView_FilterByClientWorkspace;
    public static String DepotView_NewConnection;
    public static String DepotView_NoConnectionsCurrentlyDefined;
    public static String DepotView_Open;
    public static String DepotView_OpenWith;
    public static String DepotView_Refresh;
    public static String DepotView_RefreshingDepotViewTree;
    public static String DepotView_RemoveConnection;
    public static String DepotView_ServerInformation;
    public static String DepotView_ShowDeletedDepotFiles;
    public static String HideFilterAction_HideFilters;
    public static String HistoryView_P4EclipseSupportsEclipseHistoryView;
    public static String JobView_Columns;
    public static String JobView_CreateNewJob;
    public static String JobView_Jobs;
    public static String JobView_OpenJobsPreferences;
    public static String JobView_Refresh;
    public static String JobView_RefreshingJobs;
    public static String JobView_RefreshJobs;
    public static String JobView_SetJobViewColumns;
    public static String JobView_ShowJobDetails;
    public static String PendingDropAdapter_OpeningPerforceResources;
    public static String PendingDropAdapter_ShelvingPerforceResources;
    public static String PendingDropAdapter_UnshelvingPerforceResources;
    public static String PendingView_ClearAsActivePendingChangelist;
    public static String PendingView_CollapseAll;
    public static String PendingView_CreateNewPendingChangelist;
    public static String PendingView_DiffFileAgainstDepot;
    public static String PendingView_LoadPendingChangelistsFor;
    public static String PendingView_MakeActivePendingChangelist;
    public static String PendingView_NewPendingChangelist;
    public static String PendingView_Open;
    public static String PendingView_OpenWith;
    public static String PendingView_PendingChangelists;
    public static String PendingView_RefreshAllChangelists;
    public static String PendingView_RefreshPendingChangelists;
    public static String PendingView_RefreshSelectedChangelists;
    public static String PendingView_RevertFiles;
    public static String PendingView_ShowOtherClientsChangelists;
    public static String PendingView_UpdatingPendingChangelistView;
    public static String PerforceProjectView_Connection;
    public static String PerforceProjectView_SelectAResource;
    public static String PerforceProjectView_UpdateViewForRemovedConnection;
    public static String SubmittedView_EditJob;
    public static String SubmittedView_LinkWithConnectionsView;
    public static String SubmittedView_OpenChangelist;
    public static String SubmittedView_OpenChangelistPreferences;
    public static String SubmittedView_OpenSubmittedChangelist;
    public static String SubmittedView_Refresh;
    public static String SubmittedView_RefreshSubmittedChangelists;
    public static String SubmittedView_ShowChangelistDetails;
    public static String SubmittedView_SubmittedChangelists;
    public static String SubmittedView_UpdateSubmittedChangelistsView;
    public static String SubmittedView_UpdatingSubmittedChangelists;
    public static String SubmittedView_ViewChangelist;
    public static String StreamsView_Streams;
	public static String StreamsView_ShowStreamDetails;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
