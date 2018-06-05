package com.perforce.team.tests.p4java;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.JobSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JJobSpecTest extends ConnectionBasedTestCase {

    /**
     * Test refresh
     */
    public void testRefresh() {
        IP4Connection connection = createConnection();
        IJobSpec spec = connection.getJobSpec();
        assertNotNull(spec);

        assertTrue(spec.canRefresh());

        ((JobSpec) spec).setFields(null);
        assertNull(spec.getFields());

        try {
            spec.refresh();
        } catch (P4JavaException e) {
            handle(e);
        }

        assertNotNull(spec.getFields());
        assertTrue(spec.canRefresh());
    }

}
