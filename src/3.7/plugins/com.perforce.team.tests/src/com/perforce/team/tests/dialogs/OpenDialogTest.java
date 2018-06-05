/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.dialogs.OpenDialog;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class OpenDialogTest extends ProjectBasedTestCase {

    /**
     * Test open dialog
     */
    public void testDialog() {
        OpenDialog dialog = null;
        IP4PendingChangelist newList = null;
        try {
            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(this.project);
            assertNotNull(connection);

            IFile file = this.project.getFile("plugin.xml");
            assertTrue(file.exists());
            IP4Resource resource = P4ConnectionManager.getManager()
                    .getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;

            IP4PendingChangelist defaultList = connection
                    .getPendingChangelist(0);
            assertNotNull(defaultList);

            newList = connection.createChangelist("test move change dialog: "
                    + getName(), null);
            assertNotNull(newList);

            dialog = new OpenDialog(Utils.getShell(),
                    new IP4Resource[] { p4File }, connection,
                    "Test open dialog", "Test open action");
            dialog.setBlockOnOpen(false);
            dialog.open();
            dialog.updateSelection();
            assertEquals(0, dialog.getSelectedChangeId());
            assertNotNull(dialog.getSelectedFiles());
            assertEquals(1, dialog.getSelectedFiles().length);
            assertEquals(p4File, dialog.getSelectedFiles()[0]);
        } finally {
            if (dialog != null) {
                dialog.close();
            }
            if (newList != null) {
                newList.delete();
                assertNull(newList.getStatus());
                assertNull(newList.getChangelist());
            }
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.core";
    }

}
