package com.perforce.team.ui.streams;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.perforce.team.ui.streams.messages"; //$NON-NLS-1$
	public static String DeleteStreamHandler_Active_clients;
	public static String DeleteStreamHandler_Delete_stream;
	public static String StreamsFilterWidget_ShowUnloadedTaskStreamOnly;
    public static String FilteredStreamsViewer_AccessCol;
    public static String FilteredStreamsViewer_UpdateCol;
    public static String FilteredStreamsViewer_DescCol;
    public static String FilteredStreamsViewer_Loading;
    public static String FilteredStreamsViewer_OwnerCol;
    public static String FilteredStreamsViewer_ParentCol;
    public static String FilteredStreamsViewer_StreamCol;
    public static String FilteredStreamsViewer_StreamRootCol;
    public static String FilteredStreamsViewer_TypeCol;
    public static String FilteredStreamsViewer_UpdateStreamsJob;
    public static String StreamDetailPanel_ClientView;
    public static String StreamDetailPanel_DateModified;
    public static String StreamDetailPanel_Description;
    public static String StreamDetailPanel_Ignored;
    public static String StreamDetailPanel_LastAccessed;
    public static String StreamDetailPanel_OptFromParent;
    public static String StreamDetailPanel_Options;
    public static String StreamDetailPanel_OptLocked;
    public static String StreamDetailPanel_OptRestictOwner;
    public static String StreamDetailPanel_OptToParent;
    public static String StreamDetailPanel_Owner;
    public static String StreamDetailPanel_Parent;
    public static String StreamDetailPanel_Paths;
    public static String StreamDetailPanel_Remapped;
    public static String StreamDetailPanel_Stream;
    public static String StreamDetailPanel_StreamRoot;
    public static String StreamDetailPanel_Type;
    public static String StreamWizard_DepotMainlineStreamLocated;
    public static String StreamWizard_DepotTaskStreamLocated;
    public static String StreamWizard_ParentStreamToBranchFrom;
    public static String StreamWizard_ParentStreamToSourceFrom;
    public static String StreamsTreeViewer_ColumnTitleParent;
	public static String StreamsTreeViewer_ColumnTitleName;
	public static String StreamsTreeViewer_ColumnTitleRoot;
	public static String StreamsTreeViewer_ColumnTitleType;
	public static String StreamsTreeViewer_ColumnTitleDescription;
	public static String StreamsTreeViewer_Loading;
	public static String StreamsTreeViewer_ShowMore;
	public static String StreamsTreeViewer_ShowNumMore;
	public static String StreamsTreeViewer_LoadingStreams;
    public static String StreamsTreeViewer_LoadingStream;
    
    public static String StreamsFilterWidget_Depot;
    public static String StreamsFilterWidget_Name;
    public static String StreamsFilterWidget_Owner;
    public static String StreamsFilterWidget_Parent;
    public static String StreamsFilterWidget_Type;
    public static String StreamsFilterWidget_TypeToFilterChoices;
    public static String StreamsFilterWidget_ClearFilter;
    public static String StreamsFilterWidget_LoadingDepots;
    public static String StreamsSuggestProvider_Name;
    public static String StreamsSuggestProvider_Root;
    public static String StreamsSuggestProvider_Stream;
    public static String StreamsSuggestProvider_Unknown;
    public static String SuggestBox_Browse;
    public static String SuggestBox_Loading;
	public static String FilteredStreamsViewer_Matches;
    public static String FilteredStreamsViewer_Match;
    
    public static String Stream_Reload;
    public static String Stream_Unload;
    public static String Stream_Delete;
    public static String StreamOperation_Confirmation;
    public static String Stream_ActiveProjects;
	public static String StreamOperation_CurrentClientConfirmation;
    
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
