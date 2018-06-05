package com.perforce.team.ui.text.timelapse.form;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.text.timelapse.form.messages"; //$NON-NLS-1$
    public static String FormOutlinePage_Sort;
    public static String FormTimeLapseEditor_DisplayComments;
    public static String FormTimeLapseEditor_Fields;
    public static String FormTimeLapseEditor_LinkWithOutlineViewSelection;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
