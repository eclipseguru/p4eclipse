/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.changeset;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mylyn.changeset.messages"; //$NON-NLS-1$

    /**
     * P4ContextChangeSet_FixJobChangelist
     */
    public static String P4ContextChangeSet_FixJobChangelist;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
