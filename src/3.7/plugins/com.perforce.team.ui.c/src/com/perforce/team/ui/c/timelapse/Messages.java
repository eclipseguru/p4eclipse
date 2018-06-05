package com.perforce.team.ui.c.timelapse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.c.timelapse.messages"; //$NON-NLS-1$
    public static String CContextHandler_NoCProjectsFoundMessage;
    public static String CContextHandler_NoCProjectsFoundTitle;
    public static String COutlinePage_Sort;
    public static String CTimeLapseEditor_Functions;
    public static String CTimeLapseEditor_LinkWithOutlineViewSelection;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
