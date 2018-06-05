/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.extensions;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ListenerExtensionPointTest extends ConnectionBasedTestCase
        implements IP4Listener {

    private static final List<P4Event> EVENTS = new ArrayList<P4Event>();

    private static boolean start = false;

    private static void addEvent(P4Event event) {
        if (start) {
            EVENTS.add(event);
        }
    }

    /**
     * Test the com.perforce.team.core.workspace listener extension point
     */
    public void testExtensionPoint() {
        try {
            assertTrue(EVENTS.isEmpty());

            P4Workspace.getWorkspace().addListener(this);
            start = true;
            IP4Connection connection = createConnection();
            P4Event event = new P4Event(EventType.AVAILABLE, connection);
            P4Workspace.getWorkspace().notifyListeners(event);
            assertTrue(EVENTS.contains(event));
            assertEquals(1, EVENTS.size());
        } finally {
            start = false;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
     */
    public void resoureChanged(P4Event event) {
        addEvent(event);
    }
}
