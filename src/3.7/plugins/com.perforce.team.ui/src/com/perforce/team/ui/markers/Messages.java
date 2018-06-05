package com.perforce.team.ui.markers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.markers.messages"; //$NON-NLS-1$
    public static String AddSourceResolution_AddResourceToPerforce;
    public static String IgnoreResolution_AddToP4Ignore;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
