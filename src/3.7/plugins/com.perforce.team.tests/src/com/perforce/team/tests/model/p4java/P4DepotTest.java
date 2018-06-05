/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.impl.generic.core.Depot;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.util.HashMap;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4DepotTest extends ConnectionBasedTestCase {

    /**
     * Tests the p4 depot object
     */
    public void testP4Depot() {
        IP4Connection connection = createConnection();
        IP4Resource[] members = connection.members();
        assertNotNull(members);
        assertTrue(members.length > 0);
        boolean depotProcessed = false;
        boolean localFound = false;
        for (IP4Resource resource : members) {
            if (resource instanceof P4Depot) {
                P4Depot depot = (P4Depot) resource;
                depotProcessed = true;
                assertNotNull(depot.getName());
                if (depot.getName().contains("depot")) {
                    assertTrue(depot.isLocal());
                    localFound = true;
                }
                assertNotNull(depot.getActionPath());
                assertNotNull(depot.getRemotePath());
                assertNotNull(depot.getParent());
                assertSame(connection, depot.getParent());
                assertNotNull(depot.toString());
                assertNull(depot.getLocalPath());
                assertNull(depot.getClientPath());
                assertNotNull(depot.getClient());
                assertNotNull(depot.getConnection());
                assertSame(connection, depot.getConnection());
                IP4Resource[] folders = depot.members();
                assertNotNull(folders);
                assertSame(folders, depot.members());
                assertEquals(depot, depot);
                assertFalse(depot.equals(connection));
                assertFalse(depot.equals(new P4Depot(null, connection)));
            }
        }
        assertTrue(localFound);
        assertTrue(depotProcessed);
    }

    /**
     * Tests an empty depot
     */
    public void testEmptyDepot() {
        P4Depot depot = new P4Depot(null, null);
        assertNull(depot.getActionPath());
        assertNull(depot.getClient());
        assertNull(depot.getClientPath());
        assertNull(depot.getConnection());
        assertNull(depot.getErrorHandler());
        assertNotNull(depot.getAllLocalFiles());
        assertEquals(0, depot.getAllLocalFiles().length);
        assertNull(depot.getLocalPath());
        assertNull(depot.getName());
        assertNull(depot.getParent());
        assertNull(depot.getRemotePath());
        assertFalse(depot.isLocal());
        depot.setLocalPath("/test");
        assertEquals("/test", depot.getLocalPath());
    }

    /**
     * Test {@link P4Depot#equals(Object)} methods
     */
    public void testEquals() {
        Depot meta = new Depot(new HashMap<String, Object>());
        meta.setName("testdepot");
        P4Depot depot1 = new P4Depot(meta, createConnection());
        P4Depot depot2 = new P4Depot(meta, createConnection());
        P4Depot depot3 = new P4Depot(meta, new P4Connection(
                new ConnectionParameters()));
        assertEquals(depot1, depot2);
        assertFalse(depot1.equals(depot3));
    }
}
