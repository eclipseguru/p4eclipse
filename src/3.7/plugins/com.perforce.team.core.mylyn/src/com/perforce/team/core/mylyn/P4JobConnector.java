/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * @author alex
 * 
 */
public class P4JobConnector extends AbstractRepositoryConnector {

    /**
     * Simple job callback interface
     */
    public static interface IJobCallback {

        /**
         * Callback for async job loading
         * 
         * @param job
         */
        void loaded(IP4Job job);

    }

    private P4JobConfigurationManager manager;
    private P4JobDataHandler dataHandler;

    private JobCache jobCache;
    private ISchedulingRule jobCacheRule = P4Runner.createRule();

    /**
     * P4 job connector
     */
    public P4JobConnector() {
        this.manager = new P4JobConfigurationManager();
        this.dataHandler = new P4JobDataHandler(this.manager, this);
        this.jobCache = new JobCache();
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskIdPrefix()
     */
    @Override
    public String getTaskIdPrefix() {
        return Messages.P4JobConnector_Job;
    }

    /**
     * Get cached job
     * 
     * @param id
     * @param connection
     * @return - job
     */
    public IP4Job getCachedJob(String id, IP4Connection connection) {
        return getCachedJob(id, connection, false, null);
    }

    /**
     * Get cached job
     * 
     * @param id
     * @param connection
     * @param load
     * @return - job
     */
    public IP4Job getCachedJob(String id, IP4Connection connection, boolean load) {
        return getCachedJob(id, connection, load, null);
    }

    /**
     * Get cached job
     * 
     * @param id
     * @param connection
     * @param load
     * @param async
     * @return - job
     */
    public IP4Job getCachedJob(final String id, final IP4Connection connection,
            boolean load, final IJobCallback async) {
        IP4Job job = this.jobCache.get(id, connection);
        if (job == null && id != null && connection != null && load) {
            if (async == null) {
                job = connection.getJob(id);
                this.jobCache.add(job);
            } else {
                P4Runner.schedule(new P4Runnable() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        IP4Job loaded = connection.getJob(id);
                        jobCache.add(loaded);
                        if (loaded != null) {
                            async.loaded(loaded);
                        }
                    }

                    @Override
                    public String getTitle() {
                        return Messages.P4JobConnector_LoadingJob + id;
                    }

                }, jobCacheRule);
            }
        }
        return job;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#canCreateNewTask(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public boolean canCreateNewTask(TaskRepository repository) {
        return true;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#canCreateTaskFromKey(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public boolean canCreateTaskFromKey(TaskRepository repository) {
        return true;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getConnectorKind()
     */
    @Override
    public String getConnectorKind() {
        return IP4MylynConstants.KIND;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getLabel()
     */
    @Override
    public String getLabel() {
        return Messages.P4JobConnector_PerforceJobs;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getRepositoryUrlFromTaskUrl(java.lang.String)
     */
    @Override
    public String getRepositoryUrlFromTaskUrl(String taskFullUrl) {
        if (taskFullUrl != null) {
            int index = taskFullUrl.lastIndexOf('/');
            if (index > 0) {
                return taskFullUrl.substring(0, index);
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskIdsFromComment(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      java.lang.String)
     */
    @Override
    public String[] getTaskIdsFromComment(TaskRepository repository,
            String comment) {
        if (comment != null) {
            IP4JobConfiguration config = this.manager
                    .getConfiguration(repository);
            return config.getTaskIdsFromComment(repository, comment);
        }
        return null;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public TaskData getTaskData(TaskRepository taskRepository, String taskId,
            IProgressMonitor monitor) throws CoreException {
        IP4Connection connection = P4MylynUtils.getConnection(taskRepository);
        if (connection == null) {
            throw new CoreException(P4MylynUtils.getConnectionErrorStatus());
        }
        try {
            taskId=CoreUtil.decode(taskId);
        } catch (Throwable t) {
        }
        IP4Job job = connection.getJob(taskId); // We need reconsider whether should we decode or not.
        if (job == null) {
            throw new CoreException(
                    P4MylynUtils.getErrorStatus(MessageFormat.format(
                            Messages.P4JobConnector_JobRetrievalFailed, taskId)));
        }
        this.jobCache.add(job);
        return this.dataHandler.generateTaskData(taskRepository, job);
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskIdFromTaskUrl(java.lang.String)
     */
    @Override
    public String getTaskIdFromTaskUrl(String taskFullUrl) {
        if (taskFullUrl != null) {
            int index = taskFullUrl.lastIndexOf('/');
            if (index > -1 && index + 1 < taskFullUrl.length()) {
                return taskFullUrl.substring(index + 1);
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskUrl(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String getTaskUrl(String repositoryUrl, String taskId) {
        if (repositoryUrl != null && taskId != null) {
            return repositoryUrl + "/" + taskId; //$NON-NLS-1$
        } else {
            return null;
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#hasTaskChanged(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITask,
     *      org.eclipse.mylyn.tasks.core.data.TaskData)
     */
    @Override
    public boolean hasTaskChanged(TaskRepository repository, ITask task,
            TaskData taskData) {
        boolean changed = true;
        TaskMapper mapper = new TaskMapper(taskData);
        if (taskData.isPartial()) {
            changed = mapper.hasChanges(task);
        } else {
            Date last = task.getModificationDate();
            Date current = mapper.getModificationDate();
            if (last != null && current != null) {
                changed = !last.equals(current);
            }
        }
        return changed;
    }

	public boolean canQuery(TaskRepository repository) {
        IP4Connection connection = P4MylynUtils.getConnection(repository);
        if (connection == null || connection.isOffline()) {
        	return false;
        }
        return true;
	}
	
	public boolean canSynchronizeTask(TaskRepository taskRepository, ITask task) {
		return canQuery(taskRepository) && super.canSynchronizeTask(taskRepository, task);
	}
	
    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#performQuery(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.IRepositoryQuery,
     *      org.eclipse.mylyn.tasks.core.data.TaskDataCollector,
     *      org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus performQuery(TaskRepository repository,
            IRepositoryQuery query, TaskDataCollector collector,
            ISynchronizationSession session, IProgressMonitor monitor) {
        IP4Connection connection = P4MylynUtils.getConnection(repository);
        monitor = Policy.monitorFor(monitor);
        try {
            if (connection == null) {
                return P4MylynUtils.getConnectionErrorStatus();
            }
            monitor.beginTask(MessageFormat.format(
                    Messages.P4JobConnector_FetchingJobs, connection
                            .getParameters().getPortNoNull()),
                    IProgressMonitor.UNKNOWN);
            String value = query.getAttribute(IP4MylynConstants.P4_JOB_QUERY);
            if (value != null) {
                value = value.trim();
                if (value.length() == 0) {
                    value = null;
                }
            }
            int max = -1;
            String limit = query.getAttribute(IP4MylynConstants.P4_JOB_MAX);
            if (limit != null) {
                try {
                    max = Integer.parseInt(limit);
                    if (max <= 0) {
                        max = -1;
                    }
                } catch (NumberFormatException e) {
                    max = -1;
                }
            }

            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            SubProgressMonitor fetchMonitor = new SubProgressMonitor(monitor,
                    10);
            IP4Job[] jobs = null;
            try {
                fetchMonitor.beginTask("", 1); //$NON-NLS-1$
                jobs = connection.getJobs(new String[0], max, value);
                fetchMonitor.worked(1);
            } finally {
                fetchMonitor.done();
            }

            if (monitor.isCanceled() || connection.isOffline()) {
                return Status.CANCEL_STATUS;
            }

            SubProgressMonitor buildMonitor = new SubProgressMonitor(monitor,
                    10);
            try {
                buildMonitor.beginTask("", jobs.length); //$NON-NLS-1$
                for (IP4Job job : jobs) {
                    if (monitor.isCanceled() || connection.isOffline()) {
                        return Status.CANCEL_STATUS;
                    }
                    try {
                        this.jobCache.add(job);
                        buildMonitor.subTask(MessageFormat.format(
                                Messages.P4JobConnector_GeneratingTaskData,
                                job.getId()));
                        TaskData data = this.dataHandler.generateTaskData(
                                repository, job);
                        if (data != null) {
                            collector.accept(data);
                        }
                    } catch (CoreException e) {
                        return e.getStatus();
                    }
                    buildMonitor.worked(1);
                }
            } finally {
                buildMonitor.done();
            }
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    /**
     * Get job cache
     * 
     * @return - job cache
     */
    JobCache getCache() {
        return this.jobCache;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#getTaskDataHandler()
     */
    @Override
    public AbstractTaskDataHandler getTaskDataHandler() {
        return this.dataHandler;
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#updateRepositoryConfiguration(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void updateRepositoryConfiguration(TaskRepository repository,
            IProgressMonitor monitor) throws CoreException {
        monitor = Policy.monitorFor(monitor);
        IP4Connection connection = P4MylynUtils.getConnection(repository);
        if (connection == null) {
            throw new CoreException(P4MylynUtils.getConnectionErrorStatus());
        }
        monitor.beginTask(MessageFormat.format(
                Messages.P4JobConnector_UpdatingJobspec, connection
                        .getParameters().getPortNoNull()), 1);
        connection.refreshJobSpec();
        monitor.worked(1);
        monitor.done();
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector#updateTaskFromTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITask,
     *      org.eclipse.mylyn.tasks.core.data.TaskData)
     */
    @Override
    public void updateTaskFromTaskData(TaskRepository taskRepository,
            ITask task, TaskData taskData) {
        TaskMapper mapper = new TaskMapper(taskData);
        mapper.applyTo(task);
        this.manager.getConfiguration(taskRepository).updateTaskFromTaskData(
                taskRepository, task, taskData);
    }
}
