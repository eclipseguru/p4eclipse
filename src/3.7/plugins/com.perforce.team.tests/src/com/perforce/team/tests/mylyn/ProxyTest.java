/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.job.JobProxy;
import com.perforce.team.ui.mylyn.job.TaskProxy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ProxyTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createJob();
    }

    /**
     * Test job proxy
     */
    public void testJob() {
        IP4Job[] jobs = createConnection().getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        IP4Job job = jobs[0];
        JobProxy proxy = new JobProxy(job);
        assertEquals(job.getId(), proxy.getId());
        assertEquals(job.getConnection(), proxy.getConnection());
        assertEquals(job, proxy.getAdapter(IP4Job.class));
        assertEquals(job, proxy.getAdapter(IP4Resource.class));
        assertEquals(job.hashCode(), proxy.hashCode());
        assertNotNull(proxy.getChildren(proxy));
        assertNotNull(proxy.getLabel(proxy));
        assertNotNull(proxy.getImageDescriptor(proxy));
        JobProxy proxy2 = new JobProxy(job);
        assertEquals(proxy2, proxy);
    }

    /**
     * Test task proxy
     */
    public void testTask() {
        TaskRepository repository = P4MylynUiUtils
                .createTaskRepository(createConnection());
        assertNotNull(repository);
        AbstractRepositoryConnector connector = P4MylynUiUtils
                .getPerforceConnector();
        assertNotNull(connector);
        IP4Job[] jobs = createConnection().getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        IP4Job job = jobs[0];
        try {
            TaskData data = connector.getTaskData(repository, job.getId(),
                    new NullProgressMonitor());
            assertNotNull(data);
            ITask task = new TaskTask(connector.getConnectorKind(),
                    repository.getRepositoryUrl(), job.getId());
            assertEquals("", task.getSummary());
            connector.updateTaskFromTaskData(repository, task, data);
            TaskProxy proxy = new TaskProxy(task);
            assertEquals(task.getTaskId(), proxy.getId());
            assertEquals(task, proxy.getAdapter(ITask.class));
            assertEquals(task.hashCode(), proxy.hashCode());
            assertNotNull(proxy.getChildren(proxy));
            assertNotNull(proxy.getLabel(proxy));
            assertNotNull(proxy.getImageDescriptor(proxy));
            TaskProxy proxy2 = new TaskProxy(task);
            assertEquals(proxy2, proxy);
        } catch (CoreException e) {
            handle(e);
        }
    }
}
