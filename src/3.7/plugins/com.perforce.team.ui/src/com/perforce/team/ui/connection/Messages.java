package com.perforce.team.ui.connection;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.connection.messages"; //$NON-NLS-1$
    public static String AbstractConnectionWizard_CheckoutFolder;
	public static String AbstractConnectionWizard_ImportingProjects;
	public static String BasicConnectionWidget_Charset;
    public static String BasicConnectionWidget_Client;
    public static String BasicConnectionWidget_InvalidServerAddress;
    public static String BasicConnectionWidget_MustSpecifyCharset;
    public static String BasicConnectionWidget_MustSpecifyClient;
    public static String BasicConnectionWidget_MustSpecifyPort;
    public static String BasicConnectionWidget_MustSpecifyUser;
    public static String BasicConnectionWidget_ServerAddress;
    public static String BasicConnectionWidget_ServerConnection;
    public static String BasicConnectionWidget_User;
    public static String ClientWizardPage_Browse;
    public static String ClientWizardPage_ChooseClientMessage;
    public static String ClientWizardPage_ChooseClientTitle;
    public static String ClientWizardPage_Client;
    public static String ClientWizardPage_ClientDoesNotExistMessage;
    public static String ClientWizardPage_ClientDoesNotExistTitle;
    public static String ClientWizardPage_ClientExistsMessage;
    public static String ClientWizardPage_ClientExistsTitle;
	public static String ClientWizardPage_ClientNameInvalid;
    public static String ClientWizardPage_CreateNewWorkspace;
    public static String ClientWizardPage_Error;
	public static String ClientWizardPage_ExistingWorkspaceName;
    public static String ClientWizardPage_FetchingClientsFor;
    public static String ClientWizardPage_FolderWillBeCreatedHere;
    public static String ClientWizardPage_Host;
	public static String ClientWizardPage_HostOnly;
	public static String ClientWizardPage_LaunchPerforceProjectImportWizard;
	public static String ClientWizardPage_Loading;
    public static String ClientWizardPage_Location;
    public static String ClientWizardPage_MustEnterClientName;
    public static String ClientWizardPage_MustEnterValidDirectory;
    public static String ClientWizardPage_NoSpacesAllowed;
    public static String ClientWizardPage_RefreshWorkspaces;
    public static String ClientWizardPage_Root;
    public static String ClientWizardPage_Stream;
    public static String ClientWizardPage_StreamInvalid;
    public static String ClientWizardPage_SelectExistingWorkspace;
    public static String ClientWizardPage_UpdatingClientTable;
    public static String ClientWizardPage_Workspace;
    public static String ClientWizardPage_WorkspaceName;
    public static String ConnectionWizard_AddingConnection;
    public static String ConnectionWizard_ClientExistsMessage;
    public static String ConnectionWizard_ClientExistsTitle;
    public static String ConnectionWizard_CreatingClient;
    public static String ConnectionWizard_ErrorCreatingClient;
    public static String ConnectionWizard_PerforceConnectionWizard;
	public static String ConnectionWizard_PrepareForImportingProjects;
    public static String ConnectionWizard_SavingConnection;
    public static String ConnectionWizard_SavingServerHistory;
    public static String ConnectionWizard_SettingUpProjectImport;
    public static String SelectConnectionWizardPage_ClientRootNotExist;
	public static String SelectConnectionWizardPage_ClientRootCannotBeNull;
    public static String SelectConnectionWizardPage_ClientRootNotWritable;
    public static String SelectConnectionWizardPage_CreateNewConnection;
    public static String SelectConnectionWizardPage_DefineConnection;
    public static String SelectConnectionWizardPage_EnterConnectionInfo;
    public static String SelectConnectionWizardPage_SelectOrCreateConnection;
    public static String SelectConnectionWizardPage_UseExistingConnection;
    public static String ServerWizardPage_AddConnectionMessage;
    public static String ServerWizardPage_AddConnectionTitle;
    public static String ServerWizardPage_Authentication;
    public static String ServerWizardPage_Charset;
    public static String ServerWizardPage_ConnectedMessage;
    public static String ServerWizardPage_ConnectedTitle;
    public static String ServerWizardPage_ErrorConnectingMessage;
    public static String ServerWizardPage_ErrorConnectionTitle;
    public static String ServerWizardPage_Location;
    public static String ServerWizardPage_MustSpecifyServer;
    public static String ServerWizardPage_MustSpecifyUser;
    public static String ServerWizardPage_Password;
    public static String ServerWizardPage_RecentServers;
    public static String ServerWizardPage_SavePassword;
    public static String ServerWizardPage_Server;
    public static String ServerWizardPage_Test;
    public static String ServerWizardPage_User;
	public static String SelectConnectionWizardPage_ServerConnection;
	public static String SelectConnectionWizardPage_NewServerConnection;
	public static String SelectConnectionWizardPage_ClientRoot;
	public static String SelectConnectionWizardPage_TargetClientPath;
	public static String SelectConnectionWizardPage_Browse;
	public static String SelectConnectionWizardPage_ValidateClientPath;
	public static String SelectConnectionWizardPage_ValidateProjects;
	public static String SelectConnectionWizardPage_InvalidClientPath;
	public static String SelectConnectionWizardPage_ProjectNameColumnHeader;
	public static String SelectConnectionWizardPage_CurrentLocationColumnHeader;
	public static String SelectConnectionWizardPage_NewLocationTargetHeader;
	public static String SelectConnectionWizardPage_SelectProjects;
	public static String SelectConnectionWizardPage_Error;
	public static String SelectConnectionWizardPage_CannotCreateClientRoot_ReadOnly;
	public static String SelectConnectionWizardPage_CannotCreateClientRoot;
	public static String SelectConnectionWizardPage_CreateClientRootDialog_Title;
	public static String SelectConnectionWizardPage_CreateClientRootDialog_Question;
	public static String SelectConnectionWizardPage_Error_RelativePathNotInClientRoot;
	public static String SelectConnectionWizardPage_FetchingRoot;
	
    public static String SelectConnectionWizardPage_InvalidProjects;
    public static String SelectConnectionWizardPage_Relocate;
	public static String ServerWizardPage_TestingConnection;
    public static String ClientWizardPage_StreamLabel;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
