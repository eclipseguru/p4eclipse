/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;

import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.ServerNotSupportedException;
import com.perforce.team.ui.P4ConnectionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4ConnectionManagerTest extends P4WorkspaceListenerTest {

    /**
     * Tests the connection manager
     */
    public void testManager() {
        P4Workspace.getWorkspace().clear();
        IP4Connection connection = createConnection();
        assertFalse(P4ConnectionManager.getManager().containsConnection(
                connection.getParameters()));

        IP4Connection created = P4ConnectionManager.getManager().getConnection(
                connection.getParameters());
        assertNotNull(created);
        assertTrue(P4ConnectionManager.getManager().containsConnection(
                created.getParameters()));
        assertEquals(connection, created);
    }

    /**
     * Test remove parameters
     */
    public void testRemoveParameters() {
        P4Workspace.getWorkspace().clear();
        IP4Connection connection = createConnection();
        assertFalse(P4ConnectionManager.getManager().containsConnection(
                connection.getParameters()));

        IP4Connection created = P4ConnectionManager.getManager().getConnection(
                connection.getParameters());
        assertNotNull(created);
        assertTrue(P4ConnectionManager.getManager().containsConnection(
                created.getParameters()));
        P4ConnectionManager.getManager().removeConnection(
                connection.getParameters());
        assertFalse(P4ConnectionManager.getManager().containsConnection(
                connection.getParameters()));
    }

    /**
     * Test remove connection
     */
    public void testRemoveConnection() {
        P4Workspace.getWorkspace().clear();
        IP4Connection connection = createConnection();
        assertFalse(P4ConnectionManager.getManager().containsConnection(
                connection.getParameters()));

        IP4Connection created = P4ConnectionManager.getManager().getConnection(
                connection.getParameters());
        assertNotNull(created);
        assertTrue(P4ConnectionManager.getManager().containsConnection(
                created.getParameters()));
        P4ConnectionManager.getManager().removeConnection(connection);
        assertFalse(P4ConnectionManager.getManager().containsConnection(
                connection.getParameters()));
    }

    private Object[] currentListeners = null;

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Reset extension point listeners
        try {
            Field listeners = P4Workspace.class.getDeclaredField("listeners");
            assertNotNull(listeners);
            listeners.setAccessible(true);
            Object instanceListeners = listeners
                    .get(P4Workspace.getWorkspace());
            assertNotNull(instanceListeners);
            assertTrue(instanceListeners instanceof ListenerList);
            currentListeners = ((ListenerList) instanceListeners)
                    .getListeners();
        } catch (Exception e) {
            assertFalse("Exception resetting extension point listeners", true);
        }
    }

    /**
     * Tests the connection manager listener interface
     */
    public void testListener() {
        final List<P4Event> events = new ArrayList<P4Event>();
        IP4File file = new P4File(createConnection(), null);
        IP4Listener listener = new IP4Listener() {

            public void resoureChanged(P4Event event) {
                events.add(event);
                if (events.size() % 2 == 0) {
                    throw new Error("test error");
                } else {
                    throw new NullPointerException("test exception");
                }
            }
			public String getName() {
				return P4ConnectionManagerTest.this.getClass().getSimpleName();
			}
        };
        try {
            P4ConnectionManager.getManager().addListener(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        try {
            P4ConnectionManager.getManager().addListeners(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        P4ConnectionManager.getManager().addListener(listener);
        P4ConnectionManager.getManager().addListener(listener);
        P4ConnectionManager.getManager().addListeners(
                new IP4Listener[] { listener });
        try {
            P4ConnectionManager.getManager().addListeners(
                    new IP4Listener[] { listener, null });
        } catch (Exception e) {
            assertFalse(true);
        }
        P4Event event1 = new P4Event(EventType.CHANGED, file);
        P4ConnectionManager.getManager().notifyListeners(event1);
        try {
            P4ConnectionManager.getManager().removeListener(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        P4ConnectionManager.getManager().removeListener(listener);
        P4Event event2 = new P4Event(EventType.SUBMITTED, file);
        P4ConnectionManager.getManager().notifyListeners(event2);

        assertEquals(1, events.size());
        assertSame(event1, events.get(0));

        P4ConnectionManager.getManager().addListener(listener);
        P4ConnectionManager.getManager().notifyListeners(event2);
        P4ConnectionManager.getManager().clearListeners();
        P4Event event3 = new P4Event(EventType.SUBMITTED, file);
        P4ConnectionManager.getManager().notifyListeners(event3);

        assertEquals(2, events.size());
        assertSame(event1, events.get(0));
        assertSame(event2, events.get(1));
    }

    /**
     * Test connection error detection
     */
    public void testConnectionErrorDetection() {
        assertFalse(P4ConnectionManager.isConnectionError(null));
        assertTrue(P4ConnectionManager
                .isConnectionError(new ConnectionException("test")));
        assertFalse(P4ConnectionManager.isConnectionError(new P4JavaException(
                "test2")));
    }

    /**
     * Test command line error detection
     */
    public void testCommandLineErrorDetection() {
        assertFalse(P4ConnectionManager.isCommandLineError(null));
        assertFalse(P4ConnectionManager.isCommandLineError(new P4JavaException(
                "test2")));
        assertFalse(P4ConnectionManager.isCommandLineError(new ConfigException(
                (String) null)));
        assertTrue(P4ConnectionManager.isCommandLineError(new ConfigException(
                "no such command line executable found")));
    }

    /**
     * Test login error detection
     */
    public void testLoginErrorDetection() {
        assertFalse(P4ConnectionManager.isLoginError(null));
        assertFalse(P4ConnectionManager.isLoginError("test"));
        assertTrue(P4ConnectionManager.isLoginError("invalid or unset"));
        assertTrue(P4ConnectionManager.isLoginError("please login again"));
        assertTrue(P4ConnectionManager.isLoginError("The pipe is being closed"));
    }

    /**
     * Test client error detection
     */
    public void testClientErrorDetection() {
        assertFalse(P4ConnectionManager.isClientNonExistentError(null));
        assertFalse(P4ConnectionManager.isClientNonExistentError("test2"));
        assertTrue(P4ConnectionManager
                .isClientNonExistentError("Client 'test123' does not exist"));

    }

    /**
     * Test server not supported detection
     */
    public void testServerNotSupportedDetection() {
        assertFalse(P4ConnectionManager.isServerNotSupportedError(null));
        assertFalse(P4ConnectionManager
                .isServerNotSupportedError(new P4JavaException("test")));
        assertTrue(P4ConnectionManager
                .isServerNotSupportedError(new ServerNotSupportedException(
                        "test")));
    }

    /**
     * Test message dialog methods on p4 connection manager to ensure suppress
     * errors is correctly checked
     */
    public void testMessageDialog() {
        assertFalse(P4ConnectionManager.getManager().openConfirm(null, null,
                null));
        P4ConnectionManager.getManager().openError(null, null, null);
        P4ConnectionManager.getManager().openInformation(null, null, null);
    }

}
