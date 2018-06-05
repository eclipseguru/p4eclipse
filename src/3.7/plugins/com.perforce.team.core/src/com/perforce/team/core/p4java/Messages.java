package com.perforce.team.core.p4java;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.core.p4java.messages"; //$NON-NLS-1$
    public static String IP4Connection_2;
    public static String IP4Job_0;
    public static String P4Changelist_0;
    public static String P4ChangelistRevision_0;
    public static String P4Collection_0;
    public static String P4Collection_1;
    public static String P4Collection_2;
    public static String P4Collection_7;
	public static String P4Collection_RefreshResourceAfterSync;
    public static String P4Command_0;
    public static String P4File_3;
    public static String P4File_4;
    public static String P4Folder_1;
    public static String P4PendingChangelist_ErrorFindingID;
	public static String P4PendingChangelist_ErrorSubmit;
	public static String P4PendingChangelist_RefreshSubmitted;
	public static String P4PendingChangelist_SendSubmitChangelistEvent;
	public static String P4Runner_0;
    public static String P4Workspace_CannotReachServer;
	public static String P4Workspace_NonPerforceProject;
	public static String P4Workspace_ServerNotReady;
	public static String ProgressMonitorProgressPresenter_CurrentOfTotal;
	public static String ProgressMonitorProgressPresenter_CurrentOfTotalNextFile;
	public static String ProgressMonitorProgressPresenter_CurrentOfTotalNextFilePercentOfSize;
	
    public static String Stream_Populate;
	
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
