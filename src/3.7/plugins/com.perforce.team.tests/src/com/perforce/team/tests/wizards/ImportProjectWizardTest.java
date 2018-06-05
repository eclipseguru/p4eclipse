/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.wizards;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.project.ConnectionSelectionPage;
import com.perforce.team.ui.project.ExistingProjectWizard;
import com.perforce.team.ui.project.ImportProjectsWizardPage;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ImportProjectWizardTest extends ConnectionBasedTestCase {

    /**
     * Test import existing projects wizard
     */
    public void testImportWizard() {
        ExistingProjectWizard wizard = new ExistingProjectWizard();
        wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
        WizardDialog dialog = new WizardDialog(Utils.getShell(), wizard);
        try {
            IP4Connection connection = P4Workspace.getWorkspace()
                    .getConnection(this.parameters);
            assertNotNull(connection);
            dialog.setBlockOnOpen(false);
            dialog.open();
            IWizardPage page1 = dialog.getCurrentPage();
            assertNotNull(page1);
            assertTrue(page1 instanceof ConnectionSelectionPage);
            ConnectionSelectionPage connPage = (ConnectionSelectionPage) page1;
            assertNotNull(connPage.getDescription());
            assertNotNull(connPage.getTitle());
            assertTrue(connPage.isExistingConnection());
            assertFalse(connPage.isNewConnection());
            assertNotNull(connPage.getConnection());
            assertTrue(connPage.getConnections().length > 0);
            IWizardPage page2 = wizard.getNextPage(page1);
            assertNotNull(page2);
            assertTrue(page2 instanceof ImportProjectsWizardPage);
            dialog.showPage(page2);
            assertEquals(page2, dialog.getCurrentPage());
            ImportProjectsWizardPage projPage = (ImportProjectsWizardPage) page2;
            assertTrue(projPage.isImportSelected());
            assertNotNull(projPage.getDescription());
            assertNotNull(projPage.getTitle());
            assertNotNull(projPage.getClient());
            assertEquals(this.parameters.getClient(), projPage.getClient());
            assertNotNull(projPage.getPort());
            assertEquals(this.parameters.getPort(),
                    projPage.getPort());
            assertNotNull(projPage.getUser());
            assertEquals(this.parameters.getUser(), projPage.getUser());
            assertEquals(this.parameters.getCharset(), projPage.getCharset());
        } finally {
            dialog.close();
        }
    }

}
