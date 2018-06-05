/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.FixJobAction;
import com.perforce.team.ui.p4java.actions.UnfixJobAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FixingTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createJob();
    }

    /**
     * Tests fixing and unfixing of a job
     */
    public void testFixing() {
        IP4Connection connection = createConnection();

        Action wrap = Utils.getDisabledAction();
        FixJobAction fix = new FixJobAction();
        fix.setAsync(false);
        fix.selectionChanged(wrap, new StructuredSelection(connection));
        assertFalse(wrap.isEnabled());

        IP4PendingChangelist newList = connection.createChangelist("test fix: "
                + getName(), null);
        try {
            assertNotNull(newList);
            assertNotNull(newList.getChangelist());
            assertTrue(newList.getId() > 0);

            assertNotNull(newList.getJobs());
            assertEquals(0, newList.getJobs().length);

            fix.selectionChanged(wrap, new StructuredSelection(newList));
            assertTrue(wrap.isEnabled());

            String jobId = "job000001";

            IP4Job[] jobs = connection.getJobs(null, 1, "job=" + jobId);

            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            assertEquals(jobId, jobs[0].getId());

            fix.runAction(jobs);

            assertNotNull(newList.getJobs());
            assertEquals(1, newList.getJobs().length);
            assertEquals(jobs[0], newList.getJobs()[0]);

            UnfixJobAction unfix = new UnfixJobAction();
            unfix.setAsync(false);
            unfix.selectionChanged(wrap, new StructuredSelection(connection));
            assertFalse(wrap.isEnabled());

            unfix.selectionChanged(wrap,
                    new StructuredSelection(newList.getJobs()[0]));
            assertTrue(wrap.isEnabled());

            unfix.runAction();

            assertNotNull(newList.getJobs());
            assertEquals(0, newList.getJobs().length);

        } finally {
            if (newList != null) {
                newList.delete();
                assertNull(newList.getChangelist());
            }
        }

    }

}
