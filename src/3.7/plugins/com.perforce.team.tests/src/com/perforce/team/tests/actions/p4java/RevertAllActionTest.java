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
import com.perforce.team.ui.p4java.actions.RevertAllAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevertAllActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
        addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
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
        IP4Connection connection = createConnection();
        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);

        Action wrap = Utils.getDisabledAction();
        RevertAllAction action = new RevertAllAction();
        action.selectionChanged(wrap, new StructuredSelection());
        assertFalse(wrap.isEnabled());

        action.selectionChanged(wrap, new StructuredSelection(defaultList));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Tests the revert action
     */
    public void testAction() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        IFile file2 = project.getFile(new Path("META-INF/MANIFEST.MF"));
        assertTrue(file2.exists());

        IP4PendingChangelist newList = P4Workspace.getWorkspace()
                .getConnection(project)
                .createChangelist("test: " + getName(), null);
        assertNotNull(newList);
        assertTrue(newList.getId() > 0);

        try {
            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);

            IP4File p4File = (IP4File) resource;

            assertFalse(p4File.isOpened());

            p4File.edit(newList.getId());
            p4File.refresh();

            assertTrue(p4File.isOpened());

            IP4Resource resource2 = P4Workspace.getWorkspace().getResource(
                    file2);
            assertNotNull(resource2);
            assertTrue(resource2 instanceof IP4File);

            IP4File p4File2 = (IP4File) resource2;

            assertFalse(p4File2.isOpened());

            p4File2.edit(newList.getId());
            p4File2.refresh();

            assertTrue(p4File2.isOpened());

            newList.refresh();

            Action wrap = Utils.getDisabledAction();
            RevertAllAction action = new RevertAllAction();
            action.setAsync(false);

            action.selectionChanged(wrap, new StructuredSelection(newList));
            assertTrue(wrap.isEnabled());
            action.runAction(false);

            p4File.refresh();
            assertFalse(p4File.isOpened());

            p4File2.refresh();
            assertFalse(p4File2.isOpened());
        } finally {
            if (newList != null) {
                newList.delete();
                assertNull(newList.getChangelist());
            }
        }
    }

}
