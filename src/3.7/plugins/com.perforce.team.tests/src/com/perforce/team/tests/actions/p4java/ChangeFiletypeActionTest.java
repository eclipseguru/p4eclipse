/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.ChangeFiletypeAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.RevertAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangeFiletypeActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        super.addFile(project.getFile("about.ini"));
    }

    /**
     * Tests the change filetype action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        ChangeFiletypeAction change = new ChangeFiletypeAction();
        assertFalse(wrap.isEnabled());
        change.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());
        change.selectionChanged(wrap, new StructuredSelection(file));
        assertFalse(wrap.isEnabled());
        EditAction editAction = new EditAction();
        editAction.setAsync(false);
        editAction.selectionChanged(null, new StructuredSelection(file));
        editAction.run(null);
        change.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Tests the change filetype action
     */
    public void testAction() {
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertFalse(p4File.isOpened());
        assertEquals("text", p4File.getHeadType());

        EditAction editAction = new EditAction();
        editAction.setAsync(false);
        editAction.selectionChanged(null, new StructuredSelection(file));
        editAction.run(null);

        p4File.refresh();

        assertTrue(p4File.isOpened());

        Action wrap = Utils.getDisabledAction();
        ChangeFiletypeAction change = new ChangeFiletypeAction();
        change.setAsync(false);
        change.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
        change.changeType("+x");

        p4File.refresh();

        assertEquals("xtext", p4File.getOpenedType());

        RevertAction revert = new RevertAction();
        revert.setAsync(false);
        revert.selectionChanged(wrap, new StructuredSelection(file));
        revert.runAction(false);

        p4File.refresh();
        assertFalse(p4File.isOpened());
        assertEquals("text", p4File.getHeadType());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
