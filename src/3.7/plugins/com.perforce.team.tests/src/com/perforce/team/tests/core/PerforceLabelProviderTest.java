/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.PerforceLabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceLabelProviderTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createJob();
    }

    /**
     * Test invalid label provider
     */
    public void testInvalid() {
        PerforceLabelProvider provider = new PerforceLabelProvider(false);
        assertNull(provider.getColumnImage(null, 0));
        assertNull(provider.getColumnImage(null, -1));
        assertNull(provider.getColumnText(null, 0));
        assertNull(provider.getColumnText(null, -1));
        provider = new PerforceLabelProvider(true);
        assertNull(provider.getColumnImage(null, 0));
        assertNull(provider.getColumnImage(null, -1));
        assertNull(provider.getColumnText(null, 0));
        assertNull(provider.getColumnText(null, -1));

        assertFalse(provider.isLabelProperty(null, null));

        assertNotNull(provider.getColumnText(new Object(), 0));
        assertNotNull(provider.getColumnImage(new Object(), 0));
        assertNotNull(provider.getColumnText(new Object(), 1));
    }

    /**
     * Test job label
     */
    public void testJob() {
        PerforceLabelProvider provider = new PerforceLabelProvider(false);
        IP4Connection connection = createConnection();
        IP4Job[] jobs = connection.getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        assertNotNull(provider.getColumnText(jobs[0], 0));
        assertNotNull(provider.getColumnImage(jobs[0], 0));
    }

    /**
     * Test offline connection label
     */
    public void testOfflineConnection() {
        IP4Connection connection = createConnection();
        connection.setOffline(true);
        PerforceLabelProvider provider = new PerforceLabelProvider(false);
        assertNotNull(provider.getColumnText(connection, 0));
        assertNotNull(provider.getColumnImage(connection, 0));
    }

    /**
     * Test depot label
     */
    public void testDepot() {
        IP4Connection connection = createConnection();
        IP4Resource[] members = connection.members();
        assertNotNull(members);
        assertTrue(members.length > 0);
        assertTrue(members[0] instanceof P4Depot);
        P4Depot depot = (P4Depot) members[0];
        PerforceLabelProvider provider = new PerforceLabelProvider(false);
        assertNotNull(provider.getColumnText(depot, 0));
        assertNotNull(provider.getColumnImage(depot, 0));
    }

}
