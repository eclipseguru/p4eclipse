/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.SubmitAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmitActionTest extends ProjectBasedTestCase {

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

    private IP4File editFile() {
        IFile file = project.getFile("plugin.xml");
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        assertTrue(p4File.isOpened());
        return p4File;
    }

    /**
     * Test that submit is disabled for an empty non-default changelist
     */
    public void testEmptyNonDefaultEnablement() {
        IP4PendingChangelist list = null;
        try {
            Action wrap = Utils.getDisabledAction();
            SubmitAction submit = new SubmitAction();
            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(project);
            assertNotNull(connection);

            list = connection.createChangelist("test submit action enablement",
                    null);
            assertNotNull(list);
            assertEquals(0, list.members().length);
            submit.selectionChanged(wrap, new StructuredSelection(list));
            assertFalse(wrap.isEnabled());
        } finally {
            if (list != null) {
                list.delete();
                assertNull(list.getStatus());
                assertNull(list.getChangelist());
            }
        }
    }

    /**
     * Test that submit is enabled for a non-empty default changelist
     */
    public void testNonEmptyDefaultEnablement() {
        editFile();
        Action wrap = Utils.getDisabledAction();
        SubmitAction submit = new SubmitAction();
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);
        IP4PendingChangelist list = connection.getPendingChangelist(0);
        assertNotNull(list);
        assertTrue(list.members().length > 0);
        submit.selectionChanged(wrap, new StructuredSelection(list));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Test that submit is enabled for a non-empty non-default changelist
     */
    public void testNonEmptyNonDefaultEnablement() {
        IP4PendingChangelist list = null;
        IP4File p4File = null;
        try {
            p4File = editFile();
            Action wrap = Utils.getDisabledAction();
            SubmitAction submit = new SubmitAction();
            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(project);
            assertNotNull(connection);

            list = connection.createChangelist("test submit action enablement",
                    new IP4File[] { p4File });
            assertNotNull(list);
            assertTrue(list.members().length > 0);
            submit.selectionChanged(wrap, new StructuredSelection(list));
            assertTrue(wrap.isEnabled());
        } finally {
            if (p4File != null) {
                p4File.revert();
                p4File.refresh();
                assertFalse(p4File.isOpened());
            }
            if (list != null) {
                list.delete();
                assertNull(list.getStatus());
                assertNull(list.getChangelist());
            }
        }
    }
}
