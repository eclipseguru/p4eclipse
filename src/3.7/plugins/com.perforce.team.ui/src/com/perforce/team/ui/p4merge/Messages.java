package com.perforce.team.ui.p4merge;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.p4merge.messages"; //$NON-NLS-1$
    public static String ApplicationRunner_ConfigureAppMessage;
    public static String ApplicationRunner_ConfigureAppTitle;
    public static String ApplicationRunner_DisplayAppError;
    public static String ApplicationRunner_ErrorExecutingAppMessage;
    public static String ApplicationRunner_ErrorExecutingAppTitle;
    public static String ApplicationRunner_RunningApp;
    public static String MergeRunner_MergeFileUsedForResolve;
    public static String MergeRunner_ReplaceMergedMessage;
    public static String MergeRunner_ReplaceMergedTitle;
    public static String MergeRunner_Resolving;
    public static String MergeRunner_Theirs;
    public static String MergeRunner_Yours;
    public static String P4MergeDiffAction_WorkspaceFile;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
