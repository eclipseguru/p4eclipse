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
import com.perforce.team.ui.p4java.actions.ScheduleResolveAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ScheduleResolveActionTest extends ProjectBasedTestCase {

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
     * Tests the schedule resolve action
     */
    public void testAction() {
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        SyncRevisionAction syncAction = new SyncRevisionAction();
        syncAction.setAsync(false);
        syncAction.selectionChanged(null, new StructuredSelection(file));
        syncAction.runAction("#2");

        p4File.refresh();

        assertEquals(2, p4File.getHaveRevision());

        Action wrap = Utils.getDisabledAction();
        ScheduleResolveAction resolve = new ScheduleResolveAction();
        resolve.setAsync(false);
        resolve.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
        resolve.run(wrap);

        p4File.refresh();

        assertTrue(p4File.getHaveRevision() > 2);
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
