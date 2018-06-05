package com.perforce.team.ui.streams.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.streams.wizard.messages"; //$NON-NLS-1$
    public static String AdvancedSettingPage_CannotCreateSelfParentStream;
    public static String AdvancedSettingPage_DepotPath;
    public static String AdvancedSettingPage_Description;
    public static String AdvancedSettingPage_IgnoredPaths;
    public static String AdvancedSettingPage_Owner;
    public static String AdvancedSettingPage_RemmappedPaths;
    public static String AdvancedSettingPage_RootFolder;
    public static String AdvancedSettingPage_StreamFormatIncorrect;
    public static String AdvancedSettingPage_StreamLockedToOwner;
    public static String AdvancedSettingPage_StreamOwnerEmptyError;
    public static String AdvancedSettingPage_StreamRootEmptyError;
    public static String AdvancedSettingPage_StreamViewPaths;
    public static String AdvancedSettingPage_SubmitRestrictionNeedMadeInSourceStream;
    public static String AdvancedSettingPage_SubmittingRestrictToOwner;
    public static String AdvancedSettingPage_Title;
    public static String BasicSettingPage_ChangePropagation;
    public static String BasicSettingPage_Description;
    public static String BasicSettingPage_NameEmptyError;
    public static String BasicSettingPage_ParentStreamNotExistError;
    public static String BasicSettingPage_StreamDepotNotSet;
    public static String BasicSettingPage_StreamDescription;
    public static String BasicSettingPage_CreateWorkspace;
    public static String BasicSettingPage_BranchFromParent;
    public static String BasicSettingPage_PopulateTaskStreamFromDepot;
    public static String BasicSettingPage_PopulateMainStreamFromDepot;
    public static String BasicSettingPage_StreamName;
    public static String BasicSettingPage_StreamType;
    public static String BasicSettingPage_Title;
    public static String BasicSettingPage_TypeEmptyError;
    public static String EditStreamWizard_Edit;
    public static String EditStreamWizard_Error;
    public static String EditStreamWizard_New;
    public static String EditStreamWizard_StreamWizardTitle;
    public static String EditStreamWizard_LockedBy;
    public static String EditStreamWizard_ConvertOrphanToMain;
    public static String EditStreamWizard_ConvertOrphanWarn;
    public static String EditStreamWizard_CreatedBy;
    public static String PropagationOptionWidget_AllowCopyFromParent;
    public static String PropagationOptionWidget_AllowCopyToParent;
    public static String PropagationOptionWidget_AllowMergeFromParent;
    public static String PropagationOptionWidget_AllowMergeToParent;
    public static String PropagationOptionWidget_MainlineStreamDescription;
    public static String PropagationOptionWidget_VirtualStreamDescription;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
