/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.LockAction;
import com.perforce.team.ui.p4java.actions.UnlockAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LockingTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IFile file = project.getFile("plugin.xml");
        IClient client = createConnection().getClient();
        addFile(client, file);
    }

    /**
     * Tests the locking and unlocking of a file
     */
    public void testLocking() {
        LockAction lock = new LockAction();
        lock.setAsync(false);
        Action wrapAction = Utils.getDisabledAction();
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);

        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        IP4File p4File = (IP4File) resource;

        StructuredSelection selection = new StructuredSelection(file);
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, selection);
        edit.run(null);

        p4File.refresh();

        assertTrue(p4File.isOpened());
        assertFalse(p4File.isLocked());

        lock.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());
        lock.run(wrapAction);

        p4File.refresh();

        assertTrue(p4File.isOpened());
        assertTrue(p4File.isLocked());

        UnlockAction unlock = new UnlockAction();
        unlock.setAsync(false);
        wrapAction = Utils.getDisabledAction();
        unlock.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());
        unlock.run(wrapAction);

        p4File.refresh();

        assertTrue(p4File.isOpened());
        assertFalse(p4File.isLocked());
    }

    /**
     * Test the locking action enablement
     */
    public void testLockingEnablement() {
        LockAction lock = new LockAction();
        lock.setAsync(false);
        Action wrapAction = Utils.getDisabledAction();
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());

        StructuredSelection selection = new StructuredSelection(file);

        lock.selectionChanged(wrapAction, selection);
        assertFalse(wrapAction.isEnabled());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, selection);
        edit.run(null);

        lock.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());
    }

    /**
     * Test the unlocking action enablement
     */
    public void testUnlockingEnablement() {
        UnlockAction unlock = new UnlockAction();
        unlock.setAsync(false);
        Action wrapAction = Utils.getDisabledAction();
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());

        StructuredSelection selection = new StructuredSelection(file);

        unlock.selectionChanged(wrapAction, selection);
        assertFalse(wrapAction.isEnabled());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, selection);
        edit.run(null);

        LockAction lock = new LockAction();
        lock.setAsync(false);
        lock.selectionChanged(null, selection);
        lock.run(null);

        unlock.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/lock";
    }

}
