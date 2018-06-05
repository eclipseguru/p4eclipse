package com.perforce.team.core.p4java.synchronize;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.core.p4java.synchronize.messages"; //$NON-NLS-1$
    public static String DefaultChange;
    public static String Loading;
    public static String PendingChange;
    public static String PerforceSynchronizations;
    public static String Refreshing;
    public static String SubmittedChange;
    public static String Synchronizing;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
