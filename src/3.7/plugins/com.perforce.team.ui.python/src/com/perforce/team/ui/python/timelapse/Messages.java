package com.perforce.team.ui.python.timelapse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.python.timelapse.messages"; //$NON-NLS-1$
    public static String PythonOutlinePage_Sort;
    public static String PythonTimeLapseEditor_LinkWithOutlineViewSelection;
    public static String PythonTimeLapseEditor_Methods;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
