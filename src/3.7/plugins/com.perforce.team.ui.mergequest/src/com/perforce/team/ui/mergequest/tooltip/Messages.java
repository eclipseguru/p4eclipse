/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.tooltip;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.tooltip.messages"; //$NON-NLS-1$

    public static String BranchToolTip_IncomingIntegrations;
    public static String BranchToolTip_MultiConnectedMappings;
    public static String BranchToolTip_None;
    public static String BranchToolTip_OutgoingIntegrations;
    public static String BranchToolTip_SingleConnectedMapping;
    public static String MappingToolTip_Source;
    public static String MappingToolTip_SourcePath;
    public static String MappingToolTip_Target;
    public static String MappingToolTip_TargetPath;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
