/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mylyn.messages"; //$NON-NLS-1$

    /**
     * P4DefaultJobUiConfiguration_CommentReplyText
     */
    public static String P4DefaultJobUiConfiguration_CommentReplyText;

    /**
     * P4DefaultJobUiConfiguration_WorkingChangelistDescription
     */
    public static String P4DefaultJobUiConfiguration_WorkingChangelistDescription;

    /**
     * P4DefaultPendingChangelistLocator_UpdatingChangelistDescription
     */
    public static String P4DefaultPendingChangelistLocator_UpdatingChangelistDescription;

    /**
     * P4JobSettingsPage_AddConnection
     */
    public static String P4JobSettingsPage_AddConnection;

    /**
     * P4JobSettingsPage_ConfigureJobsRepository
     */
    public static String P4JobSettingsPage_ConfigureJobsRepository;

    /**
     * P4JobSettingsPage_Label
     */
    public static String P4JobSettingsPage_Label;

    /**
     * P4JobSettingsPage_PerforceJobs
     */
    public static String P4JobSettingsPage_PerforceJobs;

    /**
     * P4JobSettingsPage_RepositoryExists
     */
    public static String P4JobSettingsPage_RepositoryExists;

    /**
     * P4JobSettingsPage_RepositoryLabelExists
     */
    public static String P4JobSettingsPage_RepositoryLabelExists;

    /**
     * P4JobSettingsPage_SelectServer
     */
    public static String P4JobSettingsPage_SelectServer;

    /**
     * P4JobSettingsPage_ServerConnections
     */
    public static String P4JobSettingsPage_ServerConnections;

	public static String P4DefaultJobUiConfiguration_Error;

	public static String P4DefaultJobUiConfiguration_TaskDoesnotExist;

	public static String P4DefaultJobUiConfiguration_OpenJob;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
