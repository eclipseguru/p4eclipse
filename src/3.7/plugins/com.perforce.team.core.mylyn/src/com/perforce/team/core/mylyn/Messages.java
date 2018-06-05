/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.core.mylyn.messages"; //$NON-NLS-1$

    /**
     * P4JobConnector_FetchingJobs
     */
    public static String P4JobConnector_FetchingJobs;

    /**
     * P4JobConnector_GeneratingTaskData
     */
    public static String P4JobConnector_GeneratingTaskData;

    /**
     * P4JobConnector_Job
     */
    public static String P4JobConnector_Job;

    /**
     * P4JobConnector_JobRetrievalFailed
     */
    public static String P4JobConnector_JobRetrievalFailed;

    /**
     * P4JobConnector_LoadingJob
     */
    public static String P4JobConnector_LoadingJob;

    /**
     * P4JobConnector_PerforceJobs
     */
    public static String P4JobConnector_PerforceJobs;

    /**
     * P4JobConnector_UpdatingJobspec
     */
    public static String P4JobConnector_UpdatingJobspec;

    /**
     * P4JobDataHandler_JobIdFieldNotFound
     */
    public static String P4JobDataHandler_JobIdFieldNotFound;

    /**
     * P4JobDataHandler_JobspecRetrievalFailed
     */
    public static String P4JobDataHandler_JobspecRetrievalFailed;

    /**
     * P4JobDataHandler_NewJob
     */
    public static String P4JobDataHandler_NewJob;

    /**
     * P4JobDataHandler_SummaryLabel
     */
    public static String P4JobDataHandler_SummaryLabel;

    /**
     * P4MylynUtils_ConnectionOffline
     */
    public static String P4MylynUtils_ConnectionOffline;

    /**
     * P4MylynUtils_ConnectionRetrievalFailed
     */
    public static String P4MylynUtils_ConnectionRetrievalFailed;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
