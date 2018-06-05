/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.outline;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.editor.outline.messages"; //$NON-NLS-1$

    /**
     * BranchGraphOutlinePage_CollapseAll
     */
    public static String BranchGraphOutlinePage_CollapseAll;

    /**
     * BranchMappingContentProvider_LoadingSpec
     */
    public static String BranchMappingContentProvider_LoadingSpec;

    /**
     * BranchMappingLabelProvider_Count
     */
    public static String BranchMappingLabelProvider_Count;

    /**
     * BranchMappingLabelProvider_MappingDetails
     */
    public static String BranchMappingLabelProvider_MappingDetails;

    /**
     * MappingProxy_From
     */
    public static String MappingProxy_From;

    /**
     * MappingProxy_To
     */
    public static String MappingProxy_To;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
