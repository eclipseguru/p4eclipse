/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences;

import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IPreferenceConstants {

    /**
     * PREFIX - preference prefix
     */
    String PREFIX = PerforceUIPlugin.ID + ".preferences."; //$NON-NLS-1$

    /**
     * VALUE_DELIMITER - delimiter used for multiple values stored in one
     * preference
     */
    String VALUE_DELIMITER = "###"; //$NON-NLS-1$

    /**
     * PAIR_DELIMITER - delimiter users to seperate name and value for a
     * preference storing multiple name/value pairs
     */
    String PAIR_DELIMITER = "==="; //$NON-NLS-1$

    /**
     * LINK_SUBMITTED
     */
    String LINK_SUBMITTED = "com.perforce.team.ui.preferences.LINK_SUBMITTED"; //$NON-NLS-1$

    /**
     * CONNECTION_DECORATION_TEXT
     */
    String CONNECTION_DECORATION_TEXT = "com.perforce.team.ui.preferences.CONNECTION_DECORATION_TEXT"; //$NON-NLS-1$

    /**
     * PROJECT_DECORATION_TEXT
     */
    String PROJECT_DECORATION_TEXT = "com.perforce.team.ui.preferences.PROJECT_DECORATION_TEXT"; //$NON-NLS-1$

    /**
     * FILE_DECORATION_TEXT
     */
    String FILE_DECORATION_TEXT = "com.perforce.team.ui.preferences.FILE_DECORATION_TEXT"; //$NON-NLS-1$

    /**
     * IGNORED_DECORATION
     */
    String IGNORED_DECORATION = "com.perforce.team.ui.preferences.IGNORED_DECORATION"; //$NON-NLS-1$

    /**
     * OUTGOING_CHANGE_DECORATION
     */
    String OUTGOING_CHANGE_DECORATION = "com.perforce.team.ui.preferences.OUTGOING_CHANGE_DECORATION"; //$NON-NLS-1$

    /**
     * UNADDED_CHANGE_DECORATION
     */
    String UNADDED_CHANGE_DECORATION = "com.perforce.team.ui.preferences.UNADDED_CHANGE_DECORATION"; //$NON-NLS-1$

    /**
     * NAME_VARIABLE
     */
    String NAME_VARIABLE = "{name}"; //$NON-NLS-1$

    /**
     * NAME_DESCRIPTION
     */
    String NAME_DESCRIPTION = Messages.IPreferenceConstants_NameOfDecoratedResource;

    /**
     * CLIENT_VARIABLE
     */
    String CLIENT_VARIABLE = "{client}"; //$NON-NLS-1$

    /**
     * CLIENT_VARIABLE
     */
    String CLIENT_DESCRIPTION = Messages.IPreferenceConstants_PerforceClient;

    /**
     * USER_VARIABLE
     */
    String USER_VARIABLE = "{user}"; //$NON-NLS-1$

    /**
     * USER_VARIABLE
     */
    String USER_DESCRIPTION = Messages.IPreferenceConstants_PerforceUser;

    /**
     * SERVER_VARIABLE
     */
    String SERVER_VARIABLE = "{server}"; //$NON-NLS-1$

    /**
     * SERVER_VARIABLE
     */
    String SERVER_DESCRIPTION = Messages.IPreferenceConstants_PerforceServer;

    /**
     * CHARSET_VARIABLE
     */
    String CHARSET_VARIABLE = "{charset}"; //$NON-NLS-1$

    /**
     * CHARSET_DESCRIPTION
     */
    String CHARSET_DESCRIPTION = Messages.IPreferenceConstants_PerforceCharset;

    /**
     * HAVE_VARIABLE
     */
    String HAVE_VARIABLE = "{have}"; //$NON-NLS-1$

    /**
     * HAVE_VARIABLE
     */
    String HAVE_DESCRIPTION = Messages.IPreferenceConstants_HaveRev;

    /**
     * HEAD_VARIABLE
     */
    String HEAD_VARIABLE = "{head}"; //$NON-NLS-1$

    /**
     * HEAD_VARIABLE
     */
    String HEAD_DESCRIPTION = Messages.IPreferenceConstants_HeadRev;

    /**
     * HEAD_CHANGE_VARIABLE
     */
    String HEAD_CHANGE_VARIABLE = "{head_change}"; //$NON-NLS-1$

    /**
     * HEAD_CHANGE_DESCRIPTION
     */
    String HEAD_CHANGE_DESCRIPTION = Messages.IPreferenceConstants_HeadChange;
    
    /**
     * OFFLINE_VARIABLE
     */
    String OFFLINE_VARIABLE = "{offline}"; //$NON-NLS-1$

    /**
     * OFFLINE_VARIABLE
     */
    String OFFLINE_DESCRIPTION = Messages.IPreferenceConstants_OfflineConnection;

    /**
     * SANDBOX_VARIABLE
     */
    String SANDBOX_VARIABLE = "{sandbox}"; //$NON-NLS-1$

    /**
     * SANDBOX_VARIABLE
     */
    String SANDBOX_DESCRIPTION = Messages.IPreferenceConstants_SandboxConnection;

    /**
     * STREAM_NAME_VARIABLE
     */
    String STREAM_NAME_VARIABLE = "{stream_name}"; //$NON-NLS-1$

    /**
     * STREAM_NAME_VARIABLE
     */
    String STREAM_NAME_DESCRIPTION = Messages.IPreferenceConstants_StreamName;

    /**
     * STREAM_ROOT_VARIABLE
     */
    String STREAM_ROOT_VARIABLE = "{stream_root}"; //$NON-NLS-1$

    /**
     * STREAM_ROOT_VARIABLE
     */
    String STREAM_ROOT_DESCRIPTION = Messages.IPreferenceConstants_StreamRoot;

    /**
     * TYPE_VARIABLE
     */
    String TYPE_VARIABLE = "{type}"; //$NON-NLS-1$

    /**
     * TYPE_VARIABLE
     */
    String TYPE_DESCRIPTION = Messages.IPreferenceConstants_ResourceType;

    /**
     * ACTION_VARIABLE
     */
    String ACTION_VARIABLE = "{action}"; //$NON-NLS-1$

    /**
     * ACTION_DESCRIPTION
     */
    String ACTION_DESCRIPTION = Messages.IPreferenceConstants_CurrentFileAction;

    /**
     * OUTGOING_CHANGE_VARIABLE
     */
    String OUTGOING_CHANGE_VARIABLE = "{outgoing_change_flag}"; //$NON-NLS-1$

    /**
     * UNADDED_CHANGE_VARIABLE
     */
    String UNADDED_CHANGE_VARIABLE = "{not_under_version_control}"; //$NON-NLS-1$

    /**
     * OUTGOING_CHANGE_DESCRIPTION
     */
    String OUTGOING_CHANGE_DESCRIPTION = Messages.IPreferenceConstants_OutgoingChangesFlag;

    /**
     * UNADDED_CHANGE_DESCRIPTION
     */
    String UNADDED_CHANGE_DESCRIPTION = Messages.IPreferenceConstants_NotUnderVersionControlFlag;

    /**
     * P4MERGE_PATH
     */
    String P4MERGE_PATH = "com.perforce.team.ui.preferences.P4MERGE_PATH"; //$NON-NLS-1$

    /**
     * P4V_PATH
     */
    String P4V_PATH = "com.perforce.team.ui.preferences.P4V_PATH"; //$NON-NLS-1$

    /**
     * P4SANDBOXCONFIG_PATH
     */
    String P4SANDBOXCONFIG_PATH = "com.perforce.team.ui.preferences.P4SANDBOXCONFIG_PATH"; //$NON-NLS-1$

    /**
     * SHOW_CHANGELIST_IN_SYNC_VIEW
     */
    String SHOW_CHANGELIST_IN_SYNC_VIEW = "com.perforce.team.ui.preferences.SHOW_CHANGELIST_IN_SYNC_VIEW"; //$NON-NLS-1$

    /**
     * TRACE_LEVEL
     */
    String TRACE_LEVEL = "com.perforce.team.ui.preferences.TRACE_LEVEL"; //$NON-NLS-1$

    /**
     * CUSTOM_P4JAVA_PROPERTIES
     */
    String CUSTOM_P4JAVA_PROPERTIES = "com.perforce.team.ui.preferences.CUSTOM_P4JAVA_PROPERTIES"; //$NON-NLS-1$

    /**
     * CHANGELIST_SHOW_CHECKED_ONLY
     */
    String CHANGELIST_SHOW_CHECKED_ONLY = "com.perforce.team.ui.preferences.CHANGELIST_SHOW_CHECKED_ONLY"; //$NON-NLS-1$

    /**
     * NUM_LABELS_RETRIEVE
     */
    String NUM_LABELS_RETRIEVE = "com.perforce.team.ui.preferences.NUM_LABELS_RETRIEVE"; //$NON-NLS-1$

    /**
     * NUM_BRANCHES_RETRIEVE
     */
    String NUM_BRANCHES_RETRIEVE = PREFIX + "NUM_BRANCHES_RETRIEVE"; //$NON-NLS-1$

    /**
     * NUM_SHELVES_RETRIEVE
     */
    String NUM_SHELVES_RETRIEVE = PREFIX + "NUM_SHELVES_RETRIEVE"; //$NON-NLS-1$

    /**
     * BRANCH_HISTORY
     */
    String BRANCH_HISTORY = PREFIX + "BRANCH_HISTORY"; //$NON-NLS-1$

    /**
     * SOURCE_FILE_HISTORY
     */
    String SOURCE_FILE_HISTORY = PREFIX + "SOURCE_FILE_HISTORY"; //$NON-NLS-1$

    /**
     * TARGET_FILE_HISTORY
     */
    String TARGET_FILE_HISTORY = PREFIX + "TARGET_FILE_HISTORY"; //$NON-NLS-1$

    /**
     * PERSIST_OFFINE
     */
    String PERSIST_OFFINE = PREFIX + "PERSIST_OFFINE"; //$NON-NLS-1$

    /**
     * MIGRATE_P4JAVA_SHOWN
     */
    String MIGRATE_P4JAVA_SHOWN = PREFIX + "MIGRATE_P4JAVA_SHOWN"; //$NON-NLS-1$

    /**
     * CHANGELIST_TEMPLATES
     */
    String CHANGELIST_TEMPLATES = PREFIX + "CHANGELIST_TEMPLATES"; //$NON-NLS-1$

    /**
     * CHANGELISTS_SAVED
     */
    String CHANGELISTS_SAVED = PREFIX + "CHANGELISTS_SAVED"; //$NON-NLS-1$

    /**
     * CHANGELIST_DESCRIPTIONS
     */
    String CHANGELIST_DESCRIPTIONS = PREFIX + "CHANGELIST_DESCRIPTIONS"; //$NON-NLS-1$

    /**
     * REFACTOR_DIALOG
     */
    String REFACTOR_DIALOG = PREFIX + "REFACTOR_DIALOG"; //$NON-NLS-1$

    /**
     * HISTORY_SHOW_MODE
     */
    String HISTORY_SHOW_MODE = PREFIX + "HISTORY_SHOW_MODE"; //$NON-NLS-1$

    /**
     * SAVE_EXPANDED_DEPOTS
     */
    String SAVE_EXPANDED_DEPOTS = PREFIX + "SAVE_EXPANDED_DEPOTS"; //$NON-NLS-1$

    /**
     * DESCRIPTION_RULER_COLOR
     */
    String DESCRIPTION_RULER_COLOR = PREFIX + "DESCRIPTION_RULER_COLOR"; //$NON-NLS-1$

    /**
     * DESCRIPTION_RULER_COLUMN
     */
    String DESCRIPTION_RULER_COLUMN = PREFIX + "DESCRIPTION_RULER_COLUMN"; //$NON-NLS-1$

    /**
     * DESCRIPTION_RULER_STYLE
     */
    String DESCRIPTION_RULER_STYLE = PREFIX + "DESCRIPTION_RULER_STYLE"; //$NON-NLS-1$

    /**
     * DESCRIPTION_RULER
     */
    String DESCRIPTION_RULER = PREFIX + "DESCRIPTION_RULER"; //$NON-NLS-1$

    /**
     * DESCRIPTION_AUTO_ACTIVATE
     */
    String DESCRIPTION_AUTO_ACTIVATE = PREFIX + "DESCRIPTION_AUTO_ACTIVATE"; //$NON-NLS-1$

    /**
     * USE_INTERNAL_TIMELAPSE
     */
    String USE_INTERNAL_TIMELAPSE = PREFIX + "USE_INTERNAL_TIMELAPSE"; //$NON-NLS-1$

    /**
     * DISABLED_TIMELAPSE_CONTENT_TYPES
     */
    String DISABLED_TIMELAPSE_CONTENT_TYPES = PREFIX
            + "DISABLED_TIMELAPSE_CONTENT_TYPES"; //$NON-NLS-1$

    /**
     * DESCRIPTION_EDITOR_FONT
     */
    String DESCRIPTION_EDITOR_FONT = PREFIX + "DESCRIPTION_EDITOR_FONT"; //$NON-NLS-1$

    /**
     * SHOW_CHANGE_SETS
     */
    String SHOW_CHANGE_SETS = PREFIX + "SHOW_CHANGE_SETS"; //$NON-NLS-1$

    /**
     * SAME_JOB_STATUS
     */
    String SAME_JOB_STATUS = PREFIX + "SAME_JOB_STATUS"; //$NON-NLS-1$

    /**
     * DEPOT VIEW FILTER options
     */
    String SHOW_DELETED_FILES = PerforceUIPlugin.ID  +".depot.show_deleted_files"; //$NON-NLS-1$
    String FILTER_CLIENT_FILES = PerforceUIPlugin.ID +".depot.filter_client_files"; //$NON-NLS-1$

}
