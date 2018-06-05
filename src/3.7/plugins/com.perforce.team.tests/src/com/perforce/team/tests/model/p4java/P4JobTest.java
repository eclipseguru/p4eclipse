/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.core.IJob;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.P4Job;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.tests.ConnectionBasedTestCase;

import org.eclipse.core.resources.IResource;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4JobTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createJob();
    }

    /**
     * Tests a retrieved job
     */
    public void testJob() {
        IP4Connection connection = createConnection();
        IP4Job[] jobs = connection.getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        IP4Job job = jobs[0];
        assertNotNull(job.getClient());
        assertNotNull(job.getConnection());
        assertSame(connection, job.getConnection());
        assertNotNull(job.getDescription());
        assertNotNull(job.getFieldNames());
        assertTrue(job.getFieldNames().length > 0);
        assertNotNull(job.getFieldValues());
        assertTrue(job.getFieldValues().length > 0);
        assertNotNull(job.getId());
        assertNotNull(job.getJob());
        assertNotNull(job.getName());
        assertEquals(job.getId(), job.getName());
        assertNotNull(job.getParent());
        assertSame(connection, job.getParent());
        assertNotNull(job.getShortDescription());

        try {
            job.refresh();
            job.refresh(IResource.DEPTH_ZERO);
            job.refresh(IResource.DEPTH_ONE);
            job.refresh(IResource.DEPTH_INFINITE);
        } catch (Exception e) {
            assertFalse("Exception thrown during refresh", true);
        }
    }

    /**
     * Test updating a job
     */
    public void testUpdateJob() {
        IP4Connection connection = createConnection();
        IP4Job[] jobs = connection.getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        IP4Job job = jobs[0];
        assertNotNull(job.getDescription());
        IJob p4jJob = job.getJob();
        assertNotNull(p4jJob);

        String descField = null;
        for (String field : p4jJob.getRawFields().keySet()) {
            if (field.equalsIgnoreCase("description")) {
                descField = field;
                break;
            } else if (field.equalsIgnoreCase("desc")) {
                descField = field;
                break;
            }
        }
        assertNotNull("Description field not found", descField);

        String newDescription = "Update description at: "
                + System.currentTimeMillis();

        p4jJob.getRawFields().put(descField, newDescription);

        try {
            job.update(p4jJob);
        } catch (P4JavaException exception) {
            handle(exception);
        }

        job.refresh();
        IJob newP4jJob = job.getJob();
        assertNotSame(p4jJob, newP4jJob);
        assertEquals(newDescription.trim(), job.getDescription().trim());
    }

    /**
     * Tests an empty job
     */
    public void testEmptyJob() {
        IP4Job job = new P4Job(null, null);
        assertNull(job.getActionPath());
        assertNull(job.getActionPath(Type.LOCAL));
        assertNull(job.getActionPath(Type.REMOTE));
        assertNull(job.getClient());
        assertNull(job.getClientPath());
        assertNull(job.getConnection());
        assertNull(job.getDescription());
        assertNull(job.getErrorHandler());
        assertNotNull(job.getFieldNames());
        assertEquals(0, job.getFieldNames().length);
        assertNotNull(job.getFieldValues());
        assertEquals(0, job.getFieldValues().length);
        assertNull(job.getId());
        assertNull(job.getJob());
        assertNull(job.getLocalPath());
        assertNull(job.getName());
        assertNull(job.getParent());
        assertNull(job.getRemotePath());
        assertNull(job.getShortDescription());
        assertEquals(job, job);
        assertFalse(job.isContainer());

        IP4Job job2 = new P4Job(null, null);
        assertFalse(job.equals(job2));
        assertFalse(job.hashCode() == job2.hashCode());
    }
}
