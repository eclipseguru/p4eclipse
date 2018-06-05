package com.perforce.team.ui.pending;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.pending.messages"; //$NON-NLS-1$
    public static String PendingChangelistFileWidget_DisplayCheckedFilesOnly;
    public static String PendingChangelistFileWidget_FilesNumSelected;
    public static String PendingListener_UpdatingPendingChangelistView;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
