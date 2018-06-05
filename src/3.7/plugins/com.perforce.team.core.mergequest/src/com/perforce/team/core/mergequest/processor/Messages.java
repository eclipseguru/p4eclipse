/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.processor;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.core.mergequest.processor.messages"; //$NON-NLS-1$

    /**
     * InterchangesProcessor_RefreshingChange
     */
    public static String InterchangesProcessor_RefreshingChange;

    /**
     * InterchangesProcessor_RefreshingConnectors
     */
    public static String InterchangesProcessor_RefreshingConnectors;

    /**
     * InterchangesProcessor_RefreshingInterchanges
     */
    public static String InterchangesProcessor_RefreshingInterchanges;

    /**
     * InterchangesProcessor_RefreshingStatus
     */
    public static String InterchangesProcessor_RefreshingStatus;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
