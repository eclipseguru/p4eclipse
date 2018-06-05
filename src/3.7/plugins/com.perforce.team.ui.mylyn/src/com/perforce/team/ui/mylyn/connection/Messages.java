/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.connection;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mylyn.connection.messages"; //$NON-NLS-1$

    /**
     * ConnectionMappingDialog_Connections
     */
    public static String ConnectionMappingDialog_Connections;

    /**
     * ConnectionMappingDialog_Link
     */
    public static String ConnectionMappingDialog_Link;

    /**
     * ConnectionMappingDialog_NewConnectionMappingTitle
     */
    public static String ConnectionMappingDialog_NewConnectionMappingTitle;

    /**
     * ConnectionMappingDialog_SelectConnection
     */
    public static String ConnectionMappingDialog_SelectConnection;

    /**
     * ConnectionMappingDialog_SelectTaskRepository
     */
    public static String ConnectionMappingDialog_SelectTaskRepository;

    /**
     * ConnectionMappingDialog_TaskRepositories
     */
    public static String ConnectionMappingDialog_TaskRepositories;

    /**
     * ConnectionSettingsListener_DeleteMessage
     */
    public static String ConnectionSettingsListener_DeleteMessage;

    /**
     * ConnectionSettingsListener_DeleteTitle
     */
    public static String ConnectionSettingsListener_DeleteTitle;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
