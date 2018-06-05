/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.MoveToAnotherChangelistAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MoveToAnotherChangelistActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Test the action enablement
     */
    public void testEnablement() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        IP4File p4File = (IP4File) resource;
        p4File.refresh();
        try {
            assertFalse(p4File.isOpened());

            Action wrap = Utils.getDisabledAction();
            MoveToAnotherChangelistAction move = new MoveToAnotherChangelistAction();
            move.selectionChanged(wrap, new StructuredSelection(file));
            assertFalse(wrap.isEnabled());

            p4File.edit();
            p4File.refresh();
            assertTrue(p4File.isOpened());

            move.selectionChanged(wrap, new StructuredSelection(file));
            assertTrue(wrap.isEnabled());
        } finally {
            if (p4File != null) {
                p4File.revert();
                p4File.refresh();
                assertFalse(p4File.isOpened());
            }
        }
    }

    /**
     * Test the action
     */
    public void testAction() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        IP4File p4File = (IP4File) resource;
        IP4PendingChangelist newList = null;
        try {
            assertFalse(p4File.isOpened());
            p4File.edit();
            p4File.refresh();
            assertTrue(p4File.isOpened());

            Action wrap = Utils.getDisabledAction();
            MoveToAnotherChangelistAction move = new MoveToAnotherChangelistAction();
            move.setAsync(false);
            move.selectionChanged(wrap, new StructuredSelection(file));
            assertTrue(wrap.isEnabled());

            newList = p4File.getConnection().createChangelist(
                    "test move changelists: " + getName(), null);

            assertNotNull(newList);
            assertTrue(newList.getId() > 0);

            newList.refresh();

            assertNotNull(newList.members());
            assertEquals(0, newList.members().length);

            move.move(newList);

            newList.refresh();

            assertNotNull(newList.members());
            assertEquals(1, newList.members().length);
            assertEquals(p4File, newList.members()[0]);

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
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
