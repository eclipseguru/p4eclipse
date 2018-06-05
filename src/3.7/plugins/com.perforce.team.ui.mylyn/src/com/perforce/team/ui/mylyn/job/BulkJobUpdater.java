/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Job;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.job.JobFieldEntry.FieldChange;
import com.perforce.team.ui.mylyn.job.JobFieldEntry.FieldChange.Type;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BulkJobUpdater {

    /**
     * Callback interface for the new and old job states for an updated job
     */
    public static interface JobCallback {

        /**
         * Job updated
         * 
         * @param oldJob
         * @param newJob
         */
        void updated(IP4Job oldJob, IP4Job newJob);
    }

    /**
     * TIME_FORMAT
     */
    public static final String TIME_FORMAT = "hh:mm aaa"; //$NON-NLS-1$

    private IJobProxy[] updateJobs = null;
    private IJobSpec spec = null;
    private FieldChange[] filters = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);

    /**
     * Create a bulk job updater
     * 
     * @param jobs
     * @param spec
     * @param filters
     */
    public BulkJobUpdater(IJobProxy[] jobs, IJobSpec spec, FieldChange[] filters) {
        this.updateJobs = jobs;
        this.spec = spec;
        this.filters = filters;
    }

    private Map<String, Object> fillMap(IP4Job job) {
        Map<String, Object> jobFields = new HashMap<String, Object>();
        String name = null;
        Object value = null;
        // Add current values
        for (IJobSpecField field : spec.getFields()) {
            name = field.getName();
            value = job.getField(name);
            if (value == null) {
                value = ""; //$NON-NLS-1$
            }
            jobFields.put(name, value);
        }
        return jobFields;
    }

    /**
     * Run the update
     * 
     * @param monitor
     * @param callback
     * @return - error string
     */
    public String run(IProgressMonitor monitor, JobCallback callback) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        int total = updateJobs.length;
        monitor.beginTask("", total); //$NON-NLS-1$
        int passCount = 0;
        int jobCount = 0;
        String base = Messages.BulkJobUpdater_UpdateJobWithLink;
        List<IP4Job> eventJobs = new ArrayList<IP4Job>();
        List<IJobProxy> eventTasks = new ArrayList<IJobProxy>();
        StringBuilder errors = new StringBuilder();
        try {
            for (IJobProxy job : updateJobs) {
                if (monitor.isCanceled()) {
                    break;
                }
                jobCount++;
                monitor.setTaskName(MessageFormat.format(base, job.getId(),
                        jobCount, total));
                IP4Job realJob = job.getConnection().getJob(job.getId());
                Map<String, Object> jobFields = fillMap(realJob);
                // Add changed values
                for (FieldChange filter : filters) {
                    String value = filter.value;
                    if (filter.type == Type.APPEND) {
                        Object current = jobFields.get(filter.name);
                        if (current != null) {
                            value = current.toString() + value;
                        }
                    } else if (filter.type == Type.PREPEND) {
                        Object current = jobFields.get(filter.name);
                        if (current != null) {
                            value += current.toString();
                        }
                    }
                    jobFields.put(filter.name, value);
                }
                try {
                    realJob.update(createJob(jobFields));
                    if(job.getConnection()!=null){
                    	IP4Job updated = job.getConnection().getJob(job.getId());
	                    if (updated != null) {
	                        eventJobs.add(updated);
	                        eventTasks.add(job);
	                        if (callback != null) {
	                            callback.updated(realJob, updated);
	                        }
	                    }
	                    passCount++;
                    }
                } catch (P4JavaException e) {
                    PerforceProviderPlugin.logError(e);
                    errors.append('\n');
                    errors.append(job.getId());
                    errors.append(':');
                    errors.append('\n');
                    errors.append(e.getLocalizedMessage());
                }
                monitor.worked(1);
            }
        } finally {
            String viewErrors = ""; //$NON-NLS-1$
            if (errors.length() > 0) {
                viewErrors = Messages.BulkJobUpdater_ViewErrorsLink;
            }
            sendEvents(eventJobs, eventTasks);
            monitor.done();
            monitor.setTaskName(MessageFormat.format(
                    Messages.BulkJobUpdater_UpdateFinished, passCount,
                    updateJobs.length, updateJobs.length > 1
                            ? Messages.BulkJobUpdater_Jobs
                            : Messages.BulkJobUpdater_Job, formatTime(),
                    viewErrors));
        }
        return errors.toString().trim();
    }

    private void sendEvents(List<IP4Job> jobs, List<IJobProxy> proxies) {
        if (jobs.size() > 0) {
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.REFRESHED, jobs
                            .toArray(new IP4Resource[jobs.size()])));
        }
        if (proxies.size() > 0) {
            Set<ITask> tasks = new HashSet<ITask>();
            for (IJobProxy proxy : proxies) {
                ITask task = P4CoreUtils.convert(proxy, ITask.class);
                if (task != null) {
                    tasks.add(task);
                }
            }
            if (tasks.size() > 0) {
                TasksUiInternal.synchronizeTasks(
                        P4MylynUiUtils.getPerforceConnector(), tasks, false,
                        null);
            }
        }
    }

    private String formatTime() {
        return dateFormat.format(new Date());
    }

    private Job createJob(Map<String, Object> fields) {
        return new Job(null, fields);
    }

}
