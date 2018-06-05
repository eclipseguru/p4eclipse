package com.perforce.team.ui.actions.stub;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.actions.stub.messages"; //$NON-NLS-1$


	public static String TeamAction_errorTitle;
	public static String TeamAction_handlerNotEnabledTitle;
	public static String TeamAction_handlerNotEnabledMessage;


	public static String TeamAction_internal;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
