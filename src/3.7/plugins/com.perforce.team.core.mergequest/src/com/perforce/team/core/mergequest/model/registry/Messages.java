package com.perforce.team.core.mergequest.model.registry;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.core.mergequest.model.registry.messages"; //$NON-NLS-1$
    public static String BranchType_1;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
