package com.perforce.team.ui.search.results.tree;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.search.results.tree.messages"; //$NON-NLS-1$
    public static String SearchTreeLabelProvider_NumMatches;
    public static String SearchTreeLabelProvider_NumRevisions;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
