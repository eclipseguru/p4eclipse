/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mylyn.job.messages"; //$NON-NLS-1$

    /**
     * BulkJobEditor_EditJobsOn
     */
    public static String BulkJobEditor_EditJobsOn;

    /**
     * BulkJobEditor_InputMustBeBulkJobInput
     */
    public static String BulkJobEditor_InputMustBeBulkJobInput;

    /**
     * BulkJobInput_BulkJobChanges
     */
    public static String BulkJobInput_BulkJobChanges;

    /**
     * BulkJobInput_PerformBulkChanges
     */
    public static String BulkJobInput_PerformBulkChanges;

    /**
     * BulkJobPage_AddJobs
     */
    public static String BulkJobPage_AddJobs;

    /**
     * BulkJobPage_ErrorUpdatingJobs
     */
    public static String BulkJobPage_ErrorUpdatingJobs;

    /**
     * BulkJobPage_JobFields
     */
    public static String BulkJobPage_JobFields;

    /**
     * BulkJobPage_Jobs
     */
    public static String BulkJobPage_Jobs;

    /**
     * BulkJobPage_JobsWithAmount
     */
    public static String BulkJobPage_JobsWithAmount;

    /**
     * BulkJobPage_RemoveJobs
     */
    public static String BulkJobPage_RemoveJobs;

    /**
     * BulkJobPage_SelectJobs
     */
    public static String BulkJobPage_SelectJobs;

    /**
     * BulkJobPage_SubmitJobChanges
     */
    public static String BulkJobPage_SubmitJobChanges;

    /**
     * BulkJobPage_UpdateErrors
     */
    public static String BulkJobPage_UpdateErrors;

    /**
     * BulkJobPage_UpdatingJobs
     */
    public static String BulkJobPage_UpdatingJobs;

    /**
     * BulkJobUpdater_Job
     */
    public static String BulkJobUpdater_Job;

    /**
     * BulkJobUpdater_Jobs
     */
    public static String BulkJobUpdater_Jobs;

    /**
     * BulkJobUpdater_UpdateFinished
     */
    public static String BulkJobUpdater_UpdateFinished;

    /**
     * BulkJobUpdater_UpdateJobWithLink
     */
    public static String BulkJobUpdater_UpdateJobWithLink;

    /**
     * BulkJobUpdater_ViewErrorsLink
     */
    public static String BulkJobUpdater_ViewErrorsLink;

    /**
     * EditJobTaskAction_CreateTaskRepository
     */
    public static String EditJobTaskAction_CreateTaskRepository;

    /**
     * EditJobTaskAction_NoTaskRepository
     */
    public static String EditJobTaskAction_NoTaskRepository;

    /**
     * JobFieldManager_ChangeFields
     */
    public static String JobFieldManager_ChangeFields;

 	public static String BulkJobChangeAction_Warning;

	public static String BulkJobChangeAction_WarningDetail;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
