/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import com.perforce.p4java.PropertyDefs;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper;
import com.perforce.p4java.server.callback.ILogCallback.LogTraceLevel;
import com.perforce.team.core.p4java.ConnectionMappedException;
import com.perforce.team.core.p4java.IEventObject;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Workspace;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4WorkspaceTest extends P4WorkspaceListenerTest {

    /**
     * Tests the {@link IEventObject} interface through the {@link P4Workspace}
     * class
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
				return P4WorkspaceTest.this.getClass().getSimpleName();
			}
        };
        try {
            P4Workspace.getWorkspace().addListener(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        try {
            P4Workspace.getWorkspace().addListeners(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        P4Workspace.getWorkspace().addListener(listener);
        P4Workspace.getWorkspace().addListener(listener);
        P4Workspace.getWorkspace().addListeners(new IP4Listener[] { listener });
        try {
            P4Workspace.getWorkspace().addListeners(
                    new IP4Listener[] { listener, null });
        } catch (Exception e) {
            assertFalse(true);
        }
        P4Event event1 = new P4Event(EventType.CHANGED, file);
        P4Workspace.getWorkspace().notifyListeners(event1);
        try {
            P4Workspace.getWorkspace().removeListener(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        P4Workspace.getWorkspace().removeListener(listener);
        P4Event event2 = new P4Event(EventType.SUBMITTED, file);
        P4Workspace.getWorkspace().notifyListeners(event2);

        assertEquals(1, events.size());
        assertSame(event1, events.get(0));

        P4Workspace.getWorkspace().addListener(listener);
        P4Workspace.getWorkspace().notifyListeners(event2);
        P4Workspace.getWorkspace().clearListeners();
        P4Event event3 = new P4Event(EventType.SUBMITTED, file);
        P4Workspace.getWorkspace().notifyListeners(event3);

        assertEquals(2, events.size());
        assertSame(event1, events.get(0));
        assertSame(event2, events.get(1));
    }

    /**
     * Test basic methods on p4 workspace
     */
    public void testBasic() {
        assertNotNull(P4Workspace.getWorkspace().getErrorHandler());
        assertFalse(P4Workspace.getWorkspace().handleError(
                new P4JavaException()));
        ISystemFileCommandsHelper original = P4Workspace.getWorkspace()
                .getFileHelper();
        assertNotNull(original);
        try {
            ISystemFileCommandsHelper helper = new ISystemFileCommandsHelper() {

                public boolean setWritable(String arg0, boolean arg1) {
                    return false;
                }

                public boolean setExecutable(String arg0, boolean arg1,
                        boolean ownerOnly) {
                    return false;
                }

                public boolean isSymlink(String arg0) {
                    return false;
                }

                public boolean canExecute(String arg0) {
                    return false;
                }

                public boolean setReadable(String fileName, boolean readable,
                        boolean ownerOnly) {
                    return false;
                }

                public boolean setOwnerReadOnly(String fileName) {
                    return false;
                }
            };
            P4Workspace.getWorkspace().setFileHelper(helper);
            assertEquals(helper, P4Workspace.getWorkspace().getFileHelper());
            P4Workspace.getWorkspace().setTraceLevel(LogTraceLevel.COARSE);
            assertEquals(LogTraceLevel.COARSE, P4Workspace.getWorkspace()
                    .getTraceLevel());
        } finally {
            P4Workspace.getWorkspace().setFileHelper(original);
            P4Workspace.getWorkspace().setTraceLevel(null);
        }
        assertFalse(P4Workspace.getWorkspace().isPersistOffline());
        P4Workspace.getWorkspace().handleErrors(new IFileSpec[0]);
    }

    /**
     * Tests resource retrieval
     */
    public void testResource() {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject("testtest_project");
        assertFalse(project.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(project);
        assertNull(resource);
        resource = P4Workspace.getWorkspace().getResource(null);
        assertNull(resource);
        resource = P4Workspace.getWorkspace().getResource(
                project.getFile("test.xml"));
        assertNull(resource);
    }

    /**
     * Tests connection adding and removing
     */
    public void testConnection() {
        assertFalse(P4Workspace.getWorkspace().containsConnection(null));

        IP4Connection connection = createConnection();
        assertEquals(
                connection,
                P4Workspace.getWorkspace().getConnection(
                        connection.getParameters()));
        assertTrue(P4Workspace.getWorkspace().containsConnection(
                connection.getParameters()));

        try {
            P4Workspace.getWorkspace().removeConnection(
                    connection.getParameters());
        } catch (ConnectionMappedException e) {
            assertFalse("Connection mapped exception thrown", true);
        }
        assertFalse(P4Workspace.getWorkspace().containsConnection(
                connection.getParameters()));

        P4Workspace.getWorkspace().getConnection(connection.getParameters());
        assertTrue(P4Workspace.getWorkspace().containsConnection(
                connection.getParameters()));
        try {
            P4Workspace.getWorkspace().removeConnection(connection);
        } catch (ConnectionMappedException e) {
            assertFalse("Connection mapped exception thrown", true);
        }
        assertFalse(P4Workspace.getWorkspace().containsConnection(
                connection.getParameters()));
    }

    /**
     * Test connection testing
     */
    public void testConnect() {
        String port = parameters.getPort();
        try {
            assertTrue(P4Workspace.getWorkspace().canConnect(port));
        } catch (P4JavaException e) {
            assertFalse("p4j exception thrown connecting", true);
        }
        String badPort = "bad_port:9090";
        try {
            assertFalse(P4Workspace.getWorkspace().canConnect(badPort));
        } catch (P4JavaException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test zprogram property setting
     */
    public void testZProgram() {
        assertNotNull(P4Workspace.getWorkspace().getServerProperties()
                .getProperty(PropertyDefs.PROG_NAME_KEY));
        assertEquals(P4Workspace.PROG_NAME, P4Workspace.getWorkspace()
                .getServerProperties().getProperty(PropertyDefs.PROG_NAME_KEY));
    }

    /**
     * Test zversion property setting
     */
    public void testZVersion() {
        assertNotNull(P4Workspace.getWorkspace().getServerProperties()
                .getProperty(PropertyDefs.PROG_VERSION_KEY));
        assertFalse(P4Workspace.VERSION_UNKNOWN.equals(P4Workspace
                .getWorkspace().getServerProperties()
                .getProperty(PropertyDefs.PROG_NAME_KEY)));
        assertTrue(P4Workspace.getWorkspace().getServerProperties()
                .getProperty(PropertyDefs.PROG_VERSION_KEY).trim().length() > 0);
    }
}
