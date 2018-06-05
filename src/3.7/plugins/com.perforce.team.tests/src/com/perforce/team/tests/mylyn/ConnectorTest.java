/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.mylyn.P4JobConnector;
import com.perforce.team.core.mylyn.P4JobConnector.IJobCallback;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;

import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectorTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createJob();
        createJob();
    }

    /**
     * Test {@link P4JobConnector}
     */
    public void testConnector() {
        AbstractRepositoryConnector connector = P4MylynUiUtils
                .getPerforceConnector();
        assertNotNull(connector);
        assertEquals(IP4MylynConstants.KIND, connector.getConnectorKind());
        assertNotNull(connector.getLabel());
        assertNotNull(connector.getTaskDataHandler());
        assertNotNull(connector.getTaskIdPrefix());
        assertTrue(connector.canCreateNewTask(null));
        assertTrue(connector.canCreateTaskFromKey(null));
        assertNull(connector.getTaskIdsFromComment(null, null));
    }

    /**
     * Test job caching
     */
    public void testJobCaching() {
        P4JobConnector connector = P4MylynUiUtils.getPerforceConnector();

        assertNull(connector.getCachedJob(null, null));
        assertNull(connector.getCachedJob(null, null, true));
        assertNull(connector.getCachedJob(null, null, true, null));

        IP4Connection connection = createConnection();

        assertNull(connector.getCachedJob(null, connection));
        assertNull(connector.getCachedJob(null, connection, true));
        assertNull(connector.getCachedJob(null, connection, true, null));

        IP4Job[] jobs = connection.getJobs(2);
        assertNotNull(jobs);
        assertEquals(2, jobs.length);
        IP4Job syncJob = jobs[0];
        IP4Job asyncJob = jobs[1];

        final IP4Job[] loaded = new IP4Job[] { null };

        loaded[0] = connector.getCachedJob(syncJob.getId(), connection, true,
                null);
        assertNotSame(syncJob, loaded[0]);
        assertEquals(syncJob.getId(), loaded[0].getId());
        assertEquals(syncJob.getDescription(), loaded[0].getDescription());

        loaded[0] = null;
        assertNull(connector.getCachedJob(asyncJob.getId(), connection, true,
                new IJobCallback() {

                    public void loaded(IP4Job job) {
                        loaded[0] = job;
                    }
                }));
        while (loaded[0] == null) {
            Utils.sleep(.1);
        }
        assertNotSame(asyncJob, loaded[0]);
        assertEquals(asyncJob.getId(), loaded[0].getId());
        assertEquals(asyncJob.getDescription(), loaded[0].getDescription());
    }

    /**
     * Test task-repo url building and parsing
     */
    public void testUrl() {
        AbstractRepositoryConnector connector = P4MylynUiUtils
                .getPerforceConnector();
        assertNotNull(connector);
        assertNull(connector.getTaskUrl(null, null));
        assertNull(connector.getTaskIdFromTaskUrl(null));
        assertNull(connector.getRepositoryUrlFromTaskUrl(null));

        String task = "job1";
        String repo = "server:1666";

        String taskUrl = connector.getTaskUrl(repo, task);
        assertNotNull(taskUrl);
        String parsedTask = connector.getTaskIdFromTaskUrl(taskUrl);
        assertNotNull(parsedTask);
        assertEquals(task, parsedTask);
        String parsedRepo = connector.getRepositoryUrlFromTaskUrl(taskUrl);
        assertNotNull(parsedRepo);
        assertEquals(repo, parsedRepo);
    }

}
