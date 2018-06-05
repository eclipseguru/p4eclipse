/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.shelve;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.tests.ProjectBasedTestCase;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelveTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
        addFile(project.getFile("plugin.properties"));
    }

    /**
     * Test shelving from a numbered changelist
     */
    public void testNew() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());
        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveTest.testNew", new IP4File[] { p4File });
        try {
            assertNotNull(changelist);
            assertTrue(changelist.getId() > 0);
            assertFalse(changelist.isShelved());
            assertTrue(Arrays.asList(changelist.members()).contains(p4File));
            assertNotNull(changelist.getShelvedChanges());
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(0, p4File.getShelvedVersions().length);
            assertEquals(0, changelist.getShelvedChanges().members().length);
            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());
            assertEquals(1, changelist.getShelvedChanges().members().length);
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(1, p4File.getShelvedVersions().length);
        } finally {
            if (changelist != null) {
                changelist.revert();
                changelist.deleteShelved();
                changelist.delete();
            }
        }
    }

    /**
     * Test {@link IP4ShelveFile} interface and implementing class.
     */
    public void testShelveFile() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());
        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveTest.testShelveFile", new IP4File[] { p4File });
        try {
            assertNotNull(changelist);
            assertTrue(changelist.getId() > 0);
            assertFalse(changelist.isShelved());
            assertTrue(Arrays.asList(changelist.members()).contains(p4File));
            assertNotNull(changelist.getShelvedChanges());
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(0, p4File.getShelvedVersions().length);
            assertEquals(0, changelist.getShelvedChanges().members().length);
            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());
            assertEquals(1, changelist.getShelvedChanges().members().length);

            IP4ShelveFile[] shelvedVersions = p4File.getShelvedVersions();
            assertNotNull(shelvedVersions);
            assertEquals(1, shelvedVersions.length);
            IP4ShelveFile shelvedFile = shelvedVersions[0];
            assertFalse(shelvedFile.isContainer());
            assertNotNull(shelvedFile.getName());
            assertNotNull(shelvedFile.getParent());
            assertNotNull(shelvedFile.getClientPath());
            assertNotNull(shelvedFile.getClient());
            assertNotNull(shelvedFile.getRevision());
            assertNotNull(shelvedFile.getChangelist());
            assertNotNull(shelvedFile.getDate());
            assertNotNull(shelvedFile.getUser());
            assertNotNull(shelvedFile.getWorkspace());
            assertNotNull(shelvedFile.getDescription());
            assertNotNull(shelvedFile.getFile());
            assertNotNull(shelvedFile.toString());
            assertNotNull(shelvedFile.getActionPath());
            assertNotNull(shelvedFile.getAdapter(IP4File.class));
            try {
                shelvedFile.refresh();
                shelvedFile.refresh(0);
            } catch (Throwable e) {
                handle(e);
            }
        } finally {
            if (changelist != null) {
                changelist.revert();
                changelist.deleteShelved();
                changelist.delete();
            }
        }
    }

    /**
     * Test {@link IP4ShelvedChangelist} interface and implementing class.
     */
    public void testShelveChanges() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());
        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveTest.testShelveFile", new IP4File[] { p4File });
        try {
            assertNotNull(changelist);
            assertTrue(changelist.getId() > 0);
            assertFalse(changelist.isShelved());
            assertTrue(Arrays.asList(changelist.members()).contains(p4File));
            assertNotNull(changelist.getShelvedChanges());
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(0, p4File.getShelvedVersions().length);
            assertEquals(0, changelist.getShelvedChanges().members().length);

            IP4ShelvedChangelist[] shelved = connection
                    .getShelvedChangelists(1);
            assertNotNull(shelved);
            assertEquals(0, shelved.length);

            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());

            shelved = connection.getShelvedChangelists(1);
            assertNotNull(shelved);
            assertEquals(1, shelved.length);

            IP4ShelvedChangelist list = shelved[0];
            assertTrue(list.getId() > 0);
            assertEquals(changelist.getId(), list.getId());
        } finally {
            if (changelist != null) {
                changelist.revert();
                changelist.deleteShelved();
                changelist.delete();
            }
        }
    }

    /**
     * Test shelving a file twice
     */
    public void testUpdate() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());
        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveTest.testUpdate", new IP4File[] { p4File });
        try {
            assertNotNull(changelist);
            assertTrue(changelist.getId() > 0);
            assertFalse(changelist.isShelved());
            assertTrue(Arrays.asList(changelist.members()).contains(p4File));
            assertNotNull(changelist.getShelvedChanges());
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(0, p4File.getShelvedVersions().length);
            assertEquals(0, changelist.getShelvedChanges().members().length);
            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());
            assertEquals(1, changelist.getShelvedChanges().members().length);
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(1, p4File.getShelvedVersions().length);
            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());
            assertEquals(1, changelist.getShelvedChanges().members().length);
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(1, p4File.getShelvedVersions().length);
        } finally {
            if (changelist != null) {
                changelist.revert();
                changelist.deleteShelved();
                changelist.delete();
            }
        }
    }

    /**
     * Test replacing a shelve changelist
     */
    public void testReplace() {
        IFile file = project.getFile("plugin.xml");
        IFile file2 = project.getFile("plugin.properties");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());
        IP4Resource resource2 = connection.getResource(file2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;
        assertFalse(p4File2.isOpened());
        p4File2.edit();
        p4File2.refresh();
        assertTrue(p4File2.isOpened());
        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveTest.testUpdate", new IP4File[] { p4File });
        try {
            assertNotNull(changelist);
            assertTrue(changelist.getId() > 0);
            assertFalse(changelist.isShelved());
            assertTrue(Arrays.asList(changelist.members()).contains(p4File));
            assertNotNull(changelist.getShelvedChanges());
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(0, p4File.getShelvedVersions().length);
            assertEquals(0, changelist.getShelvedChanges().members().length);
            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());
            assertEquals(1, changelist.getShelvedChanges().members().length);
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(1, p4File.getShelvedVersions().length);
            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());
            assertEquals(1, changelist.getShelvedChanges().members().length);
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(1, p4File.getShelvedVersions().length);
            changelist.reopen(new IP4Resource[] { p4File2 });
            changelist.refresh();
            assertEquals(2, changelist.members().length);
            p4File.revert();
            p4File.refresh();
            assertFalse(p4File.isOpened());
            changelist.refresh();
            assertEquals(1, changelist.members().length);
            changelist.replaceShelvedFiles();
            assertEquals(1, changelist.getShelvedChanges().members().length);
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(0, p4File.getShelvedVersions().length);
            assertNotNull(p4File2.getShelvedVersions());
            assertEquals(1, p4File2.getShelvedVersions().length);
        } finally {
            if (changelist != null) {
                changelist.revert();
                changelist.deleteShelved();
                changelist.delete();
            }
        }
    }

    /**
     * Test deleting all the files from a shelved changelist
     */
    public void testDelete() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());
        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveTest.testNew", new IP4File[] { p4File });
        try {
            assertNotNull(changelist);
            assertTrue(changelist.getId() > 0);
            assertFalse(changelist.isShelved());
            assertTrue(Arrays.asList(changelist.members()).contains(p4File));
            assertNotNull(changelist.getShelvedChanges());
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(0, p4File.getShelvedVersions().length);
            assertEquals(0, changelist.getShelvedChanges().members().length);
            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());
            assertEquals(1, changelist.getShelvedChanges().members().length);
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(1, p4File.getShelvedVersions().length);
            changelist.deleteShelve(new IP4Resource[] { p4File });
            assertFalse(changelist.isShelved());
            assertEquals(0, changelist.getShelvedChanges().members().length);
            assertNotNull(p4File.getShelvedVersions());
            assertEquals(0, p4File.getShelvedVersions().length);

        } finally {
            if (changelist != null) {
                changelist.revert();
                changelist.deleteShelved();
                changelist.delete();
            }
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

}
