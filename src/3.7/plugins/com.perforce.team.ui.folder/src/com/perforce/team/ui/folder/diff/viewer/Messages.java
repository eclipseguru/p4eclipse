/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.viewer;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.folder.diff.viewer.messages"; //$NON-NLS-1$

    /**
     * DiffArea_SingleUniqueFile
     */
    public static String DiffArea_MultipleUniqueFiles;

    /**
     * DiffArea_SingleUniqueFile
     */
    public static String DiffArea_SingleUniqueFile;

    /**
     * DiffArea_SubmittedChangelistDetails
     */
    public static String DiffArea_SubmittedChangelistDetails;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
