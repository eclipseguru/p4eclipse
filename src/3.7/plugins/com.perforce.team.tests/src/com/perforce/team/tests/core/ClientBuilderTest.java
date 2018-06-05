/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.ErrorHandler;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.builder.ClientBuilder;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ClientBuilderTest extends ConnectionBasedTestCase {

    /**
     * Test creating a client
     */
    public void testCreate() {
        createConnection();
        ConnectionParameters create = new ConnectionParameters();
        parameters.copy(create);
        create.setClient("clientTest" + System.currentTimeMillis());
        ClientBuilder builder = new ClientBuilder(create, "/tmp/client"
                + System.currentTimeMillis());
        try {
            assertTrue(builder.build());
            IClient created = P4Workspace.getWorkspace().getClient(
                    create.getPort(), create.getUser(), create.getClient());
            assertNotNull(created);
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown: " + e.getMessage(), true);
        }
    }

    /**
     * Test creating a client with the location ending with a trailing slash
     */
    public void testCreateWithTrailingSlash() {
        createConnection();
        ConnectionParameters create = new ConnectionParameters();
        parameters.copy(create);
        create.setClient("clientTest" + System.currentTimeMillis());
        ClientBuilder builder = new ClientBuilder(create, "/tmp/client"
                + System.currentTimeMillis() + "/");
        try {
            assertTrue(builder.build());
            IClient created = P4Workspace.getWorkspace().getClient(
                    create.getPort(), create.getUser(), create.getClient());
            assertNotNull(created);
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown: " + e.getMessage(), true);
        }
    }

    /**
     * Test creating a client with the location being the empty string
     */
    public void testCreateWithEmptyLocation() {
        createConnection();
        ConnectionParameters create = new ConnectionParameters();
        parameters.copy(create);
        create.setClient("clientTest" + System.currentTimeMillis());
        ClientBuilder builder = new ClientBuilder(create, "");
        try {
            assertTrue(builder.build());
            IClient created = P4Workspace.getWorkspace().getClient(
                    create.getPort(), create.getUser(), create.getClient());
            assertNotNull(created);
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown: " + e.getMessage(), true);
        }
    }

    /**
     * Test creating a client failing
     */
    public void testFailure() {
        createConnection();
        ConnectionParameters create = new ConnectionParameters();
        parameters.copy(create);
        ClientBuilder builder = new ClientBuilder(create, "/tmp/client"
                + System.currentTimeMillis());
        try {
            assertFalse(builder.build());
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown: " + e.getMessage(), true);
        }
    }

    /**
     * Test client template
     */
    public void testTemplate() {
        ConnectionParameters params = new ConnectionParameters();
        parameters.copy(params);
        String name = "test_non_existent" + System.currentTimeMillis();
        String root = "/client/location";
        params.setClient(name);
        ClientBuilder builder = new ClientBuilder(params, root);
        IClient client = null;
        try {
            client = builder.getClientTemplate();
        } catch (P4JavaException e) {
            assertFalse("P4J exception thrown", true);
        }
        assertNotNull(client);
        assertEquals(name, client.getName());
        assertNull(client.getAccessed());
        assertNull(client.getUpdated());
    }

    /**
     * Test client template
     */
    public void testTemplateHandler() {
        ConnectionParameters params = new ConnectionParameters();
        parameters.copy(params);
        String name = "test_non_existent" + System.currentTimeMillis();
        String root = "/client/location";
        params.setClient(name);
        ClientBuilder builder = new ClientBuilder(params, root);
        IErrorHandler handler = new ErrorHandler() {

        };
        IClient client = builder.getClientTemplate(handler);
        assertNotNull(client);
        assertEquals(name, client.getName());
        assertNull(client.getAccessed());
        assertNull(client.getUpdated());
    }

    /**
     * Test build exception
     */
    public void testBuildException() {
        ConnectionParameters params = new ConnectionParameters();
        parameters.copy(params);
        String name = "test_non_existent" + System.currentTimeMillis();
        String root = "/client/location";
        params.setClient(name);
        ClientBuilder builder = new ClientBuilder(params, root);
        IP4Connection connection = new P4Connection(params) {

            @Override
            public IServer getServer() {
                return null;
            }
        };
        try {
            builder.build(connection);
            assertFalse("Exception not thrown", true);
        } catch (P4JavaException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test trying to create a client with an existing depot name
     */
    public void testExisting() {
        ConnectionParameters params = new ConnectionParameters();
        parameters.copy(params);
        IP4Connection connection = createConnection();
        IP4Resource[] depots = connection.members();
        assertNotNull(depots);
        assertTrue(depots.length > 0);
        assertTrue(depots[0] instanceof P4Depot);
        String name = depots[0].getName();
        assertNotNull(name);
        params.setClient(name);
        ClientBuilder builder = new ClientBuilder(params, "/location/existing");
        final P4JavaException[] exceptions = new P4JavaException[] { null };
        IErrorHandler handler = new ErrorHandler() {

            @Override
            public boolean shouldRetry(IP4Connection connection,
                    P4JavaException exception) {
                exceptions[0] = exception;
                return false;
            }

        };
        assertFalse(builder.build(handler));
        assertNotNull(exceptions[0]);
    }
}
