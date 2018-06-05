/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.tests.ProjectBasedTestCase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4EventTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        addFile(project.getFile("plugin.xml"));
        addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
        createJob();
    }

    /**
     * Test p4 event
     */
    public void testP4Event() {
        IP4Resource resource = P4Workspace.getWorkspace().getResource(project);
        assertNotNull(resource);
        assertTrue(resource.isContainer());
        assertTrue(resource instanceof IP4Folder);
        IP4Folder folder = (IP4Folder) resource;
        IP4Connection connection = createConnection();
        String depotPath = folder.getFirstWhereRemotePath();
        IP4Container container = connection.getFolder(depotPath, true);
        assertNotNull(container);
        container.refresh(IResource.DEPTH_ONE);
        for (IP4Resource member : container.members()) {
            if (member instanceof IP4Folder) {
                ((IP4Folder) member).updateLocation();
            }
        }
        P4Event event = new P4Event(EventType.REFRESHED, new P4Collection(
                container.members()));
        assertNotNull(event.getType());
        assertSame(EventType.REFRESHED, event.getType());
        assertNotNull(event.getChangelists());
        assertTrue(event.getChangelists().length == 0);
        assertNotNull(event.getCommonConnections());
        assertTrue(event.getCommonConnections().length > 0);
        assertNotNull(event.getContainers());
        assertTrue(event.getContainers().length > 0);
        assertNotNull(event.getFiles());
        assertTrue(event.getFiles().length > 0);
        assertNotNull(event.getLocalContainers());
        assertTrue(event.getLocalContainers().length > 0);
        assertNotNull(event.getLocalFiles());
        assertTrue(event.getLocalFiles().length > 0);
        assertNotNull(event.getLocalResources());
        assertTrue(event.getLocalResources().length > 0);
        assertNotNull(event.getResources());
        assertTrue(event.getResources().length > 0);
        assertNotNull(event.toString());
        assertNotNull(event.getOpenedFiles());
        assertEquals(0, event.getOpenedFiles().length);
    }

    /**
     * Test array constructor
     */
    public void testP4ArrayConstructor() {
        IP4Resource resource = P4Workspace.getWorkspace().getResource(project);
        assertNotNull(resource);
        assertTrue(resource.isContainer());
        assertTrue(resource instanceof IP4Folder);
        IP4Folder folder = (IP4Folder) resource;
        IP4Connection connection = createConnection();
        String depotPath = folder.getFirstWhereRemotePath();
        IP4Container container = connection.getFolder(depotPath, true);
        assertNotNull(container);
        container.refresh(IResource.DEPTH_ONE);
        for (IP4Resource member : container.members()) {
            if (member instanceof IP4Folder) {
                ((IP4Folder) member).updateLocation();
            }
        }
        P4Event event = new P4Event(EventType.REFRESHED, container.members());
        assertNotNull(event.getType());
        assertSame(EventType.REFRESHED, event.getType());
        assertNotNull(event.getChangelists());
        assertTrue(event.getChangelists().length == 0);
        assertNotNull(event.getCommonConnections());
        assertTrue(event.getCommonConnections().length > 0);
        assertNotNull(event.getContainers());
        assertTrue(event.getContainers().length > 0);
        assertNotNull(event.getFiles());
        assertTrue(event.getFiles().length > 0);
        assertNotNull(event.getLocalContainers());
        assertTrue(event.getLocalContainers().length > 0);
        assertNotNull(event.getLocalFiles());
        assertTrue(event.getLocalFiles().length > 0);
        assertNotNull(event.getLocalResources());
        assertTrue(event.getLocalResources().length > 0);
        assertNotNull(event.getResources());
        assertTrue(event.getResources().length > 0);
        assertNotNull(event.toString());
    }

    /**
     * Test an event containing a p4 connection
     */
    public void testConnectionEvent() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);
        P4Event event = new P4Event(EventType.CHANGED, connection);
        assertNotNull(event.getLocalContainers());
        assertEquals(1, event.getLocalContainers().length);
        assertEquals(project, event.getLocalContainers()[0]);
    }

    /**
     * Test the p4 event type enum
     */
    public void testEventTypes() {
        EventType[] types = P4Event.EventType.values();
        assertNotNull(types);
        assertTrue(types.length > 0);
        for (EventType type : types) {
            assertNotNull(type);
            assertNotNull(type.toString());
            assertEquals(type, EventType.valueOf(type.toString()));
        }
    }

    /**
     * Test empty p4 event
     */
    public void testEmptyP4Event() {
        P4Event event = new P4Event(null, (P4Collection) null);
        assertNotNull(event.getChangelists());
        assertEquals(0, event.getChangelists().length);
        assertNotNull(event.getCommonConnections());
        assertEquals(0, event.getCommonConnections().length);
        assertNotNull(event.getContainers());
        assertEquals(0, event.getContainers().length);
        assertNotNull(event.getFiles());
        assertEquals(0, event.getFiles().length);
        assertNotNull(event.getLocalContainers());
        assertEquals(0, event.getLocalContainers().length);
        assertNotNull(event.getLocalFiles());
        assertEquals(0, event.getLocalFiles().length);
        assertNotNull(event.getLocalResources());
        assertEquals(0, event.getLocalResources().length);
        assertNotNull(event.getResources());
        assertEquals(0, event.getResources().length);
        assertNull(event.getType());
        assertNotNull(event.toString());
    }

    /**
     * Test event with job
     */
    public void testWithJob() {
        IP4Connection connection = createConnection();
        IP4Job[] jobs = connection.getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        assertNotNull(jobs[0]);

        P4Event event = new P4Event(EventType.UNFIXED, jobs);
        assertNotNull(event.getJobs());
        assertEquals(1, event.getJobs().length);
        assertEquals(jobs[0], event.getJobs()[0]);
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
