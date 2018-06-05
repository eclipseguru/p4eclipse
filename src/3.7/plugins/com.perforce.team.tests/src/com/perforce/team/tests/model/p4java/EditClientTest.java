/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditClientTest extends ConnectionBasedTestCase {

    /**
     * Test edit client
     */
    public void testEdit() throws P4JavaException {
        IP4Connection connection = createConnection();
        IClient client = connection.getClient();
        assertNotNull(client);
        IClient newSpec = new Client();
        newSpec.setAlternateRoots(client.getAlternateRoots());
        newSpec.setClientView(client.getClientView());
        newSpec.setDescription("Edited description in EditClientTest.testEdit at: "
                + System.currentTimeMillis());
        newSpec.setHostName(client.getHostName());
        newSpec.setLineEnd(client.getLineEnd());
        newSpec.setName(client.getName());
        newSpec.setOptions(client.getOptions());
        newSpec.setOwnerName(client.getOwnerName());
        newSpec.setRoot(client.getRoot());
        newSpec.setSubmitOptions(client.getSubmitOptions());

        // Need sleep here to ensure update time advances since that is in
		// seconds
		Utils.sleep(1);
		connection.updateClient(newSpec);

        connection.connect();
        assertTrue(connection.isConnected());
        IClient newClient = connection.getClient();
        assertNotNull(newClient);
        assertEquals(newSpec.getDescription(), newClient.getDescription());
        assertEquals(newSpec.getHostName(), newClient.getHostName());
        assertEquals(newSpec.getName(), newClient.getName());
        assertEquals(newSpec.getOwnerName(), newClient.getOwnerName());
        assertEquals(newSpec.getRoot(), newClient.getRoot());
        assertTrue(newClient.getUpdated().after(client.getUpdated()));
    }

}
