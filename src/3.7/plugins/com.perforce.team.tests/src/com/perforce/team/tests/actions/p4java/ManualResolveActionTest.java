/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.dialogs.CompareDialog;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.ManualResolveAction;
import com.perforce.team.ui.p4java.actions.ScheduleResolveAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ManualResolveActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        for (int i = 0; i < 3; i++) {
            addFile(client, project.getFile("about.ini"));
        }
    }

    /**
     * Test the action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        ManualResolveAction manual = new ManualResolveAction();
        manual.selectionChanged(wrap, StructuredSelection.EMPTY);
        assertTrue(wrap.isEnabled());

        IFile file = project.getFile("about.ini");
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

        manual.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Tests the manual resolve action
     */
    public void testAction() {
        IFile file = project.getFile("about.ini");
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

        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            file.setContents(fileUrl.openStream(), IResource.FORCE, null);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        ScheduleResolveAction schedule = new ScheduleResolveAction();
        schedule.setAsync(false);
        schedule.selectionChanged(null, new StructuredSelection(file));
        schedule.run(null);

        p4File.refresh();
        assertTrue(p4File.isUnresolved());

        IP4Resource[] unresolved = new P4Collection(
                new IP4Resource[] { p4File }).getUnresolved();
        assertNotNull(unresolved);
        assertEquals(1, unresolved.length);
        assertTrue(unresolved[0] instanceof IP4File);

        p4File = (IP4File) unresolved[0];
        assertNotNull(p4File.getIntegrationSpec());

        ManualResolveAction resolve = new ManualResolveAction();
        resolve.setAsync(false);
        CompareDialog dialog = resolve.createManualResolveDialog(p4File, 0);
        assertNotNull(dialog);
        dialog.close();
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
