/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.RemoveAction;
import com.perforce.team.ui.p4java.actions.RevertAction;
import com.perforce.team.ui.p4java.actions.SyncAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RemoveActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

    /**
     * Test remove action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        RemoveAction action = new RemoveAction();
        action.setAsync(false);
        RemoveAction.setNeedConfirm(false);
        IFile newFile = project.getFile("testtest.txt");
        action.selectionChanged(wrap, new StructuredSelection(newFile));
        assertFalse(wrap.isEnabled());
        action.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        p4File.refresh();
        assertTrue(p4File.getHaveRevision() > 0);
        action.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        assertTrue(p4File.isOpened());
        action.selectionChanged(wrap, new StructuredSelection(file));
        assertFalse(wrap.isEnabled());
        RevertAction revert = new RevertAction();
        revert.setAsync(false);
        revert.selectionChanged(wrap, new StructuredSelection(file));
        revert.runAction(false);
        action.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Tests the remove action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        RemoveAction action = new RemoveAction();
        action.setAsync(false);
        RemoveAction.setNeedConfirm(false);
        action.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());

        IP4Resource root = P4Workspace.getWorkspace().getResource(project);
        assertNotNull(root);
        assertTrue(root instanceof IP4Folder);
        IP4Folder folder = (IP4Folder) root;
        IP4File[] files = folder.getAllLocalFiles();
        assertNotNull(files);
        assertTrue(files.length > 0);
        for (IP4File file : files) {
            assertNotNull(file.getP4JFile());
            assertTrue(file.getHaveRevision() > 0);
        }

        try {
            action.run(wrap);

            IP4File[] newFiles = folder.getAllLocalFiles();
            assertNotNull(newFiles);
            assertTrue(newFiles.length > 0);
            assertSame(files.length, newFiles.length);
            for (IP4File file : newFiles) {
                assertNotNull(file.getP4JFile());
                assertEquals(file.getName() + " have revision was not 0", 0,
                        file.getHaveRevision());
            }
        } finally {
            SyncAction sync = new SyncAction();
            sync.selectionChanged(null, new StructuredSelection(project));
            sync.setAsync(false);
            sync.run(null);
        }
    }
}
