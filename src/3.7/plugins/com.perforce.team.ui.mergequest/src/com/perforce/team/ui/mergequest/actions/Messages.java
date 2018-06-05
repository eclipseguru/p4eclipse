/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.actions.messages"; //$NON-NLS-1$

    /**
     * BranchIntegrateAction_Label
     */
    public static String BranchIntegrateAction_Label;

    /**
     * MappingDisplayAction_DisplayingIntegrateTasks
     */
    public static String MappingDisplayAction_DisplayingIntegrateTasks;

    /**
     * MappingIntegrateAction_IntegratingMapping
     */
    public static String MappingIntegrateAction_IntegratingMapping;

    /**
     * OpenBranchGraphAction_NotSupportedDescription
     */
    public static String OpenBranchGraphAction_NotSupportedDescription;

    /**
     * OpenBranchGraphAction_NotSupportedTitle
     */
    public static String OpenBranchGraphAction_NotSupportedTitle;

    /**
     * OpenBranchGraphAction_OpenJobTitle
     */
    public static String OpenBranchGraphAction_OpenJobTitle;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
