/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.RecoverDeletedAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RecoverDeleteActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        addFile(client, project.getFile("plugin.xml"));
        addFile(client, project.getFile("build-32.xml"));
        deleteFile(client, project.getFile("build-32.xml"));
    }

    /**
     * Test action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        RecoverDeletedAction recover = new RecoverDeletedAction();
        recover.setAsync(false);
        IFile recoverable = project.getFile("build-32.xml");
        assertFalse(recoverable.exists());
        recover.selectionChanged(wrap, new StructuredSelection(recoverable));
        assertTrue(wrap.isEnabled());
        recover.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(
                recoverable);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File file = (IP4File) resource;
        assertFalse(file.isOpened());
        assertFalse(file.isSynced());
        assertEquals(file.getHaveRevision(), file.getHeadRevision() - 1);
    }

    /**
     * Test action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        RecoverDeletedAction recover = new RecoverDeletedAction();
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        recover.selectionChanged(wrap, new StructuredSelection(file));
        assertFalse(wrap.isEnabled());
        IFile recoverable = project.getFile("build-32.xml");
        assertFalse(recoverable.exists());
        recover.selectionChanged(wrap, new StructuredSelection(recoverable));
        assertTrue(wrap.isEnabled());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/com.perforce.team.plugin";
    }

}
