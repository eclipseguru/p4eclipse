package com.perforce.team.ui.resource;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.resource.messages"; //$NON-NLS-1$
    public static String ResourceBrowserDialog_Refresh;
    public static String ResourceBrowserDialog_SelectAResource;
    public static String ResourceBrowserWidget_SelectAResource;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
