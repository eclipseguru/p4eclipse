/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mergequest.views.tasks.messages"; //$NON-NLS-1$


    /**
     * IntegrateTaskLabelProvider_ChangelistClient
     */
    public static String IntegrateTaskLabelProvider_ChangelistClient;

    /**
     * IntegrateTaskLabelProvider_Count
     */
    public static String IntegrateTaskLabelProvider_Count;


    /**
     * IntegrateTaskPage_IntegrateChangelist
     */
    public static String IntegrateTaskPage_IntegrateChangelist;

    /**
     * IntegrateTaskPage_UpdatingTasks
     */
    public static String IntegrateTaskPage_UpdatingTasks;

    /**
     * IntegrateTaskView_DefaultMessage
     */
    public static String IntegrateTaskView_DefaultMessage;

    /**
     * IntegrateTaskViewer_DateMode
     */
    public static String IntegrateTaskViewer_DateMode;

    /**
     * IntegrateTaskViewer_Description
     */
    public static String IntegrateTaskViewer_Description;

    /**
     * IntegrateTaskViewer_FlatMode
     */
    public static String IntegrateTaskViewer_FlatMode;

    /**
     * IntegrateTaskViewer_ListMode
     */
    public static String IntegrateTaskViewer_ListMode;

    /**
     * IntegrateTaskViewer_NoMappingSelected
     */
    public static String IntegrateTaskViewer_NoMappingSelected;

    /**
     * IntegrateTaskViewer_RefreshTasks
     */
    public static String IntegrateTaskViewer_RefreshTasks;

    /**
     * IntegrateTaskViewer_RefreshTasksTooltip
     */
    public static String IntegrateTaskViewer_RefreshTasksTooltip;

    /**
     * IntegrateTaskViewer_TaskMode
     */
    public static String IntegrateTaskViewer_TaskMode;

    /**
     * IntegrateTaskViewer_UserMode
     */
    public static String IntegrateTaskViewer_UserMode;

    /**
     * JobTaskGroup_NoJobsAttached
     */
    public static String JobTaskGroup_NoJobsAttached;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
