package com.perforce.team.tests.p4java;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JChangelistTest extends ConnectionBasedTestCase {

    /**
     * Test submitted update
     */
    public void testUpdate() {
        assertNotNull(System.getProperty("p4.client.live"));
        assertNotNull(System.getProperty("p4.user.live"));
        assertNotNull(System.getProperty("p4.password.live"));
        assertNotNull(System.getProperty("p4.port.live"));
        ConnectionParameters liveParams = new ConnectionParameters();
        liveParams.setClient(System.getProperty("p4.client.live"));
        liveParams.setUser(System.getProperty("p4.user.live"));
        liveParams.setPort(System.getProperty("p4.port.live"));
        liveParams.setPassword(System.getProperty("p4.password.live"));
        IP4Connection connection = createConnection(liveParams);

        IP4PendingChangelist pending = connection
                .createChangelist("test", null);
        try {
            assertNotNull(pending);

            IChangelist list = pending.getChangelist();
            assertNotNull(list);

            assertTrue(list.canUpdate());

            String description = "Test description"
                    + System.currentTimeMillis();

            ((Changelist) list).setDescription(description);
            assertNotNull(list.getDescription());

            try {
                list.update();
                list.setDescription(null);
                assertNull(list.getDescription());
                list.refresh();
            } catch (P4JavaException e) {
                handle(e);
            }

            assertTrue(list.canUpdate());
            assertEquals(description, list.getDescription().trim());
        } finally {
            pending.delete();
            assertNull(pending.getChangelist());
        }
    }

    /**
     * Test refresh
     */
    public void testWholeRefresh() {
        IP4Connection connection = createConnection();
        IP4SubmittedChangelist lists = connection.getSubmittedChangelistById(1);
        assertNotNull(lists);

        IChangelist list = lists.getChangelist();
        assertNotNull(list);

        assertTrue(list.canRefresh());

        list.setDescription(null);
        assertNull(list.getDescription());

        try {
            list.refresh();
        } catch (P4JavaException e) {
            handle(e);
        }

        assertNotNull(list.getDescription());
    }

    /**
     * Test refresh
     */
    public void testSubmittedRefresh() {
        IP4Connection connection = createConnection();
        IP4SubmittedChangelist[] lists = connection.getSubmittedChangelists(1);
        assertNotNull(lists);
        assertEquals(1, lists.length);

        IChangelist list = lists[0].getChangelist();
        assertNotNull(list);

        assertTrue(list.canRefresh());

        list.setDescription(null);
        assertNull(list.getDescription());

        try {
            list.refresh();
        } catch (P4JavaException e) {
            handle(e);
        }

        assertNotNull(list.getDescription());
        assertTrue(list.canRefresh());
    }

}
