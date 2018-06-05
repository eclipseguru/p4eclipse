/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.core.mergequest.builder.messages"; //$NON-NLS-1$

    /**
     * XmlBranchGraphBuilder_ErrorSaving
     */
    public static String XmlBranchGraphBuilder_ErrorSaving;

    /**
     * XmlBranchGraphBuilder_ErrorLoading
     */
    public static String XmlBranchGraphBuilder_ErrorLoading;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
