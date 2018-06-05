package com.perforce.team.ui.p4v;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.p4v.messages"; //$NON-NLS-1$
    public static String RevisionGraphRunner_ErrorMessage;
    public static String RevisionGraphRunner_ErrorTitle;
    public static String RevisionGraphRunner_Title;
    public static String StreamsRunner_ErrorMessage;
    public static String StreamsRunner_ErrorTitle;
    public static String StreamsRunner_Title;
    public static String TimeLapseRunner_ErrorMessage;
    public static String TimeLapseRunner_ErrorTitle;
    public static String TimeLapseRunner_Title;
    public static String P4SandboxConfigRunner_ErrorTitle;
    public static String P4SandboxConfigRunner_ErrorMessage;
    public static String P4SandboxConfigRunner_Title;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
