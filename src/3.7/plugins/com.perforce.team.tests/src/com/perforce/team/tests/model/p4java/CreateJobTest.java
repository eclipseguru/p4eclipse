/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CreateJobTest extends ConnectionBasedTestCase {

    /**
     * Test create a job
     */
    public void testCreate() {
        IP4Connection connection = createConnection();
        IJobSpec spec = connection.getJobSpec();
        assertNotNull(spec);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("Job", "new");
        fields.put("Description",
                "Test created job at: " + System.currentTimeMillis());
        fields.put("Status", "open");
        fields.put("User", connection.getUser());
        try {
            IP4Job created = connection.createJob(fields);
            assertNotNull(created);
            assertNotNull(created.getJob());
            assertNotNull(created.getId());
            assertTrue(created.getId().length() > 0);
            assertFalse(fields.get("Job").equals(created.getId()));
            assertEquals(fields.get("Description"), created.getDescription()
                    .trim());
            assertEquals(fields.get("Status"), created.getField("Status"));
            assertEquals(connection.getUser(), created.getField("User"));
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown: " + e.getMessage(), true);
        }
    }

}
