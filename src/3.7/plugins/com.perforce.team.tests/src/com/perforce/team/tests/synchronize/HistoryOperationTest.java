/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.history.P4HistoryPage;
import com.perforce.team.ui.history.P4HistoryPageSource;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.synchronize.HistoryModelOperation;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class HistoryOperationTest extends SynchronizeModelOperationTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        for (int i = 0; i < 5; i++) {
            addFile(client, project.getFile("Jamfile"));
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/native/com.perforce.p4api.cnative";
    }

    /**
     * Tests the update operation
     */
    public void testOperation() {
        IFile file = project.getFile("Jamfile");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        p4File.refresh();
        assertTrue(p4File.isSynced());

        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.runAction("#2");

        p4File.refresh();
        assertFalse(p4File.isSynced());
        assertEquals(2, p4File.getHaveRevision());

        SynchronizeModelAction history = new SynchronizeModelAction("History",
                setupConfiguration()) {

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
                        return ((ISynchronizeModelElement) element).getKind() != 0;
                    }
                }
                return false;
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                HistoryModelOperation operation = new HistoryModelOperation(
                        configuration, elements);
                operation.setAsync(false);
                return operation;
            }
        };

        ISynchronizeModelElement element = setupModelElement(file);
        assertNotNull(element);
        history.selectionChanged(new StructuredSelection(element));
        assertTrue(history.isEnabled());
        history.run();
        // Sleep here since history.run() spawns an async ui job.
        Utils.sleep(.1);

        IHistoryPage page = TeamUI.getHistoryView().getHistoryPage();
        assertTrue(page instanceof P4HistoryPage);

        P4HistoryPage p4Page = (P4HistoryPage) page;

        while (p4Page.isLoading()) {
            Utils.sleep(.1);
        }
        assertNotNull(p4Page.getInput());
        assertTrue(p4Page.getInput() instanceof P4HistoryPageSource);
        assertEquals(p4File,
                ((P4HistoryPageSource) p4Page.getInput()).getFile());
        assertNotNull(p4Page.getViewer());
        assertTrue(((TreeViewer) p4Page.getViewer()).getTree().getItemCount() >= 3);
    }

}
