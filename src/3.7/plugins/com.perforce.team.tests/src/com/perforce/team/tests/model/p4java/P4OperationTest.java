/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4ClientOperation;
import com.perforce.team.core.p4java.IP4ServerOperation;
import com.perforce.team.core.p4java.P4ClientOperation;
import com.perforce.team.core.p4java.P4ServerOperation;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4OperationTest extends P4TestCase {

    /**
     * Test server operation
     */
    public void testServerOperation() {
        IP4ServerOperation op = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {

            }
        };
        try {
            op.run(null);
            op.exception(new P4JavaException());
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown", true);
        } catch (P4JavaError e) {
            assertFalse("P4JavaError thrown", true);
        }
    }

    /**
     * Test client operation
     */
    public void testClientOperation() {
        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {

            }
        };
        try {
            op.run(null);
            op.exception(new P4JavaException());
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown", true);
        } catch (P4JavaError e) {
            assertFalse("P4JavaError thrown", true);
        }
    }

}
