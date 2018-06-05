package com.perforce.team.tests.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JClientTest extends ConnectionBasedTestCase {

    /**
     * Test create job034386
     */
    public void testCreate() {
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

        IClient spec = new Client();
        spec.setName("testCreate" + System.currentTimeMillis());
        spec.setOwnerName(liveParams.getUser());
        spec.setDescription(spec.getName());
        spec.setRoot("/testCreateRoot");
        String created;
        try {
            created = connection.getServer().createClient(spec);
            assertNotNull(created);
        } catch (Throwable e) {
            e.printStackTrace();
            handle(e);
        }
    }

    /**
     * Test refresh
     */
    public void testRefresh() {
        IP4Connection connection = createConnection();
        IClientSummary[] clients = connection.getOwnedClients();
        assertNotNull(clients);
        assertTrue(clients.length > 0);

        IClientSummary client = clients[0];

        assertFalse(client.canRefresh());
    }

}
