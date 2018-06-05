package com.perforce.team.ui.search.results;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.search.results.messages"; //$NON-NLS-1$
    public static String P4SearchResult_TextNumMatchesInConnection;
    public static String P4SearchResultPage_CompressedMode;
    public static String P4SearchResultPage_FlatMode;
    public static String P4SearchResultPage_TreeMode;
    public static String RevisionMatch_Revision;
    public static String SearchLabelDecorator_Have;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
