/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.wizards.mapping.messages"; //$NON-NLS-1$

    /**
     * MappingArea_BothDirections
     */
    public static String MappingArea_BothDirections;

    /**
     * MappingArea_ContentAssistAvailable
     */
    public static String MappingArea_ContentAssistAvailable;

    /**
     * MappingArea_EnterSourceName
     */
    public static String MappingArea_EnterSourceName;

    /**
     * MappingArea_EnterTargetName
     */
    public static String MappingArea_EnterTargetName;

    /**
     * MappingArea_SelectSourceBranchType
     */
    public static String MappingArea_SelectSourceBranchType;

    /**
     * MappingArea_SelectTargetBranchType
     */
    public static String MappingArea_SelectTargetBranchType;

    /**
     * MappingArea_SourceAndTargetMustDiffer
     */
    public static String MappingArea_SourceAndTargetMustDiffer;

    /**
     * MappingArea_SourceToTarget
     */
    public static String MappingArea_SourceToTarget;

    /**
     * MappingArea_TragetToSource
     */
    public static String MappingArea_TargetToSource;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
