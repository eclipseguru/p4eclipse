/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.text.timelapse.messages"; //$NON-NLS-1$

    public static String NodeModel_DeleteFileError;

	public static String NodeModelTimeLapseEditor_FilterSliderTickMarksBySelection;
    public static String NodeOutlinePage_ClearSelection;
    public static String NodeOutlinePage_LoadingTimelapseOutline;
    public static String NodeTimeLapseEditor_ClearFilter;
    public static String NodeTimeLapseEditor_DisablingFolding;
    public static String NodeTimeLapseEditor_EnablingFolding;
    public static String NodeTimeLapseEditor_ToggleFolding;
    public static String TextPreferenceHandler_10;
    public static String TextPreferenceHandler_DisplayAuthorInfo;
    public static String TextPreferenceHandler_DisplayCodeFolding;
    public static String TextPreferenceHandler_DisplayRevisionInfo;
    public static String TextPreferenceHandler_DisplayTextAging;
    public static String TextPreferenceHandler_GenerateMethodHistory;
    public static String TextPreferenceHandler_IgnoreLineEnding;
    public static String TextPreferenceHandler_IgnoreLineEndingAndAllWS;
    public static String TextPreferenceHandler_IgnoreLineEndingAndWS;
    public static String TextPreferenceHandler_LeastRecentTextBGColor;
    public static String TextPreferenceHandler_LinkEditorWithOutlineViewSelection;
    public static String TextPreferenceHandler_NodeChangeFGColor;
    public static String TextPreferenceHandler_OnlyShowRevsWhereSelectedNodeChanges;
    public static String TextPreferenceHandler_RecognizeLineEndingAndWS;
    public static String TextPreferenceHandler_WhitespaceOptions;
    public static String TextTimeLapseEditor_ConfigureLeastRecentTextColor;
    public static String TextTimeLapseEditor_ConfigureMostRexentTextColor;
    public static String TextTimeLapseEditor_DisplayTextAging;
    public static String TextTimeLapseEditor_IgnoreLineEnding;
    public static String TextTimeLapseEditor_IgnoreLineEndingAndAllWS;
    public static String TextTimeLapseEditor_IgnoreLineEndingAndWS;
    public static String TextTimeLapseEditor_LoadP4Annotate;
    public static String TextTimeLapseEditor_RecognizeLineEndingAndWS;
    public static String TextTimeLapseEditor_ShowAuthorsInRuler;
    public static String TextTimeLapseEditor_ShowRangesInRuler;
    public static String TextTimeLapseEditor_TextAging;
    public static String TextTimeLapseEditor_WhitespaceOptions;

	public static String TextTimeLapseEditor_GenerateHistoryByAnalyzing;

    /**
     * Resource bundle
     */
    public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);
  
    static {
  		// initialize resource bundles
  		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  	}
  	
    private Messages() {
    }

}
