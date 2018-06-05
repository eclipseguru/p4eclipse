/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.dialogs.ConfirmRevertDialog;
import com.perforce.team.ui.p4java.actions.EditAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConfirmRevertDialogTest extends ProjectBasedTestCase {

    /**
     * Test confirm revert dialog
     */
    public void testDialog() {
        IFile file = project.getFile("plugin.xml");
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(p4File));
        edit.run(null);
        assertTrue(p4File.openedForEdit());
        ConfirmRevertDialog dialog = ConfirmRevertDialog.openQuestion(
                Utils.getShell(), new IP4File[] { p4File }, false);
        assertNotNull(dialog);
        try {
            dialog.open();
            assertNull(dialog.getSelected());
            dialog.close();
            assertNotNull(dialog.getSelected());
            assertEquals(1, dialog.getSelected().length);
            assertEquals(p4File, dialog.getSelected()[0]);
        } finally {
            if (dialog.getShell() != null) {
                dialog.close();
            }
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
