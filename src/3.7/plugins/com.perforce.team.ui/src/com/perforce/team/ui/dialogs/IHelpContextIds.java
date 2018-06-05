package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.ui.PerforceUIPlugin;

/**
 * Help context ID's
 */
public interface IHelpContextIds {

    public static final String PREFIX = PerforceUIPlugin.ID + "."; //$NON-NLS-1$

    // General preferences dialog
    public static final String PREF_MARKERS_ENABLED = PREFIX
            + "pref_markers_enabled"; //$NON-NLS-1$
    public static final String PREF_NEW_OPEN_ADD = PREFIX + "pref_new_open_add"; //$NON-NLS-1$
    public static final String PREF_OPEN_DEFAULT = PREFIX + "pref_open_default"; //$NON-NLS-1$
    public static final String PREF_REFACTOR_SUPPORT = PREFIX
            + "pref_refactor_support"; //$NON-NLS-1$
    public static final String PREF_DELETE_PROJECT_FILES = PREFIX
            + "pref_delete_project_files"; //$NON-NLS-1$
    public static final String PREF_USE_SEARCH_PATH = PREFIX
            + "pref_use_search_path"; //$NON-NLS-1$
    public static final String PREF_USE_LOCATION = PREFIX + "pref_use_location"; //$NON-NLS-1$
    public static final String PREF_LOCATION_TEXT = PREFIX
            + "pref_location_text"; //$NON-NLS-1$
    public static final String PREF_LOCATION_BROWSE = PREFIX
            + "pref_location_browse"; //$NON-NLS-1$

    // Console preferences dialog
    public static final String PREF_CONSOLE_COMMAND_COLOUR = PREFIX
            + "pref_console_command_colour"; //$NON-NLS-1$
    public static final String PREF_CONSOLE_MESSAGE_COLOUR = PREFIX
            + "pref_console_message_colour"; //$NON-NLS-1$
    public static final String PREF_CONSOLE_ERROR_COLOUR = PREFIX
            + "pref_console_error_colour"; //$NON-NLS-1$
    public static final String PREF_CONSOLE_FONT = PREFIX + "pref_console_font"; //$NON-NLS-1$

    // Label decorations preferences dialog
    public static final String PREF_FILE_REVISION = PREFIX
            + "pref_file_revision"; //$NON-NLS-1$
    public static final String PREF_FILE_TYPE = PREFIX + "pref_file_type"; //$NON-NLS-1$
    public static final String PREF_FILE_ACTION = PREFIX + "pref_file_action"; //$NON-NLS-1$

    public static final String PREF_PROJECT_PORT = PREFIX + "pref_project_port"; //$NON-NLS-1$
    public static final String PREF_PROJECT_CLIENT = PREFIX
            + "pref_project_client"; //$NON-NLS-1$
    public static final String PREF_PROJECT_USER = PREFIX + "pref_project_user"; //$NON-NLS-1$

    public static final String PREF_IGNORED_RESOURCES = PREFIX
            + "pref_ignored_resources"; //$NON-NLS-1$

    public static final String PREF_OPEN_ICON = PREFIX + "pref_open_icon"; //$NON-NLS-1$
    public static final String PREF_SYNC_ICON = PREFIX + "pref_sync_icon"; //$NON-NLS-1$
    public static final String PREF_NOT_SYNC_ICON = PREFIX
            + "pref_not_sync_icon"; //$NON-NLS-1$
    public static final String PREF_UNRESOLVED_ICON = PREFIX
            + "pref_unresolved_icon"; //$NON-NLS-1$
    public static final String PREF_LOCK_ICON = PREFIX + "pref_lock_icon"; //$NON-NLS-1$
    public static final String PREF_OTHER_ICON = PREFIX + "pref_other_icon"; //$NON-NLS-1$

