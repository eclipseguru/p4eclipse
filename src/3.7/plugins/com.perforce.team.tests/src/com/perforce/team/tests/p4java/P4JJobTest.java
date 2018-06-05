package com.perforce.team.tests.p4java;

import com.perforce.p4java.core.IJob;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Job;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JJobTest extends ConnectionBasedTestCase {

    /**
     * Test refresh
     */
    public void testRefresh() {
        IP4Connection connection = createConnection();
        IP4Job[] jobs = connection.getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);

        IJob job = jobs[0].getJob();
        assertNotNull(job);

        ((Job) job).setRawFields(null);
        assertNull(job.getRawFields());

        assertTrue(job.canRefresh());
        try {
            job.refresh();
        } catch (P4JavaException e) {
            handle(e);
        }

        assertNotNull(job.getRawFields());
        assertTrue(job.canRefresh());
    }

}
