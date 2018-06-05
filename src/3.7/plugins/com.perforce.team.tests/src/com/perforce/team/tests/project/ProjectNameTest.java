/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.project;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.project.ProjectNameDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ProjectNameTest extends P4TestCase {

    /**
     * Test project name dialog
     */
    public void testDialog() {
        String name = "test_project_name1";
        ProjectNameDialog dialog = new ProjectNameDialog(Utils.getShell(), name);
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertEquals(name, dialog.getEnteredName());
            assertNull(dialog.getErrorMessage());
        } finally {
            dialog.close();
        }
    }

}
