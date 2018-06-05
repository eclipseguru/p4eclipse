/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.wizards.branch.messages"; //$NON-NLS-1$

    /**
     * BranchWizardPage_AddNewBranch
     */
    public static String BranchWizardPage_AddNewBranch;

    /**
     * BranchWizardPage_BranchExists
     */
    public static String BranchWizardPage_BranchExists;

    /**
     * BranchWizardPage_EnterBranchName
     */
    public static String BranchWizardPage_EnterBranchName;

    /**
     * BranchWizardPage_Name
     */
    public static String BranchWizardPage_Name;

    /**
     * BranchWizardPage_NewBranch
     */
    public static String BranchWizardPage_NewBranch;

    /**
     * EditBranchWizard_EditBranchDescription
     */
    public static String EditBranchWizard_EditBranchDescription;

    /**
     * EditBranchWizard_EditBranchTitle
     */
    public static String EditBranchWizard_EditBranchTitle;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
