package com.perforce.team.ui.search.query;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.search.query.messages"; //$NON-NLS-1$
    public static String DepotPathDialog_Browse;
    public static String DepotPathDialog_DepotPath;
    public static String DepotPathDialog_EnterDepotPath;
    public static String DepotPathDialog_EnterDepotPath2;
    public static String DepotPathDialog_PathMustStartWithDepot;
    public static String P4SearchPage_CheckAll;
    public static String P4SearchPage_Connection;
    public static String P4SearchPage_LoadingSearchableConnections;
    public static String P4SearchPage_LoadingSearchableConnections2;
    public static String P4SearchPage_SearchDepotPaths;
    public static String P4SearchPage_SearchOptions;
    public static String P4SearchPage_SearchPattern;
    public static String P4SearchPage_SearchProjects;
    public static String P4SearchPage_UncheckAll;
    public static String P4SearchQuery_SearchingConnectionForPattern;
    public static String SearchOptionsArea_CaseInsensitive;
    public static String SearchOptionsArea_IncludeBinaryFiles;
    public static String SearchOptionsArea_LeadingContextLines;
    public static String SearchOptionsArea_SearchAllRevs;
    public static String SearchOptionsArea_TrailingContextLines;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
