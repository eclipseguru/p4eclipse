/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.project;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.project.ShareProjectsDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShareProjectTest extends ConnectionBasedTestCase {

    /**
     * Test share project dialog
     */
    public void testDialog() {
        IP4Connection connection = createConnection();
        ShareProjectsDialog dialog = new ShareProjectsDialog(Utils.getShell(),
                connection);
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertNull(dialog.getSelectedProjects());
            assertNotNull(dialog.getErrorMessage());
        } finally {
            dialog.close();
        }
    }

}
