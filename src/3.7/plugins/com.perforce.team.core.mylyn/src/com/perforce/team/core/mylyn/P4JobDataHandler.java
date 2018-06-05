/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Job;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JobDataHandler extends AbstractTaskDataHandler {

    /**
     * PRIVATE_COMMENT_FIELD
     */
    public static final String INTERNAL_COMMENT_FIELD = "com.perforce.team.core.mylyn.internalComment"; //$NON-NLS-1$

    /**
     * Max job ids to specify in single 'jobs -e' query
     */
    public static final int MAX_QUERY_COUNT = 1000;

    /**
     * OR_SEPARATOR
     */
    public static final String OR_SEPARATOR = "|"; //$NON-NLS-1$

    private P4JobConnector connector = null;
    private P4JobConfigurationManager provider = null;

    /**
     * 
     * @param provider
     * @param connector
     */
    public P4JobDataHandler(P4JobConfigurationManager provider,
            P4JobConnector connector) {
        this.provider = provider;
        this.connector = connector;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler#getAttributeMapper(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public TaskAttributeMapper getAttributeMapper(TaskRepository taskRepository) {
        return getConfiguration(taskRepository).getAttributeMapper(
                taskRepository);
    }

    private String getFieldId(IJobSpecField field,
            IP4JobConfiguration configuration, IJobSpec spec) {
        String id = null;
        switch (field.getCode()) {
        case IP4Job.JOB_DESCRIPTION_CODE:
            id = TaskAttribute.DESCRIPTION;
            break;
        case IP4Job.JOB_STATUS_CODE:
            id = TaskAttribute.STATUS;
            break;
        case IP4Job.JOB_USER_CODE:
            id = TaskAttribute.USER_REPORTER;
            break;
        default:
            if (IP4Job.DATE_DATA_TYPE.equals(field.getDataType())) {
                if (configuration.isCreatedDateField(field, spec)) {
                    id = TaskAttribute.DATE_CREATION;
                } else if (configuration.isModifiedDateField(field, spec)) {
                    id = TaskAttribute.DATE_MODIFICATION;
                }
            } else if (configuration.isPriorityField(field)) {
                id = TaskAttribute.PRIORITY;
            } else if (configuration.isKindField(field)) {
                id = TaskAttribute.TASK_KIND;
            }
            if (id == null) {
                id = Integer.toString(field.getCode());
            }
        }
        return id;
    }

    private String getFieldType(int code, String dataType, String name) {
        String type = TaskAttribute.TYPE_SHORT_TEXT;
        if (IP4Job.JOB_USER_CODE == code) {
            type = TaskAttribute.TYPE_PERSON;
        } else if (IP4Job.LINE_DATA_TYPE.equals(dataType)) {
            type = TaskAttribute.TYPE_SHORT_TEXT;
        } else if (IP4Job.SELECT_DATA_TYPE.equals(dataType)) {
            type = TaskAttribute.TYPE_SINGLE_SELECT;
        } else if (IP4Job.TEXT_DATA_TYPE.equals(dataType)) {
            type = TaskAttribute.TYPE_LONG_RICH_TEXT;
        } else if (IP4Job.DATE_DATA_TYPE.equals(dataType)) {
            type = TaskAttribute.TYPE_DATETIME;
        } else if (IP4Job.WORD_DATA_TYPE.equals(dataType)) {
            type = TaskAttribute.TYPE_SHORT_TEXT;
        }
        return type;
    }

    private boolean isReadOnly(boolean isNew, int code, String fieldType) {
        if (!isNew && code == IP4Job.JOB_NAME_CODE) {
            return true;
        } else {
            return IP4Job.ALWAYS_FIELD_TYPE.equals(fieldType)
                    || IP4Job.ONCE_FIELD_TYPE.equals(fieldType);
        }
    }

    private TaskAttribute createAttribute(TaskAttribute parent, String id,
            String label, String type, boolean readOnly) {
        TaskAttribute attr = new TaskAttribute(parent, id);
        TaskAttributeMetaData metadata = attr.getMetaData();
        metadata.setKind(TaskAttribute.KIND_DEFAULT);
        metadata.setLabel(label);
        metadata.setType(type);
        metadata.setReadOnly(readOnly);
        return attr;
    }

    private IP4JobConfiguration getConfiguration(TaskRepository repository) {
        return this.provider.getConfiguration(repository);
    }

    /**
     * Build the metadata of a task from the job spec with values from the job
     * if specified
     * 
     * @param data
     * @param repository
     * @param job
     * @param isNew
     * @throws CoreException
     */
    public void buildTask(TaskData data, TaskRepository repository, IP4Job job,
            boolean isNew) throws CoreException {
        IP4Connection connection = P4MylynUtils.getConnection(repository);
        if (connection == null) {
            throw new CoreException(P4MylynUtils.getConnectionErrorStatus());
        }
        if (!isNew && connection.isOffline()) {
            throw new CoreException(P4MylynUtils.getConnectionOfflineStatus());
        }
        IJobSpec spec = connection.getJobSpec();
        if (spec == null) {
            throw new CoreException(
                    P4MylynUtils
                            .getErrorStatus(Messages.P4JobDataHandler_JobspecRetrievalFailed));
        }
        TaskAttribute root = data.getRoot();
        TaskAttribute key = root.createAttribute(TaskAttribute.TASK_KEY);
        key.getMetaData().setKind(TaskAttribute.KIND_DEFAULT);
        TaskAttribute summary = root.createAttribute(TaskAttribute.SUMMARY);
        summary.getMetaData().setLabel(Messages.P4JobDataHandler_SummaryLabel);
        if (job != null && !isNew) {
            key.setValue(job.getId());
            summary.setValue(job.getShortDescription());
        } else {
            summary.setValue(Messages.P4JobDataHandler_NewJob);
        }
        IP4JobConfiguration config = getConfiguration(repository);
        for (IJobSpecField field : spec.getFields()) {
            String name = field.getName();
            if (!config.isCommentField(field)) {
                String dataType = field.getDataType();
                int code = field.getCode();
                String id = getFieldId(field, config, spec);
                String attributeType = getFieldType(code, dataType, name);
                boolean readOnly = isReadOnly(isNew, code, field.getFieldType());
                TaskAttribute attr = createAttribute(root, id, name,
                        attributeType, readOnly);
                Object value = null;
                if (isNew) {
                    value = getJobDefaultValue(job, connection, field,
                            spec.getFieldPreset(name));
                }
                if (value == null && job != null) {
                    value = job.getField(name);
                }

                if (IP4Job.SELECT_DATA_TYPE.equals(dataType)) {
                    List<String> values = spec.getFieldValues(name);
                    for (String fieldValue : values) {
                        attr.putOption(fieldValue, fieldValue);
                    }
                } else if (IP4Job.DATE_DATA_TYPE.equals(dataType)) {
                    if (value != null) {
                        Date date = P4MylynUtils.parseDate(value.toString());
                        if (date != null) {
                            value = Long.valueOf(date.getTime());
                        }
                    }
                }

                if (value != null) {
                    attr.setValue(value.toString());
                }
            } else {
            	if(job!=null){
	                Object value = job.getField(name);
	                if (value == null) {
	                    value = ""; //$NON-NLS-1$
	                }
	                String text = value.toString();
	                root.createAttribute(INTERNAL_COMMENT_FIELD).setValue(text);
	                config.buildCommentField(root, text, repository);
            	}
            }
        }
        config.setCompletionDate(root);
    }

    /**
     * Build the metadata of a task from the job spec with values from the job
     * if specified
     * 
     * @param data
     * @param repository
     * @param job
     * @throws CoreException
     */
    public void buildJobTask(TaskData data, TaskRepository repository,
            IP4Job job) throws CoreException {
        buildTask(data, repository, job, false);
    }

    private Object getJobDefaultValue(IP4Job template,
            IP4Connection connection, IJobSpecField field, String preset) {
        boolean set = false;
        Object defaultValue = null;
        if (template != null) {
            Object value = template.getField(field.getName());
            if (value != null) {
                defaultValue = value.toString();
                set = true;
            }
        }
        if (!set && preset != null) {
            String type = field.getFieldType();
            if (!IP4Job.OPTIONAL_FIELD_TYPE.equals(type)) {
                if (IP4Job.BLANK_PRESET.equals(preset)) {
                    defaultValue = IP4Job.BLANK_VALUE;
                } else if (IP4Job.USER_PRESET.equals(preset)) {
                    defaultValue = connection.getParameters().getUserNoNull();
                } else if (!IP4Job.NOW_PRESET.equals(preset)) {
                    defaultValue = preset;
                }
            }
        } else if (IP4Job.JOB_NAME_CODE == field.getCode()) {
            defaultValue = "new"; //$NON-NLS-1$
        }
        return defaultValue;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler#initializeTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.data.TaskData,
     *      org.eclipse.mylyn.tasks.core.ITaskMapping,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public boolean initializeTaskData(TaskRepository repository, TaskData data,
            ITaskMapping initializationData, IProgressMonitor monitor)
            throws CoreException {
        IP4Connection connection = P4MylynUtils.getConnection(repository);
        if (connection == null) {
            throw new CoreException(P4MylynUtils.getConnectionErrorStatus());
        }
        data.setPartial(true);
        IP4Job template = connection.getJob(""); //$NON-NLS-1$
        buildTask(data, repository, template, true);
        return true;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler#canGetMultiTaskData(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public boolean canGetMultiTaskData(TaskRepository taskRepository) {
        return true;
    }

    private String getQuery(String[] taskIds, int length, String idField) {
        StringBuilder query = new StringBuilder();
        query.append('(');
        String id = null;
        String separator = ""; //$NON-NLS-1$
        for (int i = 0; i < length; i++) {
            id = taskIds[i];
            query.append(separator);
            separator = OR_SEPARATOR;
            query.append(idField);
            query.append('=');
            id = CoreUtil.decode(id);
            id = P4MylynUtils.escapeJobQueryValue(id);
            query.append(id);
        }
        query.append(')');
        return query.toString();
    }

    private IP4Job[] fetchJobs(String[] ids, int length, String idField,
            IP4Connection connection) {
        String query = getQuery(ids, length, idField);
        return connection.getJobs(null, length, query);
    }

    private void collectJobs(IP4Job[] jobs, TaskRepository repository,
            TaskDataCollector collector, IProgressMonitor monitor)
            throws CoreException {
        for (IP4Job job : jobs) {
            this.connector.getCache().add(job);
            TaskData data = generateTaskData(repository, job);
            monitor.worked(1);
            collector.accept(data);
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler#getMultiTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      java.util.Set, org.eclipse.mylyn.tasks.core.data.TaskDataCollector,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void getMultiTaskData(TaskRepository repository,
            Set<String> taskIds, TaskDataCollector collector,
            IProgressMonitor monitor) throws CoreException {
        if (taskIds == null || taskIds.isEmpty()) {
            return;
        }
        IP4Connection connection = P4MylynUtils.getConnection(repository);
        if (connection == null) {
            throw new CoreException(P4MylynUtils.getConnectionErrorStatus());
        }
        if (connection.isOffline()) {
            throw new CoreException(P4MylynUtils.getConnectionOfflineStatus());
        }

        int taskCount = taskIds.size();
        monitor = Policy.monitorFor(monitor);
        monitor.beginTask(MessageFormat.format(
                Messages.P4JobConnector_FetchingJobs, connection
                        .getParameters().getPortNoNull()), taskCount);

        try {
            IJobSpec spec = connection.getJobSpec();
            if (spec == null) {
                throw new CoreException(
                        P4MylynUtils
                                .getErrorStatus(Messages.P4JobDataHandler_JobspecRetrievalFailed));
            }

            String idField = null;
            for (IJobSpecField field : spec.getFields()) {
                if (IP4Job.JOB_NAME_CODE == field.getCode()) {
                    idField = field.getName();
                    break;
                }
            }

            if (idField == null) {
                throw new CoreException(
                        P4MylynUtils
                                .getErrorStatus(Messages.P4JobDataHandler_JobIdFieldNotFound));
            }

            if (taskCount > 1) {
                if (taskCount <= MAX_QUERY_COUNT) {
                    IP4Job[] jobs = fetchJobs(
                            taskIds.toArray(new String[taskCount]), taskCount,
                            idField, connection);
                    collectJobs(jobs, repository, collector, monitor);
                } else {
                    String[] ids = new String[MAX_QUERY_COUNT];
                    String[] all = taskIds.toArray(new String[taskCount]);
                    int read = 0;
                    int left = taskCount;
                    while (left > 0) {
                        int chunkSize = Math.min(left, MAX_QUERY_COUNT);
                        System.arraycopy(all, read, ids, 0, chunkSize);
                        left -= chunkSize;
                        read += chunkSize;
                        IP4Job[] jobs = fetchJobs(ids, chunkSize, idField,
                                connection);
                        collectJobs(jobs, repository, collector, monitor);
                    }
                }
            } else {
                String id = CoreUtil.decode(taskIds.iterator().next());
                IP4Job job = connection.getJob(id);
                this.connector.getCache().add(job);
                TaskData data = generateTaskData(repository, job);
                collector.accept(data);
                monitor.worked(1);
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Generate task data from a job id and repository
     * 
     * @param repository
     * @param id
     * @return - task data
     */
    public TaskData generateTaskData(TaskRepository repository, String id) {
        TaskAttributeMapper mapper = getAttributeMapper(repository);
        TaskData data = new TaskData(mapper, connector.getConnectorKind(),
                repository.getUrl(), CoreUtil.encode(id));
        data.setPartial(true);
        return data;
    }

    /**
     * Generate task data from job and repository
     * 
     * @param repository
     * @param job
     * @return - task data
     * @throws CoreException
     */
    public TaskData generateTaskData(TaskRepository repository, IP4Job job)
            throws CoreException {
        TaskAttributeMapper mapper = getAttributeMapper(repository);
        TaskData data = new TaskData(mapper, connector.getConnectorKind(),
                repository.getUrl(), CoreUtil.encode(job.getId()));
        data.setPartial(false);
        buildJobTask(data, repository, job);
        return data;
    }

    /**
     * Get task key from a task data object
     * 
     * @param data
     * @return - task key or null if not found
     */
    public String getTaskKey(TaskData data) {
        String id = null;
        if (data != null) {
            TaskAttribute key = data.getRoot().getAttribute(
                    TaskAttribute.TASK_KEY);
            if (key != null) {
                id = key.getValue();
            }
        }
        return id;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler#postTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.data.TaskData, java.util.Set,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public RepositoryResponse postTaskData(TaskRepository repository,
            TaskData data, Set<TaskAttribute> oldAttributes,
            IProgressMonitor monitor) throws CoreException {
        IP4Connection connection = P4MylynUtils.getConnection(repository);
        if (connection == null) {
            throw new CoreException(P4MylynUtils.getConnectionErrorStatus());
        }
        if (connection.isOffline()) {
            throw new CoreException(P4MylynUtils.getConnectionOfflineStatus());
        }
        IJobSpec spec = connection.getJobSpec();
        if (spec == null) {
            throw new CoreException(
                    P4MylynUtils
                            .getErrorStatus(Messages.P4JobDataHandler_JobspecRetrievalFailed));
        }
        Map<String, Object> fields = new HashMap<String, Object>();

        TaskAttribute newComment = data.getRoot().getAttribute(
                TaskAttribute.COMMENT_NEW);

        IP4JobConfiguration config = getConfiguration(repository);

        TaskAttribute root = data.getRoot();
        for (IJobSpecField field : spec.getFields()) {
            String id = getFieldId(field, config, spec);
            TaskAttribute attribute = root.getAttribute(id);
            if (attribute != null) {
                Object value = null;
                String type = attribute.getMetaData().getType();
                if (TaskAttribute.TYPE_DATE.equals(type)
                        || TaskAttribute.TYPE_DATETIME.equals(type)) {
                    try {
                        long time = Long.parseLong(attribute.getValue());
                        value = P4MylynUtils.formatToP4Date(new Date(time));
                    } catch (NumberFormatException nfe) {
                        value = attribute.getValue();
                    }
                } else if (newComment != null && config.isCommentField(field)) {
                    TaskAttribute internalComment = data.getRoot()
                            .getAttribute(INTERNAL_COMMENT_FIELD);
                    String currentValue = ""; //$NON-NLS-1$
                    if (internalComment != null) {
                        currentValue = internalComment.getValue();
                    }
                    value = config.generateCommentFieldValue(connection,
                            newComment.getValue(), currentValue);
                } else {
                    value = attribute.getValue();
                }
                fields.put(field.getName(), value);
            }
        }

        if (data.isNew()) {
            try {
                IP4Job created = connection.createJob(fields);
                if(created!=null){
	                return new RepositoryResponse(ResponseKind.TASK_CREATED,
	                        created.getId());
                }else {
                	throw new CoreException(P4MylynUtils.getErrorStatus(Messages.P4JobConnector_JobRetrievalFailed));
                }
            } catch (P4JavaException e) {
                String message = e.getLocalizedMessage();
                if (message != null) {
                    message = message.trim();
                }
                throw new CoreException(P4MylynUtils.getErrorStatus(message, e));
            }
        } else {
            IJob updatedJob = new Job(null, fields);
            try {
                IServer server = connection.getServer();
                if (server != null) {
                    server.updateJob(updatedJob);
                }
            } catch (P4JavaException e) {
                String message = e.getLocalizedMessage();
                if (message != null) {
                    message = message.trim();
                }
                throw new CoreException(P4MylynUtils.getErrorStatus(message, e));
            } catch (P4JavaError e) {
                String message = e.getLocalizedMessage();
                if (message != null) {
                    message = message.trim();
                }
                throw new CoreException(P4MylynUtils.getErrorStatus(message, e));
            }
            return new RepositoryResponse(ResponseKind.TASK_UPDATED,
                    data.getTaskId());
        }
    }
}