    // Jobs preferences dialog
    public static final String PREF_ALL_JOBS_RADIO = PREFIX
            + "pref_all_jobs_radio"; //$NON-NLS-1$
    public static final String PREF_MAX_JOBS_RADIO = PREFIX
            + "pref_max_jobs_radio"; //$NON-NLS-1$
    public static final String PREF_MAX_JOBS_TEXT = PREFIX
            + "pref_max_jobs_text"; //$NON-NLS-1$

    // Changelists preferences dialog
    public static final String PREF_ALL_CHANGELISTS_RADIO = PREFIX
            + "pref_all_changelists_radio"; //$NON-NLS-1$
    public static final String PREF_MAX_CHANGELISTS_RADIO = PREFIX
            + "pref_max_changelists_radio"; //$NON-NLS-1$
    public static final String PREF_MAX_CHANGELISTS_TEXT = PREFIX
            + "pref_max_changelists_text"; //$NON-NLS-1$

    // Streams preferences dialog
    public static final String PREF_ALL_STREAMS_RADIO = PREFIX
            + "pref_all_streams_radio"; //$NON-NLS-1$
    public static final String PREF_MAX_STREAMS_RADIO = PREFIX
            + "pref_max_streams_radio"; //$NON-NLS-1$
    public static final String PREF_MAX_STREAMS_TEXT = PREFIX
            + "pref_max_streams_text"; //$NON-NLS-1$

    // Client preferences dialog
    public static final String PREF_CLIENT_SWITCH_RADIO = PREFIX
            + "pref_client_switch_radio"; //$NON-NLS-1$

    // Console view
    public static final String CONSOLE_VIEW = PREFIX + "console_view"; //$NON-NLS-1$
    public static final String CONSOLE_CLEAR = PREFIX + "console_clear"; //$NON-NLS-1$

    // History view
    public static final String HISTORY_VIEW = PREFIX + "history_view"; //$NON-NLS-1$
    public static final String HISTORY_REFRESH = PREFIX + "history_refresh"; //$NON-NLS-1$

    // Jobs view
    public static final String JOBS_VIEW = PREFIX + "jobs_view"; //$NON-NLS-1$
    public static final String JOBS_REFRESH = PREFIX + "jobs_refresh"; //$NON-NLS-1$
    public static final String JOBS_COLUMNS = PREFIX + "jobs_columns"; //$NON-NLS-1$
    public static final String JOBS_SET_FILTER = PREFIX + "jobs_set_filter"; //$NON-NLS-1$
    public static final String JOBS_CLEAR_FILTER = PREFIX + "jobs_clear_filter"; //$NON-NLS-1$

    // Pending view
    public static final String PENDING_VIEW = PREFIX + "pending_view"; //$NON-NLS-1$
    public static final String PENDING_REFRESH = PREFIX + "pending_refresh"; //$NON-NLS-1$
    public static final String PENDING_NEW_CHANGELIST = PREFIX
            + "pending_new_changelist"; //$NON-NLS-1$

    // Submitted view
    public static final String SUBMITTED_VIEW = PREFIX + "submitted_view"; //$NON-NLS-1$
    public static final String SUBMITTED_REFRESH = PREFIX + "submitted_refresh"; //$NON-NLS-1$
    public static final String SUBMITTED_DELETE = PREFIX + "submitted_delete"; //$NON-NLS-1$

    // Depot view
    public static final String DEPOT_VIEW = PREFIX + "depot_view"; //$NON-NLS-1$
    public static final String DEPOT_REFRESH = PREFIX + "depot_refresh"; //$NON-NLS-1$

    // Stream view
    public static final String STREAMS_VIEW = PREFIX + "streams_view"; //$NON-NLS-1$
    public static final String STREAMS_REFRESH = PREFIX + "streams_refresh"; //$NON-NLS-1$
    public static final String STRAMS_NEW = PREFIX + "streams_new"; //$NON-NLS-1$

