package com.perforce.team.ui.java.timelapse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.java.timelapse.messages"; //$NON-NLS-1$
    public static String JavaOutlinePage_Sort;
    public static String JavaTimeLapseEditor_LinkWithOutlineViewSelection;
    public static String JavaTimeLapseEditor_Methods;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
