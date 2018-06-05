/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.actions.OpenAction;
import com.perforce.team.ui.p4java.actions.EditAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorReference;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class OpenEditorActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IFile file = project.getFile("plugin.xml");
        IClient client = createConnection().getClient();
        addFile(client, file);
    }

    /**
     * Tests the open action that opens an editor on the selection
     */
    public void testOpenAction() {
        IFile file = project.getFile("plugin.xml");
        IP4Resource p4Resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);
        IP4File p4File = (IP4File) p4Resource;

        StructuredSelection selection = new StructuredSelection(file);
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, selection);
        edit.run(null);

        OpenAction.openFiles(new String[] { p4File.getLocalPath() });
        IEditorReference[] refs = PerforceUIPlugin.getActivePage()
                .getEditorReferences();
        assertTrue(refs.length > 0);
        boolean editorFound = false;
        for (IEditorReference ref : PerforceUIPlugin.getActivePage()
                .getEditorReferences()) {
            if ("plugin.xml".equals(ref.getName())) {
                editorFound = true;
                break;
            }
        }
        assertTrue(editorFound);
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
