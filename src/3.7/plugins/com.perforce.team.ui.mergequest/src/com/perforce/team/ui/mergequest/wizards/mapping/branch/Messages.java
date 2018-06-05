/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.wizards.mapping.branch.messages"; //$NON-NLS-1$

    /**
     * BranchArea_BranchName
     */
    public static String BranchArea_BranchName;

    /**
     * BranchArea_BranchSpecName
     */
    public static String BranchArea_BranchSpecName;

    /**
     * BranchArea_Browse
     */
    public static String BranchArea_Browse;

    /**
     * BranchArea_DepotPaths
     */
    public static String BranchArea_DepotPaths;

    /**
     * BranchArea_EnterSpecName
     */
    public static String BranchArea_EnterSpecName;

    /**
     * BranchArea_LoadingSpec
     */
    public static String BranchArea_LoadingSpec;

    /**
     * BranchArea_LoadViewToolTip
     */
    public static String BranchArea_LoadViewToolTip;

    /**
     * BranchArea_New
     */
    public static String BranchArea_New;

    /**
     * BranchArea_Source
     */
    public static String BranchArea_Source;

    /**
     * BranchArea_Target
     */
    public static String BranchArea_Target;

    /**
     * BranchAssistant_NoBranchesFound
     */
    public static String BranchAssistant_NoBranchesFound;

    /**
     * BranchAssistant_Searching
     */
    public static String BranchAssistant_Searching;

    /**
     * BranchMappingWizardPage_Description
     */
    public static String BranchMappingWizardPage_Description;

    /**
     * BranchMappingWizardPage_Title
     */
    public static String BranchMappingWizardPage_Title;

    /**
     * LoadingProposal_Loading
     */
    public static String LoadingProposal_Loading;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