    // Share project dialog
    public static final String SHARE_PROJECT_RECENT = PREFIX
            + "share_project_recent"; //$NON-NLS-1$
    public static final String SHARE_PROJECT_PORT = PREFIX
            + "share_project_port"; //$NON-NLS-1$
    public static final String SHARE_PROJECT_USER = PREFIX
            + "share_project_user"; //$NON-NLS-1$
    public static final String SHARE_PROJECT_CLIENT = PREFIX
            + "share_project_client"; //$NON-NLS-1$
    public static final String SHARE_PROJECT_PASSWORD = PREFIX
            + "share_project_password"; //$NON-NLS-1$
    public static final String SHARE_PROJECT_CHARSET = PREFIX
            + "share_project_charset"; //$NON-NLS-1$
    public static final String SHARE_PROJECT_TEST_CONNECTION = PREFIX
            + "share_project_test_connection"; //$NON-NLS-1$

    // Add/Edit/Delete dialog
    public static final String ADD_EDIT_DELETE_CHANGES = PREFIX
            + "add_edit_delete_changes"; //$NON-NLS-1$
    public static final String ADD_EDIT_DELETE_FILES = PREFIX
            + "add_edit_delete_files"; //$NON-NLS-1$

    // Check Consistency dialog
    public static final String CHECK_CONSISTENCY_DIFF_FILES = PREFIX
            + "check_consistency_diff_files"; //$NON-NLS-1$
    public static final String CHECK_CONSISTENCY_MISSING_FILES = PREFIX
            + "check_consistency_missing_files"; //$NON-NLS-1$
    public static final String CHECK_CONSISTENCY_NEW_FILES = PREFIX
            + "check_consistency_new_files"; //$NON-NLS-1$
    public static final String CHECK_CONSISTENCY_CHANGES = PREFIX
            + "check_consistency_changes"; //$NON-NLS-1$

    // Auto resolve dialog
    public static final String AUTO_RESOLVE_SELECTED_FILES = PREFIX
            + "auto_resolve_selected_files"; //$NON-NLS-1$
    public static final String AUTO_RESOLVE_ALL_FILES = PREFIX
            + "auto_resolve_all_files"; //$NON-NLS-1$

    public static final String AUTO_RESOLVE_ACCEPT_THEIRS = PREFIX
            + "auto_resolve_accept_theirs"; //$NON-NLS-1$
    public static final String AUTO_RESOLVE_ACCEPT_YOURS = PREFIX
            + "auto_resolve_accept_yours"; //$NON-NLS-1$
    public static final String AUTO_RESOLVE_SAFE_AUTO = PREFIX
            + "auto_resolve_safe_auto"; //$NON-NLS-1$
    public static final String AUTO_RESOLVE_AUTO = PREFIX + "auto_resolve_auto"; //$NON-NLS-1$
    public static final String AUTO_RESOLVE_CONFLICT = PREFIX
            + "auto_resolve_conflict"; //$NON-NLS-1$

    public static final String AUTO_RESOLVE_RERESOLVE = PREFIX
            + "auto_resolve_reresolve"; //$NON-NLS-1$
    public static final String AUTO_RESOLVE_TEXT_MERGE = PREFIX
            + "auto_resolve_text_merge"; //$NON-NLS-1$

    // Change Spec dialog
    public static final String CHANGE_SPEC_DESCRIPTION = PREFIX
            + "change_spec_description"; //$NON-NLS-1$
    public static final String CHANGE_SPEC_FILES = PREFIX + "change_spec_files"; //$NON-NLS-1$
    public static final String CHANGE_SPEC_JOBS = PREFIX + "change_spec_jobs"; //$NON-NLS-1$
    public static final String CHANGE_SPEC_ADD_JOBS = PREFIX
            + "change_spec_add_jobs"; //$NON-NLS-1$
    public static final String CHANGE_SPEC_REOPEN = PREFIX
            + "change_spec_reopen"; //$NON-NLS-1$
    public static final String CHANGE_SPEC_SELECT_ALL = PREFIX
            + "change_spec_select_all"; //$NON-NLS-1$
    public static final String CHANGE_SPEC_DESELECT_ALL = PREFIX
            + "change_spec_deselect_all"; //$NON-NLS-1$

