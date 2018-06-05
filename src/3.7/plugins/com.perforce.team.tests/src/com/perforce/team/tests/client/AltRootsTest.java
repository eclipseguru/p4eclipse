/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.client;

import com.perforce.p4java.server.IOptionsServer;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.util.Arrays;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AltRootsTest extends ConnectionBasedTestCase {

    /**
     * Test alt roots client
     */
    public void testAlt() {
        IP4Connection connection = createConnection();
        assertNotNull(connection.getServer());
        assertNotNull(connection.getClient());

        // Verify server has JVM working directory and connection none since the
        // current root is usable
        assertNotNull(connection.getServer().getWorkingDirectory());
        assertNull(connection.getCurrentDirectory());

        // Change root to alt root and update with bad root
        connection.getClient().setAlternateRoots(
                Arrays.asList(connection.getClient().getRoot()));
        connection.getClient().setRoot("\\\badroot");
        try {
            connection.getClient().update();
        } catch (Throwable e) {
            handle(e);
        }

        // Verify original working directory api on IServer class
        connection = createConnection();
        assertNotNull(connection.getClient());
        assertNotNull(connection.getClient().getAlternateRoots());
        assertEquals(1, connection.getClient().getAlternateRoots().size());
        assertNotNull(connection.getServer().getWorkingDirectory());
        assertNotNull(connection.getCurrentDirectory());
        assertEquals(connection.getServer().getWorkingDirectory(), connection
                .getClient().getAlternateRoots().get(0));

        // Verify usage options
        assertTrue(connection.getServer() instanceof IOptionsServer);
        IOptionsServer options = (IOptionsServer) connection.getServer();
        assertNotNull(options.getUsageOptions());
        assertEquals(connection.getClient().getAlternateRoots().get(0), options
                .getUsageOptions().getWorkingDirectory());
        assertEquals(connection.getCurrentDirectory(), options
                .getUsageOptions().getWorkingDirectory());
    }
}
