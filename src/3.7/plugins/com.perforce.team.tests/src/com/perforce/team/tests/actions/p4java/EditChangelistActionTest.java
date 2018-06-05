/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditChangelistAction;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditChangelistActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createJob();
        createJob();
        super.addFile(project.getFile("about.ini"));
    }

    /**
     * Tests the enablement
     */
    public void testEnablement() {
        IP4Connection connection = createConnection();
        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        Action wrap = Utils.getDisabledAction();
        EditChangelistAction edit = new EditChangelistAction();
        edit.selectionChanged(wrap, new StructuredSelection(defaultList));
        assertFalse(wrap.isEnabled());

        IP4PendingChangelist newList = connection.createChangelist(
                "test edit changelist: " + getName(), null);
        try {
            assertNotNull(newList);
            assertTrue(newList.getId() > 0);
            edit.selectionChanged(wrap, new StructuredSelection(newList));
            assertTrue(wrap.isEnabled());
        } finally {
            if (newList != null) {
                newList.delete();
                assertNull(newList.getStatus());
                assertNull(newList.getChangelist());
            }
        }
    }

    /**
     * Test editing a changelist and removing a file
     */
    public void testFileRemoval() {
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        IP4File p4File = (IP4File) resource;

        assertFalse(p4File.isOpened());

        p4File.edit();
        p4File.refresh();

        assertTrue(p4File.isOpened());

        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        Action wrap = Utils.getDisabledAction();
        EditChangelistAction edit = new EditChangelistAction();
        edit.setAsync(false);
        IP4PendingChangelist newList = connection.createChangelist(
                "test edit changelist: " + getName(), new IP4File[] { p4File });
        try {
            assertNotNull(newList);
            assertTrue(newList.getId() > 0);
            newList.refresh();
            assertNotNull(newList.members());
            assertEquals(1, newList.members().length);
            assertEquals(p4File, newList.members()[0]);
            edit.selectionChanged(wrap, new StructuredSelection(newList));
            assertTrue(wrap.isEnabled());

            IP4PendingChangelist defaultList = connection
                    .getPendingChangelist(0);

            edit.edit(new IP4File[] { p4File }, null, null, "", newList,
                    defaultList);

            assertFalse(Arrays.asList(newList.members()).contains(p4File));
            assertTrue(Arrays.asList(defaultList.members()).contains(p4File));

            // Double check model after refresh
            newList.refresh();
            defaultList.refresh();
            assertFalse(Arrays.asList(newList.members()).contains(p4File));
            assertTrue(Arrays.asList(defaultList.members()).contains(p4File));
        } finally {
            if (p4File != null) {
                p4File.revert();
                p4File.refresh();
            }
            if (newList != null) {
                newList.delete();
                assertNull(newList.getStatus());
                assertNull(newList.getChangelist());
            }
            if (p4File != null) {
                assertFalse(p4File.isOpened());
            }
        }
    }

    /**
     * Test editing a changelist and removing a job
     */
    public void testJobRemoval() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        Action wrap = Utils.getDisabledAction();
        EditChangelistAction edit = new EditChangelistAction();
        edit.setAsync(false);
        IP4PendingChangelist newList = null;
        IP4Job job = null;
        try {
            job = null;
            IP4Job[] jobs = connection.getJobs(1);
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            job = jobs[0];
            assertNotNull(job);
            String description = "test edit changelist: " + getName();
            newList = connection.createChangelist(description, null,
                    new IP4Job[] { job });
            assertNotNull(newList);
            assertTrue(newList.getId() > 0);
            newList.refresh();
            assertNotNull(newList.members());
            assertEquals(1, newList.members().length);
            assertNotNull(newList.getJobs());
            assertEquals(1, newList.getJobs().length);
            edit.selectionChanged(wrap, new StructuredSelection(newList));
            assertTrue(wrap.isEnabled());

            IP4PendingChangelist defaultList = connection
                    .getPendingChangelist(0);

            assertTrue(Arrays.asList(newList.members()).contains(job));

            edit.edit(null, new IP4Job[] { job }, null, description, newList,
                    defaultList);
            assertFalse(Arrays.asList(newList.members()).contains(job));
            assertNotNull(newList.getJobs());
            assertEquals(0, newList.getJobs().length);

            // Double check model after refresh
            newList.refresh();
            assertFalse(Arrays.asList(newList.members()).contains(job));
            assertNotNull(newList.getJobs());
            assertEquals(0, newList.getJobs().length);
        } finally {
            if (job != null) {
                newList.refresh();
                new P4Collection(new IP4Resource[] { job }).unfix(newList);
                assertNotNull(newList.getJobs());
                assertEquals(0, newList.getJobs().length);
            }
            if (newList != null) {
                newList.delete();
                assertNull(newList.getStatus());
                assertNull(newList.getChangelist());
            }
        }
    }

    /**
     * Test editing a changliest and adding a job
     */
    public void testJobAddition() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        Action wrap = Utils.getDisabledAction();
        EditChangelistAction edit = new EditChangelistAction();
        edit.setAsync(false);
        IP4PendingChangelist newList = connection.createChangelist(
                "test edit changelist: " + getName(), null);
        IP4Job job = null;
        try {
            IP4Job[] jobs = connection.getJobs(1);
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            job = jobs[0];
            assertNotNull(newList);
            assertTrue(newList.getId() > 0);
            newList.refresh();
            assertNotNull(newList.members());
            assertEquals(0, newList.members().length);
            edit.selectionChanged(wrap, new StructuredSelection(newList));
            assertTrue(wrap.isEnabled());

            IP4PendingChangelist defaultList = connection
                    .getPendingChangelist(0);

            edit.edit(null, null, new IP4Job[] { job }, null, newList,
                    defaultList);

            assertTrue(Arrays.asList(newList.members()).contains(job));
            assertNotNull(newList.getJobs());
            assertEquals(1, newList.getJobs().length);

            // Double check model after refresh
            newList.refresh();
            assertTrue(Arrays.asList(newList.members()).contains(job));
            assertNotNull(newList.getJobs());
            assertEquals(1, newList.getJobs().length);
        } finally {
            if (job != null) {
                new P4Collection(new IP4Resource[] { job }).unfix(newList);
                assertNotNull(newList.getJobs());
                assertEquals(0, newList.getJobs().length);
            }
            if (newList != null) {
                newList.delete();
                assertNull(newList.getStatus());
                assertNull(newList.getChangelist());
            }
        }
    }

    /**
     * Test edit a changelist and changing the description
     */
    public void testDescriptionChange() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        Action wrap = Utils.getDisabledAction();
        EditChangelistAction edit = new EditChangelistAction();
        edit.setAsync(false);
        IP4PendingChangelist newList = connection.createChangelist(
                "test edit changelist: " + getName(), null);
        try {
            assertNotNull(newList);
            assertTrue(newList.getId() > 0);
            newList.refresh();
            assertNotNull(newList.members());
            assertEquals(0, newList.members().length);
            edit.selectionChanged(wrap, new StructuredSelection(newList));
            assertTrue(wrap.isEnabled());

            IP4PendingChangelist defaultList = connection
                    .getPendingChangelist(0);

            String updated = "updated description: " + getName();

            edit.edit(null, null, null, updated, newList, defaultList);

            assertEquals(updated, newList.getDescription());
            assertNotNull(newList.members());
            assertEquals(0, newList.members().length);
            assertNotNull(newList.getFiles());
            assertEquals(0, newList.getFiles().length);
            assertNotNull(newList.getJobs());
            assertEquals(0, newList.getJobs().length);

            // Double check model after refresh
            newList.refresh();
            assertEquals(updated, newList.getDescription().trim());
            assertNotNull(newList.members());
            assertEquals(0, newList.members().length);
            assertNotNull(newList.getFiles());
            assertEquals(0, newList.getFiles().length);
            assertNotNull(newList.getJobs());
            assertEquals(0, newList.getJobs().length);
        } finally {
            if (newList != null) {
                newList.delete();
                assertNull(newList.getStatus());
                assertNull(newList.getChangelist());
            }
        }
    }

    /**
     * Test editing a changelist's description, removing a file, adding a job,
     * and removing a different job
     */
    public void testAll() {
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        IP4File p4File = (IP4File) resource;

        assertFalse(p4File.isOpened());

        p4File.edit();
        p4File.refresh();

        assertTrue(p4File.isOpened());
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        Action wrap = Utils.getDisabledAction();
        EditChangelistAction edit = new EditChangelistAction();
        edit.setAsync(false);
        IP4PendingChangelist newList = null;
        IP4Job addedJob = null;
        IP4Job removedJob = null;

        try {
            // Get 2 jobs, one to add and one to remove
            IP4Job[] jobs = connection.getJobs(2);
            assertNotNull(jobs);
            assertEquals(2, jobs.length);
            addedJob = jobs[0];
            assertNotNull(addedJob);
            removedJob = jobs[1];
            assertNotNull(removedJob);

            // Create changelist
            newList = connection.createChangelist("test edit changelist: "
                    + getName(), new IP4File[] { p4File },
                    new IP4Job[] { removedJob });
            assertNotNull(newList);
            assertTrue(newList.getId() > 0);
            newList.refresh();

            // Check for initial file and job
            assertNotNull(newList.members());
            assertEquals(2, newList.members().length);
            assertNotNull(newList.getFiles());
            assertEquals(1, newList.getFiles().length);
            assertEquals(p4File, newList.getFiles()[0]);
            assertNotNull(newList.getJobs());
            assertEquals(1, newList.getJobs().length);
            assertEquals(removedJob, newList.getJobs()[0]);

            edit.selectionChanged(wrap, new StructuredSelection(newList));
            assertTrue(wrap.isEnabled());

            IP4PendingChangelist defaultList = connection
                    .getPendingChangelist(0);

            String updated = "updated description and add: " + getName();

            edit.edit(new IP4File[] { p4File }, new IP4Job[] { removedJob },
                    new IP4Job[] { addedJob }, updated, newList, defaultList);

            // Check updated description
            assertEquals(updated, newList.getDescription());

            // Check removed file
            assertFalse(Arrays.asList(newList.members()).contains(p4File));
            assertNotNull(newList.getFiles());
            assertEquals(0, newList.getFiles().length);
            assertTrue(Arrays.asList(defaultList.members()).contains(p4File));

            // Check added and removed job
            assertTrue(Arrays.asList(newList.members()).contains(addedJob));
            assertFalse(Arrays.asList(newList.members()).contains(removedJob));
            assertNotNull(newList.getJobs());
            assertEquals(1, newList.getJobs().length);
            assertEquals(addedJob, newList.getJobs()[0]);

            // Double check model after refresh
            newList.refresh();

            // Check updated description
            assertEquals(updated, newList.getDescription().trim());

            // Check removed file
            assertFalse(Arrays.asList(newList.members()).contains(p4File));
            assertNotNull(newList.getFiles());
            assertEquals(0, newList.getFiles().length);
            assertTrue(Arrays.asList(defaultList.members()).contains(p4File));

            // Check added and removed job
            assertTrue(Arrays.asList(newList.members()).contains(addedJob));
            assertFalse(Arrays.asList(newList.members()).contains(removedJob));
            assertNotNull(newList.getJobs());
            assertEquals(1, newList.getJobs().length);
            assertEquals(addedJob, newList.getJobs()[0]);
        } finally {
            if (p4File != null) {
                p4File.revert();
                p4File.refresh();
            }
            if (newList != null) {
                if (addedJob != null) {
                    newList.refresh();
                    new P4Collection(new IP4Resource[] { addedJob })
                            .unfix(newList);
                    assertNotNull(newList.getJobs());
                    assertEquals(0, newList.getJobs().length);
                }
                newList.delete();
                assertNull(newList.getStatus());
                assertNull(newList.getChangelist());
            }
            if (p4File != null) {
                assertFalse(p4File.isOpened());
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
