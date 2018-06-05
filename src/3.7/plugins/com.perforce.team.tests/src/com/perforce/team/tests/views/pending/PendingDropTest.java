/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.pending;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.views.DragData;
import com.perforce.team.ui.views.PendingDragAdapter;
import com.perforce.team.ui.views.PendingDropAdapter;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingDropTest extends ProjectBasedTestCase {

    /**
     * Test invalid drops
     */
    public void testInvalidDrop() {
        PendingDropAdapter drop = new PendingDropAdapter(null, false);
        assertFalse(drop.validateDrop(null, 0, null));
        assertFalse(drop.doFileDrop(new String[] {}));
        assertFalse(drop.performDrop(null));
    }

    /**
     * Test dropping a job from one non-default changelist to another
     */
    public void testJobDrop() {
        IP4Changelist newList1 = null;
        IP4Changelist newList2 = null;
        IP4Job job = null;
        try {
            PendingDropAdapter drop = new PendingDropAdapter(null, false);

            IP4Connection connection = createConnection();

            IP4Job[] jobs = connection.getJobs(1);
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            job = jobs[0];
            assertNotNull(job);

            newList1 = createConnection()
                    .createChangelist("test1: " + getName(), new IP4File[0],
                            new IP4Job[] { job });
            newList2 = createConnection().createChangelist(
                    "test2: " + getName(), new IP4File[0], new IP4Job[0]);
            assertNotNull(newList1);

            assertTrue(Arrays.asList(newList1.getJobs()).contains(job));
            assertTrue(drop.validateDrop(newList2, 0, null));

            DragData.setConnection(connection);
            DragData.setSource(new PendingDragAdapter(null));

            assertTrue(drop.performDrop(new StructuredSelection(job)));

            assertTrue(Arrays.asList(newList2.getJobs()).contains(job));
            assertTrue(Arrays.asList(newList1.getJobs()).contains(job));
        } finally {
            if (newList1 != null) {
                if (job != null) {
                    new P4Collection(new IP4Resource[] { job }).unfix(newList1);
                }
                newList1.delete();
                assertNull(newList1.getChangelist());
            }
            if (newList2 != null) {
                if (job != null) {
                    new P4Collection(new IP4Resource[] { job }).unfix(newList2);
                }
                newList2.delete();
                assertNull(newList2.getChangelist());
            }
        }
    }

    /**
     * Tests a resource drop on the pending view
     */
    public void testResourceDrop() {
        IP4Changelist newList = null;
        IP4File file = null;
        try {
            PendingDropAdapter drop = new PendingDropAdapter(null, false);

            IFile localFile = project.getFile("plugin.xml");
            assertNotNull(localFile);
            assertTrue(localFile.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    localFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            file = (IP4File) resource;

            newList = file.getConnection().createChangelist(
                    "test: " + getName(), new IP4File[0]);
            assertNotNull(newList);
            assertTrue(drop.validateDrop(newList, 0, null));

            DragData.setConnection(resource.getConnection());
            DragData.setSource(new PendingDragAdapter(null));

            StructuredSelection selection = new StructuredSelection(localFile);
            assertTrue(drop.performDrop(selection));

            file.refresh();
            assertTrue(file.isOpened());
            assertNotNull(file.getChangelist());
            assertEquals(newList.getId(), file.getChangelist().getId());
        } finally {
            if (file != null) {
                file.revert();
                file.refresh();
            }
            if (newList != null) {
                newList.delete();
            }
        }
    }

    /**
     * Tests a resource adaptable drop on the pending view
     */
    public void testAdaptableDrop() {
        IP4Changelist newList = null;
        IP4File file = null;
        try {
            PendingDropAdapter drop = new PendingDropAdapter(null, false);

            final IFile localFile = project.getFile(new Path(
                    "META-INF/MANIFEST.MF"));
            assertNotNull(localFile);
            assertTrue(localFile.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    localFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            file = (IP4File) resource;

            newList = file.getConnection().createChangelist(
                    "test: " + getName(), new IP4File[0]);
            assertNotNull(newList);
            assertTrue(drop.validateDrop(newList, 0, null));

            DragData.setConnection(resource.getConnection());
            DragData.setSource(new PendingDragAdapter(null));

            IAdaptable adaptable = new IAdaptable() {

                public Object getAdapter(Class adapter) {
                    if (IResource.class.equals(adapter)) {
                        return localFile;
                    }
                    return null;
                }

            };

            StructuredSelection selection = new StructuredSelection(adaptable);
            assertTrue(drop.performDrop(selection));

            file.refresh();
            assertTrue(file.isOpened());
            assertNotNull(file.getChangelist());
            assertEquals(newList.getId(), file.getChangelist().getId());
        } finally {
            if (file != null) {
                file.revert();
                file.refresh();
            }
            if (newList != null) {
                newList.delete();
            }
        }
    }

    /**
     * Tests a string path drop on the pending view
     */
    public void testPathDrop() {
        IP4Changelist newList = null;
        IP4File file = null;
        try {
            PendingDropAdapter drop = new PendingDropAdapter(null, false);

            IFile localFile = project.getFile("plugin.xml");
            assertNotNull(localFile);
            assertTrue(localFile.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    localFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            file = (IP4File) resource;

            newList = file.getConnection().createChangelist(
                    "test: " + getName(), new IP4File[0]);
            assertNotNull(newList);
            assertTrue(drop.validateDrop(newList, 0, null));

            DragData.setConnection(resource.getConnection());
            DragData.setSource(new PendingDragAdapter(null));

            assertTrue(drop.doFileDrop(new String[] { file.getLocalPath() }));

            file.refresh();
            assertTrue(file.isOpened());
            assertNotNull(file.getChangelist());
            assertEquals(newList.getId(), file.getChangelist().getId());
        } finally {
            if (file != null) {
                file.revert();
                file.refresh();
            }
            if (newList != null) {
                newList.delete();
            }
        }
    }

    /**
     *
     */
    public void testMoveLocalResourceNonDefaultToDefault() {
        IP4PendingChangelist newList = null;
        IP4File file = null;
        try {
            PendingDropAdapter drop = new PendingDropAdapter(null, false);

            IFile localFile = project.getFile("plugin.properties");
            assertNotNull(localFile);
            assertTrue(localFile.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    localFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            file = (IP4File) resource;

            IP4PendingChangelist defaultList = file.getConnection()
                    .getPendingChangelist(0);
            assertNotNull(defaultList);

            EditAction action = new EditAction();
            action.setAsync(false);
            action.selectionChanged(null, new StructuredSelection(localFile));
            action.run(null);

            assertTrue(Arrays.asList(defaultList.getFiles()).contains(file));
            assertTrue(file.openedForEdit());
            assertEquals(0, file.getChangelistId());

            newList = file.getConnection().createChangelist(
                    "test: " + getName(), new IP4File[0]);
            assertNotNull(newList);
            assertFalse(Arrays.asList(newList.getFiles()).contains(file));

            P4Collection collection = new P4Collection(
                    new IP4Resource[] { file });
            collection.reopen(newList);

            assertTrue(Arrays.asList(newList.getFiles()).contains(file));
            assertFalse(Arrays.asList(defaultList.getFiles()).contains(file));

            assertTrue(drop.validateDrop(defaultList, 0, null));

            DragData.setConnection(resource.getConnection());
            DragData.setSource(new PendingDragAdapter(null));

            assertTrue(drop.doFileDrop(new String[] { file.getLocalPath() }));

            assertTrue(Arrays.asList(defaultList.getFiles()).contains(file));

            assertTrue(file.isOpened());
            assertNotNull(file.getChangelist());
            assertEquals(0, file.getChangelist().getId());
            assertFalse(Arrays.asList(newList.getFiles()).contains(file));
        } finally {
            if (file != null) {
                file.revert();
                file.refresh();
            }
            if (newList != null) {
                newList.delete();
            }
        }
    }

    /**
     *
     */
    public void testMoveLocalResourceDefaultToNonDefault() {
        IP4Changelist newList = null;
        IP4File file = null;
        try {
            PendingDropAdapter drop = new PendingDropAdapter(null, false);

            IFile localFile = project.getFile("plugin.properties");
            assertNotNull(localFile);
            assertTrue(localFile.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    localFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            file = (IP4File) resource;

            IP4PendingChangelist defaultList = file.getConnection()
                    .getPendingChangelist(0);
            assertNotNull(defaultList);

            EditAction action = new EditAction();
            action.setAsync(false);
            action.selectionChanged(null, new StructuredSelection(localFile));
            action.run(null);

            assertTrue(Arrays.asList(defaultList.getFiles()).contains(file));
            assertTrue(file.openedForEdit());
            assertEquals(0, file.getChangelistId());

            newList = file.getConnection().createChangelist(
                    "test: " + getName(), new IP4File[0]);
            assertNotNull(newList);
            assertFalse(Arrays.asList(newList.getFiles()).contains(file));
            assertTrue(drop.validateDrop(newList, 0, null));

            DragData.setConnection(resource.getConnection());
            DragData.setSource(new PendingDragAdapter(null));

            assertTrue(drop.doFileDrop(new String[] { file.getLocalPath() }));

            assertFalse(Arrays.asList(defaultList.getFiles()).contains(file));

            assertTrue(file.isOpened());
            assertNotNull(file.getChangelist());
            assertEquals(newList.getId(), file.getChangelist().getId());
            assertTrue(Arrays.asList(newList.getFiles()).contains(file));
        } finally {
            if (file != null) {
                file.revert();
                file.refresh();
            }
            if (newList != null) {
                newList.delete();
            }
        }
    }

    /**
     *
     */
    public void testMoveRemoteResourceDefaultToNonDefault() {
        IP4Changelist newList = null;
        IP4File file = null;
        try {
            PendingDropAdapter drop = new PendingDropAdapter(null, false);

            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(project);

            IFileSpec spec = new FileSpec(
                    "//depot/r05.2/p4-eclipse/doc/p4eclipse.doc");
            file = connection.getFile(spec);
            assertNotNull(file);

            IP4PendingChangelist defaultList = file.getConnection()
                    .getPendingChangelist(0);
            assertNotNull(defaultList);

            EditAction action = new EditAction();
            action.setAsync(false);
            action.selectionChanged(null, new StructuredSelection(file));
            action.run(null);

            file = connection.getFile(spec);

            assertTrue(Arrays.asList(defaultList.getFiles()).contains(file));
            assertTrue(file.openedForEdit());
            assertEquals(0, file.getChangelistId());

            newList = file.getConnection().createChangelist(
                    "test: " + getName(), new IP4File[0]);
            assertNotNull(newList);
            assertFalse(Arrays.asList(newList.getFiles()).contains(file));
            assertTrue(drop.validateDrop(newList, 0, null));

            DragData.setConnection(connection);
            DragData.setSource(new PendingDragAdapter(null));

            assertTrue(drop.doFileDrop(new String[] { file.getRemotePath() }));

            assertFalse(Arrays.asList(defaultList.getFiles()).contains(file));

            assertTrue(file.isOpened());
            assertNotNull(file.getChangelist());
            assertEquals(newList.getId(), file.getChangelist().getId());
            assertTrue(Arrays.asList(newList.getFiles()).contains(file));
        } finally {
            if (file != null) {
                file.revert();
                file.refresh();
            }
            if (newList != null) {
                newList.delete();
            }
        }
    }

    /**
     *
     */
    public void testMoveRemoteResourceNonDefaultToDefault() {
        IP4PendingChangelist newList = null;
        IP4File file = null;
        try {
            PendingDropAdapter drop = new PendingDropAdapter(null, false);

            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(project);

            IFileSpec spec = new FileSpec(
                    "//depot/r05.2/p4-eclipse/supporting_documentation/P4ECLIPSE-Build.doc");
            file = connection.getFile(spec);
            assertNotNull(file);

            IP4PendingChangelist defaultList = file.getConnection()
                    .getPendingChangelist(0);
            assertNotNull(defaultList);

            EditAction action = new EditAction();
            action.setAsync(false);
            action.selectionChanged(null, new StructuredSelection(file));
            action.run(null);

            file = connection.getFile(spec);

            assertTrue(Arrays.asList(defaultList.getFiles()).contains(file));
            assertTrue(file.openedForEdit());
            assertEquals(0, file.getChangelistId());

            newList = file.getConnection().createChangelist(
                    "test: " + getName(), new IP4File[0]);
            assertNotNull(newList);
            assertFalse(Arrays.asList(newList.getFiles()).contains(file));

            P4Collection collection = new P4Collection(
                    new IP4Resource[] { file });
            collection.reopen(newList);

            assertTrue(Arrays.asList(newList.getFiles()).contains(file));
            assertFalse(Arrays.asList(defaultList.getFiles()).contains(file));

            assertTrue(drop.validateDrop(defaultList, 0, null));

            DragData.setConnection(connection);
            DragData.setSource(new PendingDragAdapter(null));

            assertTrue(drop.doFileDrop(new String[] { file.getRemotePath() }));

            assertTrue(Arrays.asList(defaultList.getFiles()).contains(file));

            assertTrue(file.isOpened());
            assertNotNull(file.getChangelist());
            assertEquals(0, file.getChangelist().getId());
            assertFalse(Arrays.asList(newList.getFiles()).contains(file));
        } finally {
            if (file != null) {
                file.revert();
                file.refresh();
            }
            if (newList != null) {
                newList.delete();
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
