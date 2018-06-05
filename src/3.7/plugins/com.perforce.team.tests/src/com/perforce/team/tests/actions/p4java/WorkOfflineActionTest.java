/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.WorkOfflineAction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class WorkOfflineActionTest extends ProjectBasedTestCase {

    /**
     * Tests the action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        WorkOfflineAction offline = new WorkOfflineAction();

        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        try {
            assertFalse(connection.isOffline());

            offline.selectionChanged(wrap, new StructuredSelection(project));
            assertTrue(wrap.isEnabled());
            connection.setOffline(true);
            assertTrue(connection.isOffline());
            offline.selectionChanged(wrap, new StructuredSelection(project));
            assertFalse(wrap.isEnabled());
        } finally {
            connection.setOffline(false);
            connection.connect();
            assertTrue(connection.isConnected());
        }
    }

    /**
     * Tests the work online action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        WorkOfflineAction offline = new WorkOfflineAction();
        offline.setAsync(false);
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        try {
            connection.setOffline(false);
            assertFalse(connection.isOffline());
            offline.selectionChanged(wrap, new StructuredSelection(project));
            assertTrue(wrap.isEnabled());

            final List<P4Event> events = new ArrayList<P4Event>();
            IP4Listener listener = new IP4Listener() {

                public void resoureChanged(P4Event event) {
                    events.add(event);
                }
				public String getName() {
					return WorkOfflineActionTest.this.getClass().getSimpleName();
				}
            };
            P4Workspace.getWorkspace().addListener(listener);
            offline.run(wrap);
            P4Workspace.getWorkspace().removeListener(listener);
            assertTrue(connection.isOffline());

            assertEquals(1, events.size());
            P4Event changed = events.get(0);
            assertEquals(EventType.CHANGED, changed.getType());
            assertNotNull(changed.getCommonConnections());
            assertEquals(1, changed.getCommonConnections().length);
            assertEquals(connection, changed.getCommonConnections()[0]);
        } finally {
            connection.setOffline(false);
            connection.connect();
            assertTrue(connection.isConnected());
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }
}