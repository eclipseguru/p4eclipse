package com.perforce.team.ui.ruby.timelapse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.ruby.timelapse.messages"; //$NON-NLS-1$
    public static String RubyOutlinePage_Sort;
    public static String RubyTimeLapseEditor_LinkWithOutlineViewSelection;
    public static String RubyTimeLapseEditor_Methods;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
