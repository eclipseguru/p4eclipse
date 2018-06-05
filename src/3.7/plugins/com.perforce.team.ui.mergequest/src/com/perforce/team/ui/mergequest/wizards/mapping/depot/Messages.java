/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.depot;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.wizards.mapping.depot.messages"; //$NON-NLS-1$

    /**
     * DepotPathArea_BranchName
     */
    public static String DepotPathArea_BranchName;

    /**
     * DepotPathArea_DefaultName
     */
    public static String DepotPathArea_DefaultName;

    /**
     * DepotPathArea_DepotPath
     */
    public static String DepotPathArea_DepotPath;

    /**
     * DepotPathArea_EnterMappingName
     */
    public static String DepotPathArea_EnterMappingName;

    /**
     * DepotPathArea_EnterSourcePath
     */
    public static String DepotPathArea_EnterSourcePath;

    /**
     * DepotPathArea_EnterTargetPath
     */
    public static String DepotPathArea_EnterTargetPath;

    /**
     * DepotPathArea_MappingName
     */
    public static String DepotPathArea_MappingName;

    /**
     * DepotPathArea_NameCollision
     */
    public static String DepotPathArea_NameCollision;

    /**
     * DepotPathArea_NameInUse
     */
    public static String DepotPathArea_NameInUse;

    /**
     * DepotPathArea_PathsCannotBeSame
     */
    public static String DepotPathArea_PathsCannotBeSame;

    /**
     * DepotPathArea_Source
     */
    public static String DepotPathArea_Source;

    /**
     * DepotPathArea_SourcePathStartsWith
     */
    public static String DepotPathArea_SourcePathStartsWith;

    /**
     * DepotPathArea_Target
     */
    public static String DepotPathArea_Target;

    /**
     * DepotPathArea_TargetPathStartsWith
     */
    public static String DepotPathArea_TargetPathStartsWith;

    /**
     * DepotPathArea_ValidSourcePath
     */
    public static String DepotPathArea_ValidSourcePath;

    /**
     * DepotPathArea_ValidTargetDepotPath
     */
    public static String DepotPathArea_ValidTargetPath;

    /**
     * DepotPathMappingWizardPage_Description
     */
    public static String DepotPathMappingWizardPage_Description;

    /**
     * DepotPathMappingWizardPage_Title
     */
    public static String DepotPathMappingWizardPage_Title;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
