/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.p4java.actions.messages"; //$NON-NLS-1$

    public static String AbstractStreamAction_StreamSwitchedDesc;

	public static String AddAction_AddingToSourceControl;

    public static String AddAction_AddToChangelist;

    public static String AddAction_AddToSourceControl;

    public static String AddIgnoreAction_IgnoringSelectedFoldersFiles;

    public static String ChangeFiletypeAction_ChangingFileType;

    public static String DeleteAction_MarkForDelete;

    public static String DeleteAction_MarkingForDelete;

    public static String DeleteAction_OpenInChangelist;

    public static String DeleteChangelistAction_ChangeContainsShelvedFilesMessage;

    public static String DeleteChangelistAction_ChangeContainsShelvedFilesTitle;

    public static String DeleteChangelistAction_DeletingChangelists;

    public static String DeleteChangelistAction_DeletingChangelistTask;

    public static String DepotDiffPreviousAction_CantDiffAddedMessage;

    public static String DepotDiffPreviousAction_CantDiffAddedTitle;

    public static String DepotDiffPreviousAction_CantDiffDeletedMessage;

    public static String DepotDiffPreviousAction_CantDiffDeletedTitle;

    public static String DepotDiffPreviousAction_GeneratingDiff;

    public static String EditAction_Cancel;

	public static String EditAction_CheckingOut;

    public static String EditAction_CheckOut;

	public static String EditAction_DontGetLatest;

	public static String EditAction_GetLatest;

    public static String EditAction_OpenInChangelist;
    
    public static String EditAction_UnsyncedFiles;
    
    public static String EditChangelistAction_DefaultTitle;
    public static String EditChangelistAction_NumberedTitle;

    public static String EditClientAction_ClientNotFoundMessage;

    public static String EditClientAction_ClientNotFoundTitle;

    public static String EditClientAction_EditingPerforceClient;

    public static String EditClientAction_RefreshingPerforceClient;

    public static String EditJobAction_DisplayingDialogSubtask;

    public static String EditJobAction_EditingJob;

    public static String EditJobAction_FixingChangelist;

    public static String EditJobAction_LoadingJob;

    public static String EditJobAction_RefreshingChangelistsSubtask;

    public static String EditJobAction_RefreshingJob;

    public static String EditJobAction_RefreshingJobSubtask;

    public static String EditJobAction_UnfixingChangelist;

    public static String EditJobAction_UpdatingJob;

    public static String IntegrateAction_IntegratingFiles;

    public static String LabelFilesAction_LabelDoesNotExistMessage;

    public static String LabelFilesAction_LabelDoesNotExistTitle;

    public static String LabelFilesAction_LabelingResources;

    public static String LockAction_Locking;

    public static String MoveToAnotherChangelistAction_ChangelistNum;

    public static String MoveToAnotherChangelistAction_CreatingNewChangelist;

    public static String MoveToAnotherChangelistAction_DefaultChangelist;

    public static String MoveToAnotherChangelistAction_ReopeningPerforceResources;

    public static String NewChangelistAction_CreatingNewChangelist;

    public static String NewChangelistAction_DisplayingNewChangelistDialog;

    public static String NewChangelistAction_RefreshingDefaultChangelist;

    public static String NewChangelistAction_Test;

    public static String NewJobAction_DisplayingNewJobDialog;

    public static String NewJobAction_RetrievingJobTemplate;

    public static String P4Action_NumFiles;

    public static String P4Action_NumFolders;
    
	public static String PullAction_Pulling;

	public static String PopulateAction_PopulatingFiles;

    public static String RefreshAction_RefreshingPerforceResources;

    public static String RemoveAction_NotPromptAgain;

	public static String RemoveAction_RemoveDialogMessage;

	public static String RemoveAction_RemoveDialogTitle;

	public static String RemoveAction_RemoveMultipleFolders;

	public static String RemoveAction_RemoveSingleFolder;

	public static String RemoveAction_RemovingFromWorkspace;

    public static String ResolveAction_NoUnresolvedFilesMessage;

    public static String ResolveAction_NoUnresolvedFilesTitle;

    public static String RevertAction_DeletingEmptyChangelists;

    public static String RevertAction_DeletingEmptyChangelistsSubtask;

    public static String RevertAction_DeletingShelvedFilesSubtask;

    public static String RevertAction_NoFilesToRevertMessage;

    public static String RevertAction_NoFilesToRevertTitle;

    public static String RevertAction_Reverting;

    public static String RevertUnchangedAction_RefreshingLocalResources;

    public static String RevertUnchangedAction_RevertingFiles;

    public static String RevertUnchangedAction_RevertingUnchanged;

    public static String RevertUnchangedAction_RevertUnchangedMessage;

    public static String RevertUnchangedAction_RevertUnchangedTitle;

    public static String ShareProjectsAction_SharingProject;

    public static String ShareProjectsAction_SharingProjectsMessage;

    public static String SubmitAction_ChangelistHasShelvedFilesMessage;

    public static String SubmitAction_ChangelistHasShelvedFilesTitle;

    public static String SubmitAction_ChangelistSubmitted;

    public static String SubmitAction_NoFilesToSubmitMessage;

    public static String SubmitAction_NoFilesToSubmitTitle;

    public static String SubmitAction_RefreshingSubmittedFiles;

    public static String SubmitAction_SubmitFailed;

    public static String SubmitAction_SubmittingChangelist;

    public static String SubmitAction_SubmittingChangelistTitle;

    public static String SubmitShelveAction_ChangelistContainsNonShelvedFiles;

	public static String SubmitShelveAction_Error;

	public static String SubmitShelveAction_MoveFilesToAnotherCHangelist;

	public static String SubmitShelveAction_MoveFollowingFIlesToAnotherChangelist;

	public static String SubmitShelveAction_Submit_shelved_change;

	public static String SyncAction_Syncing;

    public static String SyncPreviewAction_PreviewingSync;

    public static String SyncRevisionAction_Syncing;

    public static String TeamSynchronizeAction_SychronizingTitle;

    public static String TeamSynchronizeAction_SynchronizingMessage;

    public static String UnlockAction_Unlocking;

    public static String ViewChangelistAction_ChangelistIsPendingMessage;

    public static String ViewChangelistAction_ChangelistIsPendingTitle;

    public static String ViewChangelistAction_NoShelvedFilesMessage;

    public static String ViewChangelistAction_NoShelvedFilesTitle;

    public static String ViewChangelistAction_OpeningChangelist;

	public static String WorkinStreamAction_AboutToSwitchWorkspace;

	public static String WorkinStreamAction_CreatedBy;

	public static String WorkinStreamAction_DontWarnWheSwitchStream;

	public static String WorkinStreamAction_MustSwitchWorkspaceByCreateNew;

	public static String WorkinStreamAction_MustSwitchWorkspaceToWorkWithStream;

	public static String WorkinStreamAction_NewWorkspace;

	public static String WorkinStreamAction_SwitchStream;

	public static String WorkinStreamAction_SwitchToStream;

	public static String WorkinStreamAction_SwitchWorkspace;

	public static String WorkonlineAction_WorkOnline;


    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
