package com.perforce.team.core.p4java.builder;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.perforce.team.core.p4java.builder.messages"; //$NON-NLS-1$
	public static String ClientBuilder_CreateFolderError;
	public static String ClientBuilder_RetrieveServerError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
