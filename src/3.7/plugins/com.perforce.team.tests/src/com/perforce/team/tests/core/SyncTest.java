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
import com.perforce.team.ui.p4java.actions.SyncAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SyncTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IFile file = project.getFile("plugin.xml");
        IClient client = createConnection().getClient();
        for (int i = 0; i < 3; i++) {
            addFile(client, file);
        }
    }

    /**
     * Tests the syn action and sync rev action classes
     */
    public void testSyncActions() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        StructuredSelection selection = new StructuredSelection(file);

        Action wrap = Utils.getDisabledAction();
        SyncRevisionAction syncRev = new SyncRevisionAction();
        syncRev.setAsync(false);
        syncRev.selectionChanged(wrap, selection);
        assertTrue(wrap.isEnabled());
        syncRev.runAction("#2");

        IP4Resource p4Resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);

        IP4File p4File = (IP4File) p4Resource;

        assertFalse(p4File.isSynced());
        assertEquals(2, p4File.getHaveRevision());
        assertEquals(3, p4File.getHeadRevision());

        wrap = Utils.getDisabledAction();
        SyncAction sync = new SyncAction();
        sync.setAsync(false);
        sync.selectionChanged(wrap, selection);
        assertTrue(wrap.isEnabled());
        sync.run(wrap);

        assertTrue(p4File.isSynced());
        assertEquals(3, p4File.getHaveRevision());
        assertEquals(3, p4File.getHeadRevision());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
