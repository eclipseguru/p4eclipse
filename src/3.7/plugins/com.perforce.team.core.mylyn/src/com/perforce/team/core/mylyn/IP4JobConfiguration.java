/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.p4java.IP4Connection;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 * Interface for configuration of the job task model for a specific job spec.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4JobConfiguration extends IP4Configuration {

    /**
     * Should the specified job spec field be treated as the
     * {@link TaskAttribute#PRIORITY} task attribute?
     * 
     * @param field
     * @return - true if field is the priority field, false otherwise
     */
    boolean isPriorityField(IJobSpecField field);

    /**
     * Should the specified job spec field be treated as the
     * {@link TaskAttribute#TASK_KIND} task attribute?
     * 
     * @param field
     * @return - true if field is the kind field, false otherwise
     */
    boolean isKindField(IJobSpecField field);

    /**
     * Should the specified job spec field be treated as the
     * {@link TaskAttribute#DATE_MODIFICATION} task attribute?
     * 
     * @param field
     * @param spec
     * @return - true if field is the modified date field, false otherwise
     */
    boolean isModifiedDateField(IJobSpecField field, IJobSpec spec);

    /**
     * Should the specified job spec field be treated as the
     * {@link TaskAttribute#DATE_CREATION} task attribute?
     * 
     * @param field
     * @param spec
     * @return - true if field is the created date field, false otherwise
     */
    boolean isCreatedDateField(IJobSpecField field, IJobSpec spec);

    /**
     * Should the specified job spec field be parsed as a set of comments
     * through a call to
     * {@link #buildCommentField(TaskAttribute, String, TaskRepository)}?
     * 
     * @param field
     * @return - true if field is the comment field, false otherwise
     */
    boolean isCommentField(IJobSpecField field);

    /**
     * Build the specified comment field value into a set of task attributes
     * that use the {@link TaskAttribute#PREFIX_COMMENT} value. This method
     * should also add a {@link TaskAttribute#COMMENT_NEW} field is new comments
     * are supported.
     * 
     * This method will be called for the field specified to
     * {@link #isCommentField(IJobSpecField)} when that method returns true.
     * 
     * @param root
     * @param value
     * @param repository
     */
    void buildCommentField(TaskAttribute root, String value,
            TaskRepository repository);

    /**
     * Build the entire comment block to put back into the comment job field by
     * combining the current value with the new value.
     * 
     * The new value will be the value of the {@link TaskAttribute#COMMENT_NEW}
     * attribute.
     * 
     * The current comment value will be the value in the job comment field.
     * 
     * @param connection
     * @param newCommentValue
     * @param currentCommentValue
     * @return - full comment field to be put back into a job that is being
     *         updated
     */
    String generateCommentFieldValue(IP4Connection connection,
            String newCommentValue, String currentCommentValue);

    /**
     * Parse an array of task ids from the specified comment for the specified
     * task repository.
     * 
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskIdsFromComment(TaskRepository,
     *      String)
     * @param repository
     * @param comment
     * @return - array of string task ids
     */
    String[] getTaskIdsFromComment(TaskRepository repository, String comment);

    /**
     * Set the completion date. This method will be called after all other task
     * attributes have been created. This method should perform inspection of
     * other fields to create and set the value of the
     * {@link TaskAttribute#DATE_COMPLETION} attribute.
     * 
     * @param root
     */
    void setCompletionDate(TaskAttribute root);

    /**
     * Update task from task data.
     * 
     * @param taskRepository
     * @param task
     * @param taskData
     * 
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#updateTaskFromTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITask,
     *      org.eclipse.mylyn.tasks.core.data.TaskData)
     */
    void updateTaskFromTaskData(TaskRepository taskRepository, ITask task,
            TaskData taskData);

    /**
     * Get task attribute mapper for the specified task repository
     * 
     * @param taskRepository
     * @return - task attribute mapper
     */
    TaskAttributeMapper getAttributeMapper(TaskRepository taskRepository);

}
