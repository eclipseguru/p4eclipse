/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.synchronize.ReopenModelOperation;

import java.util.Arrays;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ReopenOperationTest extends SynchronizeModelOperationTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("Jamfile"));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/native/com.perforce.p4api.cnative";
    }

    /**
     * Tests the reopen operation
     */
    public void testOperation() {
        IP4PendingChangelist newList = null;
        IP4File p4File = null;
        try {
            IFile file = project.getFile("Jamfile");
            assertTrue(file.exists());

            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            p4File = (IP4File) resource;
            p4File.refresh();
            assertTrue(p4File.isSynced());
            assertFalse(p4File.isOpened());

            newList = p4File.getConnection().createChangelist(
                    "test reopen operation", null);
            assertNotNull(newList);
            assertTrue(newList.getId() > 0);

            EditAction edit = new EditAction();
            edit.setAsync(false);
            edit.selectionChanged(null, new StructuredSelection(file));
            edit.run(null);
            assertTrue(p4File.openedForEdit());
            assertEquals(0, p4File.getChangelistId());

            final IP4PendingChangelist moveList = newList;

            SynchronizeModelAction reopen = new SynchronizeModelAction(
                    "Reopen", setupConfiguration()) {

                @Override
                protected void initialize(
                        final ISynchronizePageConfiguration configuration,
                        final ISelectionProvider selectionProvider) {

                }

                @Override
                protected boolean updateSelection(IStructuredSelection selection) {
                    super.updateSelection(selection);
                    if (selection != null && selection.size() == 1) {
                        Object element = selection.getFirstElement();
                        if (element instanceof ISynchronizeModelElement) {
                            return ((ISynchronizeModelElement) element)
                                    .getKind() != 0;
                        }
                    }
                    return false;
                }

                @Override
                protected SynchronizeModelOperation getSubscriberOperation(
                        ISynchronizePageConfiguration configuration,
                        IDiffElement[] elements) {
                    ReopenModelOperation operation = new ReopenModelOperation(
                            configuration, elements);
                    operation.setAsync(false);
                    operation.setMoveToList(moveList);
                    return operation;
                }
            };

            ISynchronizeModelElement element = setupModelElement(file);
            assertNotNull(element);
            reopen.selectionChanged(new StructuredSelection(element));
            assertTrue(reopen.isEnabled());
            reopen.run();
            assertEquals(newList.getId(), p4File.getChangelistId());
            assertTrue(Arrays.asList(newList.members()).contains(p4File));
        } finally {
            if (p4File != null) {
                p4File.revert();
            }
            if (newList != null) {
                newList.delete();
            }
        }
    }
}
