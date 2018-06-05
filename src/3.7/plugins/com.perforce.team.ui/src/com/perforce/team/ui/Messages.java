package com.perforce.team.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Messages class
 */
public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.messages"; //$NON-NLS-1$

    public static String ConfigWizard_MovingProjects;

	public static String ConfigWizard_ShareProject;

    public static String ConfigWizard_ShareProjects;

	public static String ConfigWizard_SharingProjects;

	public static String DatePicker_CloseLink;
    public static String FileModificationValidatorManager_confirm0;
    public static String FileModificationValidatorManager_Overwrite;
    public static String IgnoredFiles_CantUpdate;
    public static String IgnoredFiles_OK;
    public static String IgnoredFiles_UnableToIgnoreMessage;
    public static String P4ConnectionManager_CantRemoveConnectionMessage;
    public static String P4ConnectionManager_CantRemoveConnectionTitle;
    public static String P4ConnectionManager_ClientUnknown;
    public static String P4ConnectionManager_ConnectionAlreadyExistsMessage;
    public static String P4ConnectionManager_ConnectionAlreadyExistsTitle;
    public static String P4ConnectionManager_ConnectionError;
    public static String P4ConnectionManager_DisplayingPerforceErrors;
    public static String P4ConnectionManager_EditSettings;
    public static String P4ConnectionManager_ErrorHasOccurred;
    public static String P4ConnectionManager_P4NotFoundMessage;
    public static String P4ConnectionManager_P4NotFoundTitle;
    public static String P4ConnectionManager_PerforceError;
    public static String P4ConnectionManager_Retry;
    public static String P4ConnectionManager_ServerNotSupported;
    public static String P4ConnectionManager_WorkOffline;
    public static String P4FilePropertySource_Action;
    public static String P4FilePropertySource_CheckedOutByUser;
    public static String P4FilePropertySource_Client;
    public static String P4FilePropertySource_DefaultChangelistId;
    public static String P4FilePropertySource_DepotLocation;
    public static String P4FilePropertySource_FileType;
    public static String P4FilePropertySource_HaveRevision;
    public static String P4FilePropertySource_HeadAction;
    public static String P4FilePropertySource_HeadRevision;
    public static String P4FilePropertySource_LastChanged;
    public static String P4FilePropertySource_PendingChangelist;
    public static String P4FilePropertySource_ServerAddress;
    public static String P4FilePropertySource_User;
    public static String P4FilePropertySource_WorkspaceLocation;
    public static String P4TeamUtils_CannotRetrieveEngineInfo;

	public static String P4TeamUtils_Error;
	public static String P4Command_Error;
	public static String P4TeamUtils_NoConnectionError;

	public static String P4TrustDialog_Connect;
    public static String P4TrustDialog_FingerprintMessage;
    public static String P4TrustDialog_InterceptWarning;
    public static String P4TrustDialog_NewConnectionMessage;
    public static String P4TrustDialog_NewKeyMessage;
    public static String P4TrustDialog_TrustChangedFingerprint;
    public static String P4TrustDialog_TrustError;
    public static String P4TrustDialog_TrustNewFingerprint;
    public static String P4UIUtils_DisplayingPerforceError;
    public static String P4UIUtils_ErrorOpeningEditor;
    public static String P4UIUtils_FindingContentType;

    public static String P4UIUtils_NameMustNotEmpty;
    public static String P4UIUtils_OpeningEditor;

    public static String P4UIUtils_QuoteCharNotAllowedInName;

    public static String P4UIUtils_RevisionCharacterNotAllowedInName;

    public static String P4UIUtils_SpaceNotAllowedInName;
    public static String PerforceContentProvider_FetchingChildren;
    public static String PerforceContentProvider_Loading;
    public static String PerforceLabelProvider_Change;
    public static String PerforceLabelProvider_Loading;
    public static String PerforceLabelProvider_ShelvedFiles;
    public static Object PerforceMarkerManager_ADDITION;
    public static String PerforceProjectSetSerializer_CreateFolderError;

	public static String PerforceProjectSetSerializer_CreatingProject;
    public static String PerforceProjectSetSerializer_DisplayingPerforceErrors;
    public static String PerforceProjectSetSerializer_ErrorCreatingProjectMessage;
    public static String PerforceProjectSetSerializer_ErrorCreatingProjectTitle;
    public static String PerforceProjectSetSerializer_FetchingLocalLocation;
    public static String PerforceProjectSetSerializer_ImportFailedMessage;
    public static String PerforceProjectSetSerializer_ImportFailedTitle;
    public static String PerforceProjectSetSerializer_ImportingFrom;
    public static String PerforceProjectSetSerializer_ImportingOneProject;
    public static String PerforceProjectSetSerializer_ImportingProjects;
    public static String PerforceProjectSetSerializer_LoadingProjectName;
    public static String PerforceProjectSetSerializer_ManagingProject;
    public static String PerforceProjectSetSerializer_Syncing;
    public static String PerforceProjectSetSerializer_UnableToConnect;

    public static String SharingWizard_MoveProjectActionLabel;
    public static String SharingWizard_failed;
    public static String SharingWizard_Error;
    public static String SharingWizard_InvalidProjects;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
