/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.shelve;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;

import java.io.FileInputStream;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UnshelveTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Test unshelving a file
     */
    public void testUnshelveFile() {
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
                "UnshelveTest.testUnshelveFile", new IP4File[] { p4File });
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

            IP4ShelveFile[] shelvedFile = p4File.getShelvedVersions();

            assertNotNull(shelvedFile);
            assertEquals(1, shelvedFile.length);

            assertEquals(changelist.getId(), shelvedFile[0].getId());
            assertEquals(p4File.getRemotePath(), shelvedFile[0].getRemotePath());

            p4File.revert();
            p4File.refresh();
            changelist.refresh();
            assertEquals(0, changelist.members().length);

            shelvedFile[0].unshelve(changelist.getId());

            changelist.refresh();
            assertEquals(1, changelist.members().length);

            IP4Resource pending = changelist.members()[0];
            assertEquals(shelvedFile[0].getRemotePath(),
                    pending.getRemotePath());
            try {
                String pendingContent = Utils.getContent(new FileInputStream(
                        pending.getLocalPath()));
                String shelvedContent = Utils.getContent(shelvedFile[0]
                        .getRemoteContents());
                assertEquals(shelvedContent, pendingContent);
            } catch (Exception e) {
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
     * Test unshelving a changelist
     */
    public void testUnshelveChangelist() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());
        IP4PendingChangelist changelist = connection
                .createChangelist("UnshelveTest.testUnshelveChangelist",
                        new IP4File[] { p4File });
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

            IP4ShelveFile[] shelvedFile = p4File.getShelvedVersions();

            assertNotNull(shelvedFile);
            assertEquals(1, shelvedFile.length);

            assertEquals(changelist.getId(), shelvedFile[0].getId());
            assertEquals(p4File.getRemotePath(), shelvedFile[0].getRemotePath());

            p4File.revert();
            p4File.refresh();
            changelist.refresh();
            assertEquals(0, changelist.members().length);

            changelist.getShelvedChanges().unshelve(
                    new IP4Resource[] { p4File }, changelist.getId());

            changelist.refresh();
            assertEquals(1, changelist.members().length);

            IP4Resource pending = changelist.members()[0];
            assertEquals(shelvedFile[0].getRemotePath(),
                    pending.getRemotePath());
            try {
                String pendingContent = Utils.getContent(new FileInputStream(
                        pending.getLocalPath()));
                String shelvedContent = Utils.getContent(shelvedFile[0]
                        .getRemoteContents());
                assertEquals(shelvedContent, pendingContent);
            } catch (Exception e) {
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
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

}
