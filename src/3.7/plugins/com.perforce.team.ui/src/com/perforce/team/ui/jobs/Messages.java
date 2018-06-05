package com.perforce.team.ui.jobs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.jobs.messages"; //$NON-NLS-1$
    public static String BaseJobDialog_Add;
    public static String BaseJobDialog_Browse;
    public static String BaseJobDialog_Changelist;
    public static String BaseJobDialog_ChangelistNotFoundMessage;
    public static String BaseJobDialog_ChangelistNotFoundTitle;
    public static String BaseJobDialog_InvalidChangelistMessage;
    public static String BaseJobDialog_InvalidChangelistTitle;
    public static String BaseJobDialog_Job;
    public static String BaseJobDialog_NoneSelected;
    public static String EditJobDialog_EditJobTitle;
    public static String NewJobDialog_NewJob;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
