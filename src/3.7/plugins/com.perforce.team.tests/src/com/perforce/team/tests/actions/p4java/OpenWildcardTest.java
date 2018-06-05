/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.DeleteAction;
import com.perforce.team.ui.p4java.actions.EditAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class OpenWildcardTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project1";
    }

    /**
     * Test adding a file with a '#' in the filename
     */
    public void testAddHash() {
        IFile hash = project.getFile("test#.txt");
        assertFalse(hash.exists());
        try {
            Utils.fillFile(hash);
            assertTrue(hash.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(hash);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(hash));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(hash);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());
    }

    /**
     * Test editing a file with a '#' in the filename
     */
    public void testEditHash() {
        IFile hash = project.getFile("test" + System.currentTimeMillis()
                + "#.txt");
        assertFalse(hash.exists());
        try {
            Utils.fillFile(hash);
            assertTrue(hash.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(hash);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(hash));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(hash);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());

        IP4PendingChangelist defaultList = P4Workspace.getWorkspace()
                .getConnection(project).getPendingChangelist(0);
        assertNotNull(defaultList);
        int id = defaultList.submit("unit test submit", new IP4File[] { file }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(file.openedForAdd());
        assertEquals(1, file.getHeadRevision());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(hash));
        edit.run(null);

        assertTrue(file.openedForEdit());
        assertNotNull(file.getP4JFile());
    }

    /**
     * Test deleting a file with a '#' in the filename
     */
    public void testDeleteHash() {
        IFile hash = project.getFile("test" + System.currentTimeMillis()
                + "#.txt");
        assertFalse(hash.exists());
        try {
            Utils.fillFile(hash);
            assertTrue(hash.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(hash);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(hash));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(hash);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());

        IP4PendingChangelist defaultList = P4Workspace.getWorkspace()
                .getConnection(project).getPendingChangelist(0);
        assertNotNull(defaultList);
        int id = defaultList.submit("unit test submit", new IP4File[] { file }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(file.openedForAdd());
        assertEquals(1, file.getHeadRevision());

        DeleteAction delete = new DeleteAction();
        delete.setAsync(false);
        delete.selectionChanged(null, new StructuredSelection(hash));
        delete.run(null);

        assertTrue(file.openedForDelete());
        assertNotNull(file.getP4JFile());
    }

    /**
     * Test adding a file with a '@' in the filename
     */
    public void testAddAt() {
        IFile at = project.getFile("test" + System.currentTimeMillis()
                + "@.txt");
        assertFalse(at.exists());
        try {
            Utils.fillFile(at);
            assertTrue(at.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(at);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(at));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(at);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());
    }

    /**
     * Test editing a file with a '@' in the filename
     */
    public void testEditAt() {
        IFile at = project.getFile("test" + System.currentTimeMillis()
                + "@.txt");
        assertFalse(at.exists());
        try {
            Utils.fillFile(at);
            assertTrue(at.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(at);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(at));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(at);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());

        IP4PendingChangelist defaultList = P4Workspace.getWorkspace()
                .getConnection(project).getPendingChangelist(0);
        assertNotNull(defaultList);
        int id = defaultList.submit("unit test submit", new IP4File[] { file }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(file.openedForAdd());
        assertEquals(1, file.getHeadRevision());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(at));
        edit.run(null);

        assertTrue(file.openedForEdit());
        assertNotNull(file.getP4JFile());
    }

    /**
     * Test deleting a file with a '@' in the filename
     */
    public void testDeleteAt() {
        IFile at = project.getFile("test" + System.currentTimeMillis()
                + "@.txt");
        assertFalse(at.exists());
        try {
            Utils.fillFile(at);
            assertTrue(at.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(at);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(at));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(at);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());

        IP4PendingChangelist defaultList = P4Workspace.getWorkspace()
                .getConnection(project).getPendingChangelist(0);
        assertNotNull(defaultList);
        int id = defaultList.submit("unit test submit", new IP4File[] { file }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(file.openedForAdd());
        assertEquals(1, file.getHeadRevision());

        DeleteAction delete = new DeleteAction();
        delete.setAsync(false);
        delete.selectionChanged(null, new StructuredSelection(at));
        delete.run(null);

        assertTrue(file.openedForDelete());
        assertNotNull(file.getP4JFile());
    }

    /**
     * Test adding a file with a '*' in the filename
     */
    public void testAddStar() {
        IFile star = project.getFile("te*st" + System.currentTimeMillis()
                + ".txt");
        assertFalse(star.exists());
        try {
            Utils.fillFile(star);
            assertTrue(star.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(star);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(star));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(star);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());
    }

    /**
     * Test editing a file with a '*' in the filename
     */
    public void testEditStar() {
        IFile star = project.getFile("te*st" + System.currentTimeMillis()
                + ".txt");
        assertFalse(star.exists());
        try {
            Utils.fillFile(star);
            assertTrue(star.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(star);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(star));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(star);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());

        IP4PendingChangelist defaultList = P4Workspace.getWorkspace()
                .getConnection(project).getPendingChangelist(0);
        assertNotNull(defaultList);
        int id = defaultList.submit("unit test submit", new IP4File[] { file }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(file.openedForAdd());
        assertEquals(1, file.getHeadRevision());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(star));
        edit.run(null);

        assertTrue(file.openedForEdit());
        assertNotNull(file.getP4JFile());
    }

    /**
     * Test deleting a file with a '*' in the filename
     */
    public void testDeleteStar() {
        IFile star = project.getFile("te*st" + System.currentTimeMillis()
                + ".txt");
        assertFalse(star.exists());
        try {
            Utils.fillFile(star);
            assertTrue(star.exists());
        } catch (Exception e) {
            assertFalse("Exception thrown creating file: " + e.getMessage(),
                    true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(star);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.openedForAdd());
        assertNull(file.getP4JFile());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(star));
        add.run(null);

        resource = P4Workspace.getWorkspace().getResource(star);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        file = (IP4File) resource;
        assertTrue(file.openedForAdd());
        assertNotNull(file.getP4JFile());

        IP4PendingChangelist defaultList = P4Workspace.getWorkspace()
                .getConnection(project).getPendingChangelist(0);
        assertNotNull(defaultList);
        int id = defaultList.submit("unit test submit", new IP4File[] { file }, new NullProgressMonitor());
        assertTrue(id > 0);

        assertFalse(file.openedForAdd());
        assertEquals(1, file.getHeadRevision());

        DeleteAction delete = new DeleteAction();
        delete.setAsync(false);
        delete.selectionChanged(null, new StructuredSelection(star));
        delete.run(null);

        assertTrue(file.openedForDelete());
        assertNotNull(file.getP4JFile());
    }

}
