/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.properties;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.properties.messages"; //$NON-NLS-1$

    /**
     * BranchPropertySource_Height
     */
    public static String BranchPropertySource_Height;

    /**
     * BranchPropertySource_Id
     */
    public static String BranchPropertySource_Id;

    /**
     * BranchPropertySource_Name
     */
    public static String BranchPropertySource_Name;

    /**
     * BranchPropertySource_Type
     */
    public static String BranchPropertySource_Type;

    /**
     * BranchPropertySource_Width
     */
    public static String BranchPropertySource_Width;

    /**
     * BranchPropertySource_X
     */
    public static String BranchPropertySource_X;

    /**
     * BranchPropertySource_Y
     */
    public static String BranchPropertySource_Y;

    /**
     * MappingPropertySource_Direction
     */
    public static String MappingPropertySource_Direction;

    /**
     * MappingPropertySource_Id
     */
    public static String MappingPropertySource_Id;

    /**
     * MappingPropertySource_LatestSource
     */
    public static String MappingPropertySource_LatestSource;

    /**
     * MappingPropertySource_LatestTarget
     */
    public static String MappingPropertySource_LatestTarget;

    /**
     * MappingPropertySource_Name
     */
    public static String MappingPropertySource_Name;

    /**
     * MappingPropertySource_SourceCount
     */
    public static String MappingPropertySource_SourceCount;

    /**
     * MappingPropertySource_SourceName
     */
    public static String MappingPropertySource_SourceName;

    /**
     * MappingPropertySource_TargetCount
     */
    public static String MappingPropertySource_TargetCount;

    /**
     * MappingPropertySource_TargetName
     */
    public static String MappingPropertySource_TargetName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
