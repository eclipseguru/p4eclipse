/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.synchronize.RevertUnchangedModelOperation;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
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
public class RevertUnchangedOperationTest extends SynchronizeModelOperationTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/native/com.perforce.p4api.cnative";
    }

    /**
     * Tests the revert operation
     */
    public void testOperation() {
        IFile file = project.getFile("version.sh");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        p4File.refresh();
        assertTrue(p4File.isSynced());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);

        p4File.refresh();
        assertTrue(p4File.openedForEdit());

        SynchronizeModelAction revert = new SynchronizeModelAction("Revert",
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
                RevertUnchangedModelOperation operation = createOperation(
                        configuration, elements);
                return operation;
            }
        };

        ISynchronizeModelElement element = setupModelElement(file);
        assertNotNull(element);
        revert.selectionChanged(new StructuredSelection(element));
        assertTrue(revert.isEnabled());
        revert.run();

        p4File.refresh();
        assertFalse(p4File.isOpened());
    }

    private RevertUnchangedModelOperation createOperation(
            ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
        RevertUnchangedModelOperation operation = new RevertUnchangedModelOperation(
                configuration, elements);
        operation.setAsync(false);
        operation.setShowDialog(false);
        assertFalse(operation.isAsync());
        assertFalse(operation.isShowDialog());
        return operation;
    }

}
