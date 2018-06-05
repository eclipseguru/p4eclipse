/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.commands.messages"; //$NON-NLS-1$

    /**
     * BendpointCreateCommand_DefaultLabel
     */
    public static String BendpointCreateCommand_DefaultLabel;

    /**
     * BendpointDeleteCommand_DefaultLabel
     */
    public static String BendpointDeleteCommand_DefaultLabel;

    /**
     * BendpointMoveCommand_DefaultLabel
     */
    public static String BendpointMoveCommand_DefaultLabel;

    /**
     * BranchConstraintCommand_DefaultLabel
     */
    public static String BranchConstraintCommand_DefaultLabel;

    /**
     * BranchCreateCommand_BranchNameLabel
     */
    public static String BranchCreateCommand_BranchNameLabel;

    /**
     * BranchCreateCommand_DefaultLabel
     */
    public static String BranchCreateCommand_DefaultLabel;

    /**
     * BranchDeleteCommand_DefaultLabel
     */
    public static String BranchDeleteCommand_DefaultLabel;

    /**
     * BranchEditCommand_DefaultLabel
     */
    public static String BranchEditCommand_DefaultLabel;

    /**
     * MappingCreateCommand_DefaultLabel
     */
    public static String MappingCreateCommand_DefaultLabel;

    /**
     * MappingDeleteCommand_DefaultLabel
     */
    public static String MappingDeleteCommand_DefaultLabel;

    /**
     * MappingReconnectCommand_DefaultLabel
     */
    public static String MappingReconnectCommand_DefaultLabel;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
