/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.IJob;
import com.perforce.p4java.exception.P4JavaException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Job extends IP4Resource {

    /**
     * BLANK_PRESET - $blank
     */
    String BLANK_PRESET = "$blank"; //$NON-NLS-1$

    /**
     * BLANK_VALUE
     */
    String BLANK_VALUE = Messages.IP4Job_0;

    /**
     * NOW_PRESET - $now
     */
    String NOW_PRESET = "$now"; //$NON-NLS-1$

    /**
     * USER_PRESET - $user
     */
    String USER_PRESET = "$user"; //$NON-NLS-1$

    /**
     * ALWAYS_FIELD_TYPE - always
     */
    String ALWAYS_FIELD_TYPE = "always"; //$NON-NLS-1$

    /**
     * ONCE_FIELD_TYPE - once
     */
    String ONCE_FIELD_TYPE = "once"; //$NON-NLS-1$

    /**
     * OPTIONAL_FIELD_TYPE - optional
     */
    String OPTIONAL_FIELD_TYPE = "optional"; //$NON-NLS-1$

    /**
     * TEXT_DATA_TYPE - text
     */
    String TEXT_DATA_TYPE = "text"; //$NON-NLS-1$

    /**
     * WORD_DATA_TYPE - word
     */
    String WORD_DATA_TYPE = "word"; //$NON-NLS-1$

    /**
     * SELECT_DATA_TYPE - select
     */
    String SELECT_DATA_TYPE = "select"; //$NON-NLS-1$

    /**
     * DATE_DATA_TYPE - date
     */
    String DATE_DATA_TYPE = "date"; //$NON-NLS-1$

    /**
     * LINE_DATA_TYPE - line
     */
    String LINE_DATA_TYPE = "line"; //$NON-NLS-1$

    /**
     * SAME_STATUS_TYPE -
     */
    String SAME_STATUS_TYPE = "same"; //$NON-NLS-1$

    /**
     * CLOSED_STATUS_TYPE
     */
    String CLOSED_STATUS_TYPE = "closed"; //$NON-NLS-1$

    /**
     * FIX_STATUS_PREFIX
     */
    String FIX_STATUS_PREFIX = "fix/"; //$NON-NLS-1$

    /**
     * JOB_NAME_CODE - 101
     */
    int JOB_NAME_CODE = 101;

    /**
     * JOB_STATUS_CODE - 102
     */
    int JOB_STATUS_CODE = 102;

    /**
     * JOB_USER_CODE - 103
     */
    int JOB_USER_CODE = 103;

    /**
     * JOB_DATE_CODE - 104
     */
    int JOB_DATE_CODE = 104;

    /**
     * JOB_DESCRIPTION_CODE - 105
     */
    int JOB_DESCRIPTION_CODE = 105;

    /**
     * Gets the id
     * 
     * @return - job id
     */
    String getId();

    /**
     * Gets the description
     * 
     * @return - job description
     */
    String getDescription();

    /**
     * Gets the short description of this job
     * 
     * @return - short description
     */
    String getShortDescription();

    /**
     * Gets a job field
     * 
     * @param name
     * @return - job field
     */
    Object getField(String name);

    /**
     * Gets the field name present in this job
     * 
     * @return - field names
     */
    String[] getFieldNames();

    /**
     * Gets the field values present in this job
     * 
     * @return - field values
     */
    Object[] getFieldValues();

    /**
     * Gets the underlying p4j job
     * 
     * @return - p4j job
     */
    IJob getJob();

    /**
     * Update the job on the server
     * 
     * @param job
     * @throws P4JavaException
     *             - if update fails
     */
    void update(IJob job) throws P4JavaException;

}
