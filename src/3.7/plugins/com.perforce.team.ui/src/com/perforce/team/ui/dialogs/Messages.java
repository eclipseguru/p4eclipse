package com.perforce.team.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.dialogs.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

    public static String AuthenticationDialog_Connection;
    public static String AuthenticationDialog_PasswordSettings;
    public static String AuthenticationDialog_PerforceAuthentication;
    public static String AuthenticationDialog_RememberPassword;
    public static String ClientPreferencesDialog_Always;
	public static String ClientPreferencesDialog_Location;
	public static String ClientPreferencesDialog_Never;
	public static String ClientPreferencesDialog_NewConnectDef;
	public static String ClientPreferencesDialog_Prompt;
	public static String ClientPreferencesDialog_SyncChoices;
	public static String ClientPreferencesDialog_Title;
	public static String CompareDialog_SaveErrorMessage;
    public static String CompareDialog_SaveErrorTitle;
    public static String ConfirmRevertDialog_DeleteEmptyPendingAfterRevert;
    public static String ConfirmRevertDialog_DeleteShelvedAfterRevert;
    public static String ConfirmRevertDialog_DeselectAll;
    public static String ConfirmRevertDialog_FilesSelected;
    public static String ConfirmRevertDialog_MustSelectAtLeastOneFile;
    public static String ConfirmRevertDialog_RevertFiles;
    public static String ConfirmRevertDialog_RevertingWillOverwrite;
    public static String ConfirmRevertDialog_RevertSelected;
    public static String ConfirmRevertDialog_SelectAll;
    public static String ConsolePreferencesDialog_CommandHistorySize;
    public static String ConsolePreferencesDialog_CommandLine;
    public static String ConsolePreferencesDialog_ConsoleSettings;
    public static String ConsolePreferencesDialog_Error;
    public static String ConsolePreferencesDialog_Message;
    public static String ConsolePreferencesDialog_ShowTimestamp;
    public static String FilePropertiesDialog_ClientPath;
    public static String FilePropertiesDialog_DepotPath;
    public static String FilePropertiesDialog_FileNotManagedByPerforce;
    public static String FilePropertiesDialog_FileType;
    public static String FilePropertiesDialog_HaveAction;
    public static String FilePropertiesDialog_HaveRevision;
    public static String FilePropertiesDialog_HeadChange;
    public static String FilePropertiesDialog_HeadRevision;
    public static String FilePropertiesDialog_LastModified;
    public static String FilePropertiesDialog_LockedBy;
    public static String FilePropertiesDialog_OpenedBy;
    public static String FilePropertiesDialog_OpenedByDesc;
    public static String FilePropertiesDialog_OpenedByDescWithAction;
    public static String FilePropertiesDialog_ShelvedBy;
    public static String FileTypeDialog_AlwaysWritableOnClient;
    public static String FileTypeDialog_Apple;
    public static String FileTypeDialog_BaseFileType;
    public static String FileTypeDialog_Binary;
    public static String FileTypeDialog_ChangeFileType;
    public static String FileTypeDialog_DisallowMultipleOpens;
    public static String FileTypeDialog_ExecBitSetOnClient;
    public static String FileTypeDialog_FileTypeModifiers;
    public static String FileTypeDialog_KeywordExpansion;
    public static String FileTypeDialog_OnlyIdAndHeader;
    public static String FileTypeDialog_PreserveModeTimes;
    public static String FileTypeDialog_Resource;
    public static String FileTypeDialog_ServerStorageMethod;
    public static String FileTypeDialog_ServerStoresCompressedRevs;
    public static String FileTypeDialog_ServerStoresDefaultMethod;
    public static String FileTypeDialog_ServerStoresFullRevs;
    public static String FileTypeDialog_ServerStoresOnlyHeadRev;
    public static String FileTypeDialog_ServerStoresRCSDeltas;
    public static String FileTypeDialog_Symlink;
    public static String FileTypeDialog_Text;
    public static String FileTypeDialog_Unicode;
    public static String GeneralPreferencesDialog_CreateMarker;
    public static String GeneralPreferencesDialog_EnableEditDeleteRenameRefactorOps;
    public static String GeneralPreferencesDialog_EnableSaveOps;
    public static String GeneralPreferencesDialog_GeneralSettings;
    public static String GeneralPreferencesDialog_GroupSyncsByChangelist;
    public static String GeneralPreferencesDialog_LogAllCommands;
    public static String GeneralPreferencesDialog_DisableMarkerDecoration;
    public static String GeneralPreferencesDialog_MarkForAdd;
    public static String GeneralPreferencesDialog_MarkForDeleteWhenDeletingLinkedResources;
    public static String GeneralPreferencesDialog_MarkForDeleteWhenDeletingProject;
    public static String GeneralPreferencesDialog_RetainExpandedFolders;
    public static String GeneralPreferencesDialog_RetainOfflineState;
    public static String GeneralPreferencesDialog_ShowChangelistSelectionWhenMarkingFiles;
    public static String GeneralPreferencesDialog_ShowChangelistSelectionWhenRefactoring;
    public static String GeneralPreferencesDialog_TakeNoAction;
    public static String GeneralPreferencesDialog_UseMoveForRefactoring;
    public static String GeneralPreferencesDialog_WhenAddingNewFile;
    public static String InteractiveResolveDialog_Accept;
    public static String InteractiveResolveDialog_Diff;
    public static String InteractiveResolveDialog_Edit;
    public static String InteractiveResolveDialog_ResolveFile;
    public static String InteractiveResolveDialog_Summary;
    public static String InteractiveResolveDialog_TheirFile;
    public static String InteractiveResolveDialog_YourFile;
    public static String JobColumnsDialog_Add;
    public static String JobColumnsDialog_MoveDown;
    public static String JobColumnsDialog_MoveUp;
    public static String JobColumnsDialog_Remove;
    public static String JobColumnsDialog_SetJobViewColumns;
    public static String JobFixDialog_SelectJobsFixedByChangelist;
    public static String JobListViewer_RemoveFromChangelist;
    public static String JobsDialog_ClearFolderFile;
    public static String JobsDialog_ClearKeywordFilter;
    public static String JobsDialog_FolderFile;
    public static String JobsDialog_Keywords;
    public static String JobsDialog_Loading;
    public static String JobsDialog_LoadingJobs;
    public static String JobsDialog_ShowMore;
    public static String JobsDialog_ShowNumMore;
    public static String JobsDialog_UpdatingJobsView;
    public static String P4FormDialog_CancelChangesMessage;
    public static String P4FormDialog_CancelChangesTitle;
    public static String P4FormDialog_CancelCreationMessage;
    public static String P4FormDialog_CancelCreationTitle;
    public static String P4FormDialog_Create;
    public static String P4FormDialog_Save;
    public static String ProjectPropertiesDialog_EditConnectionNote;
    public static String RefactorDialog_From;
    public static String RefactorDialog_OpenInChangelist;
    public static String RefactorDialog_RefactoringFilesFromTo;
    public static String RefactorDialog_SelectRefactorChangelist;
    public static String RefactorDialog_To;
    public static String RefactorDialog_UseSelectedChangelistUntilSubmitted;
    public static String ResolvePreferencesDialog_AcceptSource;
    public static String ResolvePreferencesDialog_AcceptTarget;
    public static String ResolvePreferencesDialog_AcceptMergeSafe;
    public static String ResolvePreferencesDialog_AcceptMergeNoConflicts;
    public static String ResolvePreferencesDialog_AcceptMergeWithConflicts;
    public static String ResolvePreferencesDialog_AutoResolveOptions;
    public static String ResolvePreferencesDialog_Automatically;
    public static String ResolvePreferencesDialog_DefaultMode;
    public static String ResolvePreferencesDialog_Interactively;
	public static String ResolvePreferencesDialog_Prompt;
    public static String ResolvePreferencesDialog_InteractiveMergeTool;
    public static String ResolvePreferencesDialog_MergeBinaryAsText;
    public static String ResolvePreferencesDialog_MergeWithP4Merge;
    public static String ResolvePreferencesDialog_MergeWithEclipse;
    public static String SetConnectionDialog_InvalidCharsetMessage;
    public static String SetConnectionDialog_InvalidCharsetTitle;
    public static String SetConnectionDialog_ServerConnection;
    public static String SyncRevisionDialog_ForceOperation;
    public static String SyncRevisionDialog_GetLatestRevision;
    public static String SyncRevisionDialog_LabelChangeOrDate;
    public static String SyncRevisionDialog_Other;
    public static String SyncRevisionDialog_PreviewSync;
    public static String SyncRevisionDialog_RevisionNumber;
    public static String SyncRevisionDialog_SyncFilesToRevLabelChangeOrDate;
    public static String StreamsPreferenceDialog_Streams;
    public static String StreamsPreferencesDialog_AutoSwitch;
	public static String StreamsPreferencesDialog_DisplayStreamAs;
	public static String StreamsPreferencesDialog_ManualSwitch;
    public static String StreamsPreferencesDialog_NameAndRoot;
    public static String StreamsPreferencesDialog_NameOnly;
	public static String StreamsPreferencesDialog_NotWarnSwitch;
    public static String StreamsPreferencesDialog_RootOnly;
	public static String StreamsPreferencesDialog_SwitchDesc;
	public static String SwitchClientDialog_DefaultDescription;
	public static String StreamsPreferencesDialog_WorkspaceLink;
	public static String SwitchClientDialog_Description;
	public static String SwitchClientDialog_NoPrompot;
	public static String SwitchClientDialog_NoSync;
	public static String SwitchClientDialog_ReactivatePromptTip;
	public static String SwitchClientDialog_Sync;
	public static String SwitchClientDialog_Title;
	public static String GeneralPreferencesDialog_PromptOnRemove;
	public static String ConsolePreferencesDialog_HideLargeSizeOutput;
	public static String GeneralPreferencesDialog_RefreshRevisionWhenRefresh;
}
