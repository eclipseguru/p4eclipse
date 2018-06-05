/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4DefaultChangelist;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4PendingChangelist;
import com.perforce.team.tests.ProjectBasedTestCase;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4ChangelistTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        IClient client = createSecondUserAndClient();
        createPendingChangelist(client);
        openFile(client, client.getServer().getChangelist(0), client.getRoot()
                + File.separator + "openedFile.txt");
        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Tests the standard p4 pending changelist class
     */
    public void testChangelist() {
        IP4Connection connection = createConnection();
        IP4Changelist changelist = new P4PendingChangelist(connection, null,
                false);
        assertNull(changelist.getActionPath());
        assertFalse(changelist.isDefault());
        assertTrue(changelist.isContainer());
        assertEquals(-1, changelist.getId());
        assertNull(changelist.getStatus());
        assertNotNull(changelist.getName());
        assertNull(changelist.getDate());
        assertTrue(changelist.needsRefresh());
        changelist.refresh(IResource.DEPTH_INFINITE);
        assertFalse(changelist.needsRefresh());
        changelist.markForRefresh();
        assertTrue(changelist.needsRefresh());
        assertNotNull(changelist.getShortDescription());
        assertNull(changelist.getDescription());
        assertTrue(changelist.isReadOnly());
        assertNull(changelist.getUserName());
        assertNull(changelist.getClientName());
        assertNull(changelist.getAllLocalFiles());
        assertNull(changelist.getLocalPath());
        assertNull(changelist.getClientPath());
        assertNull(changelist.getRemotePath());
        assertEquals(0, changelist.size());
        assertNotNull(changelist.getJobIds());
        assertEquals(0, changelist.getJobIds().length);
    }

    /**
     * Test of created default changelist
     */
    public void testCreatedDefault() {
        IP4Connection connection = createConnection();
        IP4Changelist changelist = new P4DefaultChangelist(connection, null,
                true);
        assertNull(changelist.getActionPath());
        assertTrue(changelist.isDefault());
        assertTrue(changelist.isContainer());
        assertEquals(0, changelist.getId());
        assertNotNull(changelist.getStatus());
        assertNotNull(changelist.getName());
        assertNull(changelist.getDate());
        assertTrue(changelist.needsRefresh());
        changelist.refresh(IResource.DEPTH_INFINITE);
        assertFalse(changelist.needsRefresh());
        changelist.markForRefresh();
        assertTrue(changelist.needsRefresh());
        assertNotNull(changelist.getShortDescription());
        assertNull(changelist.getDescription());
        assertTrue(changelist.isReadOnly());
        assertNull(changelist.getUserName());
        assertNull(changelist.getClientName());
        assertNull(changelist.getAllLocalFiles());
    }

    /**
     * Test the default changelist
     */
    public void testDefault() {
        IP4Connection connection = createConnection();
        IP4Changelist changelist = connection.getPendingChangelist(0);
        assertNotNull(changelist);
        assertNotNull(changelist.getName());
        assertTrue(changelist.isDefault());
        assertFalse(changelist.isReadOnly());
        assertEquals(0, changelist.getId());
        assertNotNull(changelist.getUserName());
        assertNotNull(changelist.getClientName());
        assertNull(changelist.getDate());
        assertNotNull(changelist.getDescription());
        changelist.setDescription("test");
        assertNotNull(changelist.getDescription());
        changelist.refresh();
        assertFalse(changelist.needsRefresh());
        changelist.markForRefresh();
        assertTrue(changelist.needsRefresh());
        assertNotNull(changelist.getStatus());
        assertEquals(ChangelistStatus.PENDING, changelist.getStatus());
    }

    /**
     * Test default changelist of other user
     */
    public void testOtherDefault() {
        IP4Connection connection = createConnection();
        IP4PendingChangelist otherDefault = null;
        IP4PendingChangelist[] lists = connection.getPendingChangelists(true);
        for (IP4PendingChangelist list : lists) {
            if (list.isDefault() && !list.isOnClient()) {
                otherDefault = list;
                break;
            }
        }
        assertNotNull(otherDefault);
        assertFalse(otherDefault.needsRefresh());
        otherDefault.markForRefresh();
        assertTrue(otherDefault.needsRefresh());
        otherDefault.refresh();
        assertFalse(otherDefault.needsRefresh());
        assertNotNull(otherDefault.getStatus());
        assertEquals(ChangelistStatus.PENDING, otherDefault.getStatus());
        assertEquals(IChangelist.DEFAULT, otherDefault.getId());
        assertNotNull(otherDefault.getClient());
    }

    /**
     * Tests the deletion of a changelist
     */
    public void testDeleteEmptyChangelist() {
        IP4Connection connection = createConnection();
        IP4Changelist list = connection.createChangelist("test: " + getName(),
                new IP4File[0]);
        assertNotNull(list.getChangelist());
        assertNotNull(list.getStatus());
        int id = list.getId();
        assertTrue(id > 0);
        assertSame(ChangelistStatus.PENDING, list.getStatus());
        list.delete();
        assertNull(list.getChangelist());
        assertNull(list.getStatus());
        assertNull(connection.getPendingChangelist(id));
    }

    /**
     * Tests creating and modifying a changelist
     */
    public void testCreatedChangelist() {
        IP4Connection connection = createConnection();
        IP4Changelist list = connection.createChangelist("test: " + getName(),
                new IP4File[0]);
        assertNotNull(list);
        assertNotNull(list.getChangelist());
        assertNotNull(list.getStatus());
        assertSame(ChangelistStatus.PENDING, list.getStatus());

        String description = "change description";
        list.setDescription(description);
        assertEquals(description, list.getDescription());
        assertNotNull(list.getName());

        list.delete();
        assertNull(list.getChangelist());
        assertNull(list.getStatus());
    }

    /**
     * Tests the deletion of a changelist
     */
    public void testDeleteChangelist() {
        IP4Connection connection = createConnection();
        IFile local = this.project.getFile("plugin.xml");
        assertTrue(local.exists());
        IP4File file = new P4File(connection, local.getLocation()
                .makeAbsolute().toOSString());
        file.refresh();
        file.edit();
        IP4Changelist list = connection.createChangelist("test: " + getName(),
                new IP4File[] { file });
        assertNotNull(list.getChangelist());
        assertNotNull(list.getStatus());
        list.refresh();
        IP4Resource[] members = list.members();
        assertNotNull(members);
        assertEquals(1, members.length);

        IP4File[] openedFiles = connection.getOpenedBy(local.getLocation()
                .makeAbsolute().toOSString());
        assertNotNull(openedFiles);
        assertTrue(openedFiles.length > 0);
        assertNotNull(openedFiles[0].getUserName());
        assertNotNull(openedFiles[0].getClientName());
        assertTrue(openedFiles[0].getChangelistId() > 0);

        assertSame(ChangelistStatus.PENDING, list.getStatus());
        list.delete();
        assertNotNull(list.getChangelist());
        assertNotNull(list.getStatus());

        file.revert();
        list.delete();
        assertNull(list.getChangelist());
        assertNull(list.getStatus());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
