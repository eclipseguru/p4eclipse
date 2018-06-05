/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.RevertUnchangedAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevertUnchangedTest extends ProjectBasedTestCase {

    /**
     * Test the revert unchanged action
     */
    public void testRevertUnchanged() {
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());

        StructuredSelection selection = new StructuredSelection(file);
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, selection);
        edit.run(null);

        IP4Resource p4Resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);
        IP4File p4File = (IP4File) p4Resource;

        assertTrue(p4File.isOpened());
        assertTrue(p4File.openedForEdit());

        Action wrapAction = Utils.getDisabledAction();
        RevertUnchangedAction revertAction = new RevertUnchangedAction();
        revertAction.setAsync(false);
        selection = new StructuredSelection(p4File);
        revertAction.selectionChanged(wrapAction, selection);
        revertAction.runAction(false);
        assertNotNull(revertAction.getSelected());

        assertFalse(p4File.isOpened());
        assertFalse(p4File.openedForEdit());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
