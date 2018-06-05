/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.client;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditClientAction;
import com.perforce.team.ui.server.ClientWidget;
import com.perforce.team.ui.server.EditClientDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditClientTest extends ConnectionBasedTestCase {

    /**
     * Test edit client action enablement
     */
    public void testEditActionEnablement() {
        Action wrap = Utils.getDisabledAction();
        EditClientAction edit = new EditClientAction();
        IP4Connection connection1 = createConnection();
        assertFalse(connection1.isOffline());
        IP4Connection connection2 = createConnection();

        edit.selectionChanged(wrap, new StructuredSelection(connection1));
        assertTrue(wrap.isEnabled());

        connection1.setOffline(true);
        edit.selectionChanged(wrap, new StructuredSelection(connection1));
        assertFalse(wrap.isEnabled());

        edit.selectionChanged(wrap, new StructuredSelection(new Object[] {
                connection1, connection2 }));
        assertFalse(wrap.isEnabled());

        connection1.setOffline(false);
        edit.selectionChanged(wrap, new StructuredSelection(new Object[] {
                connection1, connection2 }));
        assertFalse(wrap.isEnabled());
    }

    /**
     * Test basic client view editing
     */
    public void testEditClient() {
        EditClientDialog editDialog = null;
        try {
            editDialog = new EditClientDialog(Utils.getShell(),
                    createConnection());
            editDialog.setBlockOnOpen(false);
            editDialog.open();
            ClientWidget widget = editDialog.getClientWidget();
            assertNotNull(widget);
            assertNotNull(widget.getWorkspaceText());
            assertNotNull(widget.getDescriptionText());
            assertNotNull(widget.getHostText());
            assertNotNull(widget.getLineEndingText());
            assertNotNull(widget.getOwnerText());
            assertNotNull(widget.getRootText());
            assertNotNull(widget.getSubmitOptionText());
            assertNotNull(widget.getViewText());
            assertNull(widget.getCurrentSpec());
            widget.updateCurrentSpec();
            assertNotNull(editDialog.getEditedSpec());
            assertNotNull(widget.getCurrentSpec());
        } catch (P4JavaException e) {
            assertFalse("P4JavaException thrown", true);
        } finally {
            if (editDialog != null) {
                editDialog.close();
            }
        }
    }

    /**
     * Test invalid dialog
     */
    public void testInvalid() {
        try {
            new EditClientDialog(Utils.getShell(), null);
            assertFalse("Exception not thrown", true);
        } catch (P4JavaException e) {
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }

}
