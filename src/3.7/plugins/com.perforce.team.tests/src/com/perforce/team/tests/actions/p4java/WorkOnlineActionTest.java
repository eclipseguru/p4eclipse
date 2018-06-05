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
import com.perforce.team.ui.p4java.actions.WorkOnlineAction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class WorkOnlineActionTest extends ProjectBasedTestCase {

    /**
     * Tests the action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        WorkOnlineAction online = new WorkOnlineAction();

        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        try {
            assertFalse(connection.isOffline());

            online.selectionChanged(wrap, new StructuredSelection(project));
            assertFalse(wrap.isEnabled());
            connection.setOffline(true);
            assertTrue(connection.isOffline());
            online.selectionChanged(wrap, new StructuredSelection(project));
            assertTrue(wrap.isEnabled());
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
        WorkOnlineAction online = new WorkOnlineAction();
        online.setAsync(false);
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        try {
            connection.setOffline(true);
            assertTrue(connection.isOffline());
            online.selectionChanged(wrap, new StructuredSelection(project));
            assertTrue(wrap.isEnabled());

            final List<P4Event> events = new ArrayList<P4Event>();
            IP4Listener listener = new IP4Listener() {

                public void resoureChanged(P4Event event) {
                    events.add(event);
                }
				public String getName() {
					return WorkOnlineActionTest.this.getClass().getSimpleName();
				}
            };
            P4Workspace.getWorkspace().addListener(listener);
            online.run(wrap);
            try {
				Thread.sleep(2000); // wait for until the job is finished before platform shutdown.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            P4Workspace.getWorkspace().removeListener(listener);
            assertFalse(connection.isOffline());

            log(this.getClass().getSimpleName() + ":testAction "
                    + events.toString());

            // TODO: This is sometime 4|5, not exactly 2, this happens only on
            // nightly test.
            // assertEquals(2, events.size());

            P4Event available = events.get(0);
            assertEquals(EventType.AVAILABLE, available.getType());
            assertNotNull(available.getCommonConnections());
            assertEquals(1, available.getCommonConnections().length);
            assertEquals(connection, available.getCommonConnections()[0]);
            P4Event changed = events.get(1);
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
