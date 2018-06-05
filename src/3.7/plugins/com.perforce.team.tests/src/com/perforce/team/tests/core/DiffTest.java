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
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.editor.P4CompareEditorInput;
import com.perforce.team.ui.p4java.actions.DiffDepotAction;
import com.perforce.team.ui.p4java.actions.EditAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffTest extends ProjectBasedTestCase {

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
     * Tests the diff action and opening the diff editor
     */
    public void testDiff() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        try {
            Utils.fillFile(file);
        } catch (Exception e1) {
            assertFalse("Exception thrown filling file", true);
        }
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertTrue(p4File.openedForEdit());
        DiffDepotAction diffAction = new DiffDepotAction();
        diffAction.setAsync(false);
        StructuredSelection selection = new StructuredSelection(file);
        Action wrapAction = Utils.getDisabledAction();
        diffAction.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());
        diffAction.run(wrapAction);

        Utils.sleep(.1);

        IEditorReference[] refs = PerforceUIPlugin.getActivePage()
                .getEditorReferences();
        assertNotNull(refs);

        assertTrue(refs.length > 0);
        boolean compareFound = false;
        for (int i = 0; i < refs.length; i++) {
            if ("org.eclipse.compare.CompareEditor".equals(refs[i].getId())) {
                try {
                    IEditorInput input = refs[i].getEditorInput();
                    if (input instanceof P4CompareEditorInput) {
                        compareFound = true;
                        break;
                    }
                } catch (PartInitException e) {
                }
            }
        }
        assertTrue(compareFound);
        assertTrue(PerforceUIPlugin.getActivePage().closeEditors(refs, false));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
