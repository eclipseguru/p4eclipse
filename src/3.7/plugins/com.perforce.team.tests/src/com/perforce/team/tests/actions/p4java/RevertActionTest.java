/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.RevertAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevertActionTest extends ProjectBasedTestCase {

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
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

    /**
     * Test action enablement
     */
    public void testEnablement() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        IP4File p4File = (IP4File) resource;

        assertFalse(p4File.isOpened());

        Action wrap = Utils.getDisabledAction();
        RevertAction action = new RevertAction();

        action.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());

        action.selectionChanged(wrap, new StructuredSelection(file));
        assertFalse(wrap.isEnabled());

        p4File.edit();
        p4File.refresh();

        assertTrue(p4File.isOpened());

        action.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());

        p4File.revert();
        p4File.refresh();
        assertFalse(p4File.isOpened());
    }

    /**
     * Tests the revert action
     */
    public void testAction() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        IP4File p4File = (IP4File) resource;

        assertFalse(p4File.isOpened());

        p4File.edit();
        p4File.refresh();

        assertTrue(p4File.isOpened());

        Action wrap = Utils.getDisabledAction();
        RevertAction action = new RevertAction();
        action.setAsync(false);

        action.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());

        action.runAction(false);

        p4File.refresh();
        assertFalse(p4File.isOpened());
    }

}
