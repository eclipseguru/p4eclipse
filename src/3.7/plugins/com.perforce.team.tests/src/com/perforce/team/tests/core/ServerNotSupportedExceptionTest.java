/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.ServerNotSupportedException;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerNotSupportedExceptionTest extends ConnectionBasedTestCase {

    private String unsupportedServer = null;

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertNotNull(System.getProperty("p4.port.unsupported"));
        unsupportedServer = System.getProperty("p4.port.unsupported");
    }

    /**
     * Test the server not supported exception being thrown
     */
    public void testServerNotSupported() {
        ConnectionParameters params = new ConnectionParameters();
        params.setPort(unsupportedServer);
        params.setUser("user");
        params.setClient("client");
        final List<ServerNotSupportedException> exceptions = new ArrayList<ServerNotSupportedException>();
        IErrorHandler handler = new IErrorHandler() {

            public boolean shouldRetry(IP4Connection connection,
                    P4JavaException exception) {
                if (exception instanceof ServerNotSupportedException) {
                    exceptions.add((ServerNotSupportedException) exception);
                }
                return false;
            }

            public void handleErrorSpecs(IFileSpec[] specs) {

            }

        };
        IP4Connection connection = new P4Connection(params);
        connection.setErrorHandler(handler);
        assertTrue(exceptions.isEmpty());
        connection.refreshServer();
        assertFalse(exceptions.isEmpty());
        assertEquals(1, exceptions.size());

    }
}
