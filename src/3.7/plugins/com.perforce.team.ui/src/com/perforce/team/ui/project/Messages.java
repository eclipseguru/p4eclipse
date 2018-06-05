package com.perforce.team.ui.project;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.project.messages"; //$NON-NLS-1$
    public static String ConnectionSelectionPage_ChoosePerforceConnectionDescription;
    public static String ConnectionSelectionPage_ChoosePerforceConnectionTitle;
    public static String ConnectionSelectionPage_ConfigureNewConnection;
    public static String ConnectionSelectionPage_SelectExistingConnection;
    public static String ConnectionSelectionPage_UseExistingConnection;
    public static String ImportProjectAsAction_EnterProjectName;
    public static String ImportProjectsWizard_CheckoutFolder;
	public static String ImportProjectsWizard_ImportingProjects;
	public static String ImportProjectsWizardPage_ChooseProjectsDescription;
    public static String ImportProjectsWizardPage_ChooseProjectsTitle;
    public static String ImportProjectsWizardPage_CreatingConnection;
	public static String ImportProjectsWizardPage_EclipseProjectFolders;
    public static String ImportProjectsWizardPage_FoldersNotInClientView;
    public static String ImportProjectsWizardPage_ImportExistingDepotFolders;
	public static String ImportProjectsWizardPage_ImportProjects;
	public static String ImportProjectsWizardPage_ImportProjectsHelpMessage;
	public static String ImportProjectsWizardPage_ImportSubFoldersAsProjects;
	public static String ImportProjectsWizardPage_InvalidOperation;
	public static String ImportProjectsWizardPage_NoSubFolderAvailableError;
	public static String ImportProjectsWizardPage_ProjectsToBeImpoted;
	public static String ImportProjectsWizardPage_RemoteFolders;
    public static String ImportProjectsWizardPage_SelectAtLeastOneFolder;
	public static String ImportProjectsWizardPage_TotalProjects;
    public static String ImportProjectsWizardPage_UpdateStatus;
    public static String ImportProjectsWizardPage_ValidatingFolders;
    public static String ProjectNameDialog_ProjectAlreadyExists;
    public static String ProjectNameDialog_ProjectAlreadyExists2;
    public static String ProjectNameDialog_ProjectName;
    public static String ProjectNameDialog_ProjectNameInUse;
    public static String ProjectNameDialog_ProjectNameInvalid;
    public static String ReuseConnectionWidget_ReuseSettingsForThisServer;
    public static String ShareProjectsDialog_ShareProjects;
    public static String ShareProjectsWidget_Connection;
    public static String ShareProjectsWidget_DeselectAll;
    public static String ShareProjectsWidget_MustSelectAtLeastOneProject;
    public static String ShareProjectsWidget_NoUnsharedProjectsInWorkspace;
    public static String ShareProjectsWidget_SelectAll;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
