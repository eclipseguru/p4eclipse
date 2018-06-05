/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.synchronize.CommitModelOperation;

import java.io.IOException;
import java.net.URL;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CommitOperationTest extends SynchronizeModelOperationTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project1";
    }

    /**
     * Tests the update operation
     */
    public void testOperation() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        IFile addFile = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
        assertFalse(addFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            addFile.create(fileUrl.openStream(), true, null);
            assertTrue(addFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(addFile));
        add.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(addFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        p4File.refresh();

        assertTrue(p4File.openedForAdd());
        assertFalse(p4File.isRemote());
        assertEquals(0, p4File.getHeadRevision());

        SynchronizeModelAction commit = new SynchronizeModelAction("Commit",
                setupConfiguration()) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.OUTGOING });
            }

            @Override
            protected void initialize(
                    final ISynchronizePageConfiguration configuration,
                    final ISelectionProvider selectionProvider) {

            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                CommitModelOperation operation = createOperation(configuration,
                        elements);
                return operation;
            }
        };

        try {
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { addFile }, 0, null);
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
        try {
            assertNotNull(PerforceSubscriber.getSubscriber().getSyncInfo(
                    addFile));
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }

        ISynchronizeModelElement element = setupModelElement(addFile);
        assertNotNull(element);
        commit.selectionChanged(new StructuredSelection(element));
        assertTrue(commit.isEnabled());
        commit.run();

        p4File.refresh();
        assertFalse(p4File.isOpened());
        assertEquals(1, p4File.getHeadRevision());
        assertNull(p4File.getAction());

        try {
            assertNull(PerforceSubscriber.getSubscriber().getSyncInfo(addFile));
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

    private CommitModelOperation createOperation(
            ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
        CommitModelOperation operation = new CommitModelOperation(
                configuration, elements);
        operation.setAsync(false);
        operation.setShowDialog(false);
        String description = "test commit operation";
        operation.setDescription(description);
        operation.setReopen(false);
        assertFalse(operation.isShowDialog());
        assertEquals(description, operation.getDescription());
        assertFalse(operation.isReopen());
        assertFalse(operation.isAsync());
        return operation;
    }
}
