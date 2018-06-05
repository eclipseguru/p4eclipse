/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ProjectBasedTestCase;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4PendingChangelistTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Test reopen
     */
    public void testReopen() {
        IP4Connection connection = createConnection();
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = connection.getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.openedForEdit());
        assertEquals(IChangelist.DEFAULT, p4File.getChangelistId());
        IP4PendingChangelist created = connection.createChangelist(getClass()
                .getName(), new IP4File[0]);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        try {
            created.reopen(new IP4Resource[] { p4File });
            p4File.refresh();
            assertEquals(created.getId(), p4File.getChangelistId());
        } finally {
            if (created != null) {
                created.revert();
                created.delete();
                assertNull(created.getChangelist());
            }
        }
    }

    /**
     * Test revert
     */
    public void testRevert() {
        IP4Connection connection = createConnection();
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4PendingChangelist created = connection.createChangelist(getClass()
                .getName(), new IP4File[0]);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        IP4Resource resource = connection.getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        p4File.edit(created.getId());
        p4File.refresh();
        assertTrue(p4File.openedForEdit());
        assertEquals(created.getId(), p4File.getChangelistId());
        try {
            created.revert();
            p4File.refresh();
            assertFalse(p4File.isOpened());
        } finally {
            if (created != null) {
                created.revert();
                created.delete();
                assertNull(created.getChangelist());
            }
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
