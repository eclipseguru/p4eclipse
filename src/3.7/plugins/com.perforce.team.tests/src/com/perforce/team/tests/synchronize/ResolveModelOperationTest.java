/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.ScheduleResolveAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.synchronize.ResolveModelOperation;

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
public class ResolveModelOperationTest extends SynchronizeModelOperationTest {

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

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);

        p4File.refresh();
        assertTrue(p4File.openedForEdit());

        ScheduleResolveAction schedule = new ScheduleResolveAction();
        schedule.setAsync(false);
        schedule.selectionChanged(null, new StructuredSelection(file));
        schedule.run(null);

        p4File.refresh();
        assertTrue(p4File.isUnresolved());

        SynchronizeModelAction resolve = new SynchronizeModelAction("Update",
                setupConfiguration()) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.CONFLICTING });
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
                ResolveModelOperation operation = createOperation(
                        configuration, elements);
                return operation;
            }
        };

        ISynchronizeModelElement element = setupModelElement(file);
        assertNotNull(element);
        resolve.selectionChanged(new StructuredSelection(element));
        assertTrue(resolve.isEnabled());
        resolve.run();

        p4File.refresh();
        assertFalse(p4File.isUnresolved());
        assertTrue(p4File.openedForEdit());
    }

    private ResolveModelOperation createOperation(
            ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
        ResolveModelOperation operation = new ResolveModelOperation(
                configuration, elements);
        operation.setAsync(false);
        operation.setShowDialog(false);
        ResolveFilesAutoOptions options = new ResolveFilesAutoOptions().setAcceptYours(true); 
        operation.setOptions(options);
        assertFalse(operation.isShowDialog());
        assertSame(options, operation.getOptions());
        assertFalse(operation.isAsync());
        return operation;
    }

}
