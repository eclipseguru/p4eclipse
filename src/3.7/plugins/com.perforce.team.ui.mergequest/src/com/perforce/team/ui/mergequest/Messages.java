/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.messages"; //$NON-NLS-1$

    /**
     * BranchGraphInput_ToolTip
     */
    public static String BranchGraphInput_ToolTip;

    /**
     * IntegrateTaskViewer_ChangelistCount
     */
    public static String IntegrateTaskViewer_ChangelistCount;

    /**
     * IntegrateTaskViewer_OneChangelist
     */
    public static String IntegrateTaskViewer_OneChangelist;

    /**
     * ReorderGraphDialog_DialogTitle
     */
    public static String ReorderGraphDialog_DialogTitle;

    /**
     * ReorderGraphDialog_MoveDown
     */
    public static String ReorderGraphDialog_MoveDown;

    /**
     * ReorderGraphDialog_MoveUp
     */
    public static String ReorderGraphDialog_MoveUp;

    /**
     * ReorderGraphDialog_MultipleBranches
     */
    public static String ReorderGraphDialog_MultipleBranches;

    /**
     * ReorderGraphDialog_MultipleMappings
     */
    public static String ReorderGraphDialog_MultipleMappings;

    /**
     * ReorderGraphDialog_SingleBranch
     */
    public static String ReorderGraphDialog_SingleBranch;

    /**
     * ReorderGraphDialog_SingleMapping
     */
    public static String ReorderGraphDialog_SingleMapping;

    /**
     * TaskContainer_Changelist
     */
    public static String TaskContainer_Changelist;

    /**
     * TaskContainer_CollapseAll
     */
    public static String TaskContainer_CollapseAll;

    /**
     * TaskContainer_EarlierThan
     */
    public static String TaskContainer_EarlierThan;

    /**
     * TaskContainer_IntegrateMapping
     */
    public static String TaskContainer_IntegrateMapping;

    /**
     * TaskContainer_LoadingFixes
     */
    public static String TaskContainer_LoadingFixes;

    /**
     * TaskContainer_ThisMonth
     */
    public static String TaskContainer_ThisMonth;

    /**
     * TaskContainer_ThisYear
     */
    public static String TaskContainer_ThisYear;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
