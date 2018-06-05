package com.perforce.team.ui.diff;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.diff.messages"; //$NON-NLS-1$
    public static String BaseFileDiffer_Left;
    public static String BaseFileDiffer_Right;
    public static String DiffContentProvider_GettingFileDifferences;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
