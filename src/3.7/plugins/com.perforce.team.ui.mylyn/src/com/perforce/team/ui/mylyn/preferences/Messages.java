/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mylyn.preferences.messages"; //$NON-NLS-1$

    /**
     * ConnectionMappingPreferencePage_AddMapping
     */
    public static String ConnectionMappingPreferencePage_AddMapping;

    /**
     * ConnectionMappingPreferencePage_Connection
     */
    public static String ConnectionMappingPreferencePage_Connection;

    /**
     * ConnectionMappingPreferencePage_LinkConnection
     */
    public static String ConnectionMappingPreferencePage_LinkConnection;

    /**
     * ConnectionMappingPreferencePage_RemoveMapping
     */
    public static String ConnectionMappingPreferencePage_RemoveMapping;

    /**
     * ConnectionMappingPreferencePage_TaskRepository
     */
    public static String ConnectionMappingPreferencePage_TaskRepository;

    /**
     * JobLayoutPreferencePage_Connections
     */
    public static String JobLayoutPreferencePage_Connections;

    /**
     * JobLayoutPreferencePage_DragAndDropFields
     */
    public static String JobLayoutPreferencePage_DragAndDropFields;

    /**
     * JobLayoutPreferencePage_Loading
     */
    public static String JobLayoutPreferencePage_Loading;

    /**
     * MylynPreferencePage_DisplaySingleBeforeMulti
     */
    public static String MylynPreferencePage_DisplaySingleBeforeMulti;

    /**
     * MylynPreferencePage_RecreatePendingChangelist
     */
    public static String MylynPreferencePage_RecreatePendingChangelist;

    /**
     * MylynPreferencePage_SortJobFieldInSearch
     */
    public static String MylynPreferencePage_SortJobFieldInSearch;

    /**
     * MylynPreferencePage_UseTaskEditor
     */
    public static String MylynPreferencePage_UseTaskEditor;
    
    /**
     * MylynPreferencePage_UseMylynTeamComment
     */
    public static String MylynPreferencePage_UseMylynTeamComment;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
