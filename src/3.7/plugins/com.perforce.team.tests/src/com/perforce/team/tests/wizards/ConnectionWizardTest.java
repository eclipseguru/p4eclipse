/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.wizards;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.connection.ClientWizardPage;
import com.perforce.team.ui.connection.ConnectionWizard;
import com.perforce.team.ui.connection.ConnectionWizardDialog;
import com.perforce.team.ui.connection.IConnectionWizard;
import com.perforce.team.ui.connection.ServerWizardPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionWizardTest extends ConnectionBasedTestCase {

    private ConnectionWizardDialog createDialog() {
        ConnectionWizard wizard = new ConnectionWizard();
        wizard.init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
        ConnectionWizardDialog dialog = new ConnectionWizardDialog(
                P4UIUtils.getShell(), wizard);
        dialog.setBlockOnOpen(false);
        dialog.open();
        return dialog;
    }

    /**
     * Test connection dialog
     */
    public void testDialog() {
        ConnectionWizardDialog dialog = createDialog();
        try {
            assertNotNull(dialog.getCurrentPage());
            assertTrue(dialog.getCurrentPage() instanceof ServerWizardPage);
            ServerWizardPage page = (ServerWizardPage) dialog.getCurrentPage();

            page.setPort(parameters.getPort());
            page.setUser(parameters.getUser());

            assertNotNull(page.getNextPage());
            assertTrue(page.getNextPage() instanceof ClientWizardPage);

//            ClientWizardPage clientPage = (ClientWizardPage) page.getNextPage();
//            clientPage.setClient(parameters.getClient());
//            clientPage.validatePage(); // apply value
            page.setCharset("utf8");
            

            ConnectionParameters wizardParams = dialog
                    .getConnectionParameters();
            assertEquals(parameters.getUser(), wizardParams.getUser());
            assertEquals(parameters.getPort(),
                    wizardParams.getPort());
            assertEquals("utf8", wizardParams.getCharset());
//            assertEquals(parameters.getClient(), wizardParams.getClient());
//            assertNotNull(dialog.getImportedFolders());
//            assertEquals(0, dialog.getImportedFolders().length);

        } finally {
            dialog.close();
        }
    }

    /**
     * Test with initial content
     */
    public void testWithInitial() {
        IP4Connection connection = createConnection();
        ConnectionWizardDialog dialog = new ConnectionWizardDialog(
                P4UIUtils.getShell(), new ConnectionWizard(connection));
        dialog.setBlockOnOpen(false);
        dialog.open();
        try {
            assertNotNull(dialog.getCurrentPage());
            assertTrue(dialog.getCurrentPage() instanceof ServerWizardPage);
            ServerWizardPage page = (ServerWizardPage) dialog.getCurrentPage();
            assertNotNull(page.getPort());
            assertEquals(connection.getParameters().getPort(),
                    page.getPort());
        } finally {
            dialog.close();
        }
    }

    /**
     * Test connection wizard
     */
    public void testWizard() {
        ConnectionWizardDialog dialog = createDialog();
        try {
            assertNotNull(dialog.getCurrentPage());
            assertTrue(dialog.getCurrentPage() instanceof ServerWizardPage);
            ServerWizardPage page = (ServerWizardPage) dialog.getCurrentPage();

            page.setPort(parameters.getPort());
            page.setUser(parameters.getUser());

            assertNotNull(page.getNextPage());
            assertTrue(page.getNextPage() instanceof ClientWizardPage);

//            ClientWizardPage clientPage = (ClientWizardPage) page.getNextPage();
//
//            clientPage.setClient(parameters.getClient());
            page.setCharset("utf8");

            IConnectionWizard wizard = dialog.getWizard();
            assertNotNull(wizard);
            assertEquals(parameters.getUser(), wizard.getUser());
            assertEquals(parameters.getPort(),
                    wizard.getPort());
            assertEquals("utf8", wizard.getCharset());
//            assertEquals(parameters.getClient(), wizard.getClient());

            String ticket = "test-ticket";
            wizard.setAuthTicket(ticket);
            assertEquals(ticket, wizard.getAuthTicket());

        } finally {
            dialog.close();
        }
    }

    /**
     * Test client page
     */
    public void testClientPage() {
        ConnectionWizardDialog dialog = createDialog();
        try {
            assertNotNull(dialog.getCurrentPage());
            assertTrue(dialog.getCurrentPage() instanceof ServerWizardPage);
            ServerWizardPage page = (ServerWizardPage) dialog.getCurrentPage();

            page.setPort(parameters.getPort());
            page.setUser(parameters.getUser());

            assertNotNull(page.getNextPage());
            assertTrue(page.getNextPage() instanceof ClientWizardPage);

            ClientWizardPage clientPage = (ClientWizardPage) page.getNextPage();

            assertFalse(clientPage.getControl().isVisible());

            dialog.showPage(clientPage);

            assertTrue(clientPage.getControl().isVisible());

            assertNotNull(clientPage.getTitle());
            assertTrue(clientPage.getTitle().length() > 0);
            assertNotNull(clientPage.getDescription());
            assertTrue(clientPage.getDescription().length() > 0);
            assertNull(clientPage.getClient());
            assertTrue(clientPage.isExistingClientSelected());
            assertFalse(clientPage.isNewClientSelected());
            assertNull(clientPage.getLocation());
            assertFalse(clientPage.shouldCreate());
            assertNotNull(clientPage.getErrorMessage());
            assertFalse(clientPage.isPageComplete());

            clientPage.setClient("    ");

            assertNotNull(clientPage.getErrorMessage());
            assertFalse(clientPage.isPageComplete());

            clientPage.setClient(parameters.getClient());

            assertNull(clientPage.getErrorMessage());
            assertTrue(clientPage.isPageComplete());

            assertEquals(parameters.getClient(), clientPage.getClient());

            page.setCharset("utf8");

            assertNull(clientPage.getErrorMessage());
            assertTrue(clientPage.isPageComplete());

            assertEquals("utf8", page.getCharset());

            assertNull(clientPage.getNextPage());

            page.setCharset("none");

            assertNull(clientPage.getErrorMessage());
            assertTrue(clientPage.isPageComplete());

            assertEquals("none", page.getCharset());

//            assertNotNull(clientPage.getNextPage());
        } finally {
            dialog.close();
        }
    }

    /**
     * Test server page
     */
    public void testServerPage() {
        ConnectionWizardDialog dialog = createDialog();
        try {
            assertNotNull(dialog.getCurrentPage());
            assertTrue(dialog.getCurrentPage() instanceof ServerWizardPage);
            ServerWizardPage page = (ServerWizardPage) dialog.getCurrentPage();

            assertNotNull(page.getTitle());
            assertTrue(page.getTitle().length() > 0);
            assertNotNull(page.getDescription());
            assertTrue(page.getDescription().length() > 0);
            assertTrue(page.getControl().isVisible());
            assertNull(page.getErrorMessage());
            assertFalse(page.savePassword());
            assertFalse(page.isPageComplete());

            page.setPort("     ");
            assertNotNull(page.getErrorMessage());
            assertFalse(page.isPageComplete());

            assertNotNull(page.getPort());

            String port = " testport:1666 ";
            page.setPort(port);

            assertNotNull(page.getErrorMessage());
            assertFalse(page.isPageComplete());
            assertEquals(port.trim(), page.getPort());

            page.setUser("test");

            assertNull(page.getErrorMessage());
            assertTrue(page.isPageComplete());
        } finally {
            dialog.close();
        }
    }
}