    // Job View dialog
    public static final String JOB_VIEW_FILTER = PREFIX + "job_view_filter"; //$NON-NLS-1$
    public static final String JOB_VIEW_DELETE = PREFIX + "job_view_delete"; //$NON-NLS-1$
    public static final String JOB_VIEW_JOB_LIST = PREFIX + "job_view_job_list"; //$NON-NLS-1$
    public static final String JOB_VIEW_JOB_DESC = PREFIX + "job_view_job_desc"; //$NON-NLS-1$

    // File Type dialog
    public static final String FILE_TYPE_TEXT = PREFIX + "file_type_text"; //$NON-NLS-1$
    public static final String FILE_TYPE_BINARY = PREFIX + "file_type_binary"; //$NON-NLS-1$
    public static final String FILE_TYPE_SYMLINK = PREFIX + "file_type_symlink"; //$NON-NLS-1$
    public static final String FILE_TYPE_UNICODE = PREFIX + "file_type_unicode"; //$NON-NLS-1$
    public static final String FILE_TYPE_RESOURCE = PREFIX
            + "file_type_resource"; //$NON-NLS-1$
    public static final String FILE_TYPE_APPLE = PREFIX + "file_type_apple"; //$NON-NLS-1$

    public static final String FILE_TYPE_EXEC = PREFIX + "file_type_exec"; //$NON-NLS-1$
    public static final String FILE_TYPE_WRITEABLE = PREFIX
            + "file_type_writeable"; //$NON-NLS-1$
    public static final String FILE_TYPE_KEYWORD = PREFIX + "file_type_keyword"; //$NON-NLS-1$
    public static final String FILE_TYPE_ONLY = PREFIX + "file_type_only"; //$NON-NLS-1$
    public static final String FILE_TYPE_PRESERVE = PREFIX
            + "file_type_preserve"; //$NON-NLS-1$
    public static final String FILE_TYPE_MULTIPLE = PREFIX
            + "file_type_multiple"; //$NON-NLS-1$

    public static final String FILE_TYPE_DEFAULT = PREFIX + "file_type_default"; //$NON-NLS-1$
    public static final String FILE_TYPE_COMPRESSED = PREFIX
            + "file_type_compressed"; //$NON-NLS-1$
    public static final String FILE_TYPE_DELTA = PREFIX + "file_type_delta"; //$NON-NLS-1$
    public static final String FILE_TYPE_FULL = PREFIX + "file_type_full"; //$NON-NLS-1$
    public static final String FILE_TYPE_SINGLE = PREFIX + "file_type_single"; //$NON-NLS-1$

    // Revert Unchanged dialog
    public static final String REVERT_UNCHANGED_FILES = PREFIX
            + "revert_unchanged_files"; //$NON-NLS-1$

    // Test connection dialog
    public static final String TEST_CONNECTION_RESULTS = PREFIX
            + "test_connection_results"; //$NON-NLS-1$

    // new connection dialog
    public static final String P4_NEW_CONNECTION = PREFIX
            + "p4_new_connection"; //$NON-NLS-1$
            
    // Import project dialog    
    public static final String P4_IMPORT_PROJECT = PREFIX
            + "p4_import_project"; //$NON-NLS-1$
    

    // Move change dialog
    public static final String MOVE_CHANGE_CHANGELISTS = PREFIX
            + "move_change_changelists"; //$NON-NLS-1$

    // Authorization dialog
    public static final String AUTH_REMEMBER_PASSWORD = PREFIX
            + "auth_remember_password"; //$NON-NLS-1$
    public static final String AUTH_USE_LOGIN = PREFIX + "auth_use_login"; //$NON-NLS-1$
}
