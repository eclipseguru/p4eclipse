/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.P4ConnectionManager;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobTaskSynchronizer implements IP4Listener {

    /**
     * Create a job task synchronizer listening to p4 events
     */
    public JobTaskSynchronizer() {
        P4ConnectionManager.getManager().addListener(this);
    }

    /**
     * Dispose of the synchronizer
     */
    public void dispose() {
        P4ConnectionManager.getManager().removeListener(this);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
     */
    public void resoureChanged(P4Event event) {
        EventType type = event.getType();
        if (type == EventType.FIXED || type == EventType.UNFIXED
                || type == EventType.SUBMIT_JOB) {
            Set<ITask> tasks = null;
            for (IP4Job job : event.getJobs()) {
                TaskRepository repository = P4MylynUiUtils.findRepository(
                        job.getConnection(), IP4MylynConstants.KIND);
                if (repository != null) {
                    ITask task = P4MylynUiUtils.getTask(
                            repository.getRepositoryUrl(), job.getId());
                    if (task != null) {
                        if (tasks == null) {
                            tasks = new HashSet<ITask>();
                        }
                        tasks.add(task);
                    }
                }
            }
            if (tasks != null && !tasks.isEmpty()) {
                TasksUiInternal.synchronizeTasks(
                        P4MylynUiUtils.getPerforceConnector(), tasks, false,
                        null);
            }
        }
    }

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
}
