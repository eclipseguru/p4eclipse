package com.perforce.team.ui.timelapse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.timelapse.messages"; //$NON-NLS-1$
    public static String TimeLapseAction_NoTimelapseMessage;
    public static String TimeLapseAction_NoTimelapseTitle;
    public static String TimeLapseAction_OpeningTimelapse;
    public static String TimeLapseChangelistWidget_Action;
    public static String TimeLapseChangelistWidget_Changelist;
    public static String TimeLapseChangelistWidget_DateSubmitted;
    public static String TimeLapseChangelistWidget_Description;
    public static String TimeLapseChangelistWidget_PerforceFiletype;
    public static String TimeLapseChangelistWidget_Revision;
    public static String TimeLapseChangelistWidget_Submitted;
    public static String TimeLapseChangelistWidget_Workspace;
    public static String TimeLapseEditor_DisplayActionIcons;
    public static String TimeLapseEditor_DisplayRevisionDetails;
    public static String TimeLapseEditor_LoadingHistory;
    public static String TimeLapseEditor_LoadingTimelapseView;
    public static String TimeLapseEditor_LoadingTimelapseViewFor;
    public static String TimeLapseEditor_ShowBranchingHistory;
    public static String TimeLapseEditor_WrongInputType;
    public static String TimeLapseInput_TimelapseOf;
    public static String TimeLapsePreferencePage_AvailableContentTypeSpecificTimelapseViews;
    public static String TimeLapsePreferencePage_ShowBranchingHistory;
    public static String TimeLapsePreferencePage_ShowChangelistDetails;
    public static String TimeLapsePreferencePage_ShowFileActionsInSlider;
    public static String TimeLapsePreferencePage_UseInternalTimelapse;
    public static String TimeLapsePreferencePage_UseP4VTimelapse;
    public static String TimeLapseSlider_Changelists;
    public static String TimeLapseSlider_Date;
    public static String TimeLapseSlider_Revisions;
    public static String TimeLapseSlider_Scale;
    public static String TimeLapseSlider_ShowNextRev;
    public static String TimeLapseSlider_ShowPreviousRev;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
