package com.perforce.team.ui.preferences.decorators;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.preferences.decorators.messages"; //$NON-NLS-1$
    public static String LabelPreviewPreferencePage_AddVariables;
    public static String LabelPreviewPreferencePage_BottomLeft;
    public static String LabelPreviewPreferencePage_BottomRight;
    public static String LabelPreviewPreferencePage_DecorateIgnoredResources;
    public static String LabelPreviewPreferencePage_DisplayIconForFilesNotManagedByEclipse;
    public static String LabelPreviewPreferencePage_FileDecoration;
    public static String LabelPreviewPreferencePage_General;
    public static String LabelPreviewPreferencePage_IconDecorations;
    public static String LabelPreviewPreferencePage_IgnoredFolder;
    public static String LabelPreviewPreferencePage_IndicatorForIgnoredResources;
    public static String LabelPreviewPreferencePage_IndicatorForLocked;
    public static String LabelPreviewPreferencePage_IndicatorForNotSyncedToHead;
    public static String LabelPreviewPreferencePage_IndicatorForNotUnderVersionControl;
    public static String LabelPreviewPreferencePage_IndicatorForOpenedByOther;
    public static String LabelPreviewPreferencePage_IndicatorForOpenForAddEditDelete;
    public static String LabelPreviewPreferencePage_IndicatorForSharedProjects;
    public static String LabelPreviewPreferencePage_IndicatorForSyncedToHead;
    public static String LabelPreviewPreferencePage_IndicatorForUnresolved;
    public static String LabelPreviewPreferencePage_None;
    public static String LabelPreviewPreferencePage_NotUnderVersionControlDecoration;
    public static String LabelPreviewPreferencePage_OfflineProject;
    public static String LabelPreviewPreferencePage_OnlineProject;
    public static String LabelPreviewPreferencePage_OutgoingChangeFlag;
    public static String LabelPreviewPreferencePage_Preview;
    public static String LabelPreviewPreferencePage_ProjectDecoration;
	public static String LabelPreviewPreferencePage_ConnectionDecoration;
    public static String LabelPreviewPreferencePage_SandboxProject;
    public static String LabelPreviewPreferencePage_SandboxStreamName;
	public static String LabelPreviewPreferencePage_SandboxStreamRoot;
	public static String LabelPreviewPreferencePage_ShowCurrentChangelistInSyncView;
    public static String LabelPreviewPreferencePage_TextDecorations;
    public static String LabelPreviewPreferencePage_ToEnablePerforceDecoration;
    public static String LabelPreviewPreferencePage_TopLeft;
    public static String LabelPreviewPreferencePage_TopRight;
    public static String VariablesDialog_AddVariables;
    public static String VariablesDialog_SelectVariablesToAdd;
	public static String LabelPreviewPreferencePage_IndicatorForStreamAndSandboxConnection;
	public static String LabelPreviewPreferencePage_IndicatorForStreamAndSandboxProject;
    public static String PreviewConnection_StreamName;
	public static String PreviewConnection_StreamRoot;
	static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
