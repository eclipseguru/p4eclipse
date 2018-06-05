/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.dialogs.ProjectPropertiesDialog;

import org.eclipse.jface.preference.PreferenceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ProjectPropertiesDialogTest extends ProjectBasedTestCase {

    /**
     * Test project properties dialog
     */
    public void testDialog() {
        PreferenceDialog dialog = P4UIUtils.openPropertyPage(
                "com.perforce.team.ui.dialogs.ProjectPropertiesDialog",
                project, false);
        assertNotNull(dialog);
        try {
            assertNotNull(dialog.getSelectedPage());
            assertTrue(dialog.getSelectedPage() instanceof ProjectPropertiesDialog);
            ProjectPropertiesDialog projectDialog = (ProjectPropertiesDialog) dialog
                    .getSelectedPage();
            assertNotNull(projectDialog.getElement());
            assertEquals(project, projectDialog.getElement());
            assertNull(projectDialog.getErrorMessage());
            assertNull(projectDialog.getMessage());
            assertNotNull(projectDialog.getTitle());
        } finally {
            dialog.close();
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
