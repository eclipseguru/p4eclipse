/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.SyncPreviewAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.views.ConsoleDocument;
import com.perforce.team.ui.views.ConsoleView;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SyncPreviewActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        for (int i = 0; i < 3; i++) {
            addFile(client, project.getFile("plugin.xml"));
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

    /**
     * Test action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        SyncPreviewAction preview = new SyncPreviewAction();
        preview.selectionChanged(wrap, StructuredSelection.EMPTY);
        assertFalse(wrap.isEnabled());
        preview.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Test sync preview action
     */
    public void testAction() {
        ConsoleView view = ConsoleView.openInActivePerspective();
        assertNotNull(view);
        TextViewer viewer = view.getViewer();
        assertNotNull(viewer);
        ((ConsoleDocument) viewer.getDocument()).clear();
        String content = viewer.getDocument().get();
        assertNotNull(content);
        assertTrue(content.length() == 0);

        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        SyncRevisionAction syncRev = new SyncRevisionAction();
        syncRev.setAsync(false);
        syncRev.selectionChanged(null, new StructuredSelection(file));
        syncRev.runAction("#2");

        IP4Resource p4Resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);

        IP4File p4File = (IP4File) p4Resource;

        assertFalse(p4File.isSynced());
        assertEquals(2, p4File.getHaveRevision());
        assertEquals(3, p4File.getHeadRevision());

        Action wrap = Utils.getDisabledAction();
        SyncPreviewAction preview = new SyncPreviewAction();
        preview.setAsync(false);
        preview.selectionChanged(wrap, new StructuredSelection(project));
        preview.run(wrap);

        Utils.sleep(.1);

        content = viewer.getDocument().get();
        assertTrue(content.contains("Executing p4 sync"));
        assertTrue(content.contains(p4File.getRemotePath()));
        assertTrue(content
                .contains(FileAction.UPDATED.toString().toLowerCase()));
        assertTrue(content.contains(p4File.getClientPath()));
    }
}
