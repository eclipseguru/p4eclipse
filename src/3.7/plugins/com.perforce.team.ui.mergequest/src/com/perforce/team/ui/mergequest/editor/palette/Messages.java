/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.palette;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.editor.palette.messages"; //$NON-NLS-1$

    /**
     * BranchGraphPaletteRoot_BranchesDrawer
     */
    public static String BranchGraphPaletteRoot_BranchesDrawer;

    /**
     * BranchGraphPaletteRoot_BranchMappingsDrawer
     */
    public static String BranchGraphPaletteRoot_BranchMappingsDrawer;

    /**
     * BranchGraphPaletteRoot_BranchSpecTool
     */
    public static String BranchGraphPaletteRoot_BranchSpecTool;

    /**
     * BranchGraphPaletteRoot_BranchSpecToolDescription
     */
    public static String BranchGraphPaletteRoot_BranchSpecToolDescription;

    /**
     * BranchGraphPaletteRoot_DepotPathTool
     */
    public static String BranchGraphPaletteRoot_DepotPathTool;

    /**
     * BranchGraphPaletteRoot_DepotPathToolDescription
     */
    public static String BranchGraphPaletteRoot_DepotPathToolDescription;

    /**
     * BranchGraphPaletteRoot_MarqueTool
     */
    public static String BranchGraphPaletteRoot_MarqueTool;

    /**
     * BranchGraphPaletteRoot_MarqueToolDescription
     */
    public static String BranchGraphPaletteRoot_MarqueToolDescription;

    /**
     * BranchGraphPaletteRoot_SelectTool
     */
    public static String BranchGraphPaletteRoot_SelectTool;

    /**
     * BranchGraphPaletteRoot_SelectToolDescription
     */
    public static String BranchGraphPaletteRoot_SelectToolDescription;

    /**
     * BranchGraphPaletteRoot_ToolsDrawer
     */
    public static String BranchGraphPaletteRoot_ToolsDrawer;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
