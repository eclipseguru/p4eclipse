package com.perforce.team.ui.search.results.table;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.search.results.table.messages"; //$NON-NLS-1$
    public static String SearchTableLabelProvider_NumMatches;
    public static String SearchTableLabelProvider_NumRevisions;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
