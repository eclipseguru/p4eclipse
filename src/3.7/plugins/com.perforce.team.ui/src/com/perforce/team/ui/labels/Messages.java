package com.perforce.team.ui.labels;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.labels.messages"; //$NON-NLS-1$
    public static String LabelFilesDialog_LabelFiles;
    public static String LabelFilesWidget_ApplySelectedLabelToFiles;
    public static String LabelFilesWidget_AtLatestRevision;
    public static String LabelFilesWidget_Browse;
    public static String LabelFilesWidget_Changelist;
    public static String LabelFilesWidget_DateTime;
	public static String LabelFilesWidget_Label;
    public static String LabelFilesWidget_Files;
    public static String VersionWidget_MustEnterChangelistNumber;
    public static String VersionWidget_MustEnterDate;
    public static String VersionWidget_MustEnterLabelName;
    public static String VersionWidget_MustEnterRevisionNumber;
    public static String LabelFilesWidget_Name;
    public static String LabelFilesWidget_RemoveSelectedLabelFromFiles;
    public static String LabelFilesWidget_Revision;
    public static String LabelFilesWidget_SpecifyRevision;
    public static String LabelsView_GetRevision;
    public static String LabelsView_Labels;
    public static String LabelsView_OpenLabelPrefs;
    public static String LabelsView_Refresh;
    public static String LabelsView_RefreshLabels;
    public static String LabelsView_ShowLabelDetails;
    public static String LabelsViewer_AccessTime;
    public static String LabelsViewer_ClearFolderFile;
    public static String LabelsViewer_ClearNameFilter;
    public static String LabelsViewer_ClearOwnerFilter;
    public static String LabelsViewer_Description;
    public static String LabelsViewer_FolderFile;
    public static String LabelsViewer_Label;
    public static String LabelsViewer_Loading;
    public static String LabelsViewer_LoadingLabel;
    public static String LabelsViewer_LoadingLabels;
    public static String LabelsViewer_NameContains;
    public static String LabelsViewer_Owner;
    public static String LabelsViewer_OwnerLabel;
    public static String LabelsViewer_ShowMore;
    public static String LabelsViewer_ShowNumMore;
    public static String LabelsViewer_UpdatingLabel;
    public static String LabelsViewer_UpdatingLabelsView;
    public static String LabelWidget_AccessTime;
    public static String LabelWidget_Description;
    public static String LabelWidget_Locked;
    public static String LabelWidget_Name;
    public static String LabelWidget_Options;
    public static String LabelWidget_Owner;
    public static String LabelWidget_Revision;
    public static String LabelWidget_UpdateTime;
    public static String LabelWidget_View;
    public static String SelectLabelDialog_SelectLabel;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
