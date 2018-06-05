/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.preferences;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.preferences.decorators.VariablesDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class VariablesDialogTest extends P4TestCase {

    /**
     * Test variables dialog
     */
    public void testEmptyDialog() {
        VariablesDialog dialog = new VariablesDialog(Utils.getShell(), null,
                null);
        dialog.setBlockOnOpen(false);
        dialog.open();
        assertNotNull(dialog.getAvailableVariables());
        assertEquals(0, dialog.getAvailableVariables().length);
        assertNotNull(dialog.getSelectedVariables());
        assertEquals(0, dialog.getSelectedVariables().length);
        assertNotNull(dialog.getDescriptions());
        assertEquals(0, dialog.getDescriptions().length);
        dialog.close();
    }

    /**
     * Test variables dialog
     */
    public void testDialog() {
        String[] vars = new String[] { "var1", "var2", "var3" };
        String[] descs = new String[] { "var1 desc", "var2 desc", "var3 desc" };
        VariablesDialog dialog = new VariablesDialog(Utils.getShell(), vars,
                descs);
        dialog.setBlockOnOpen(false);
        dialog.open();
        assertNotNull(dialog.getAvailableVariables());
        assertEquals(vars.length, dialog.getAvailableVariables().length);
        for (int i = 0; i < dialog.getAvailableVariables().length; i++) {
            assertEquals(vars[i], dialog.getAvailableVariables()[i]);
        }
        assertNotNull(dialog.getDescriptions());
        assertEquals(descs.length, dialog.getDescriptions().length);
        for (int i = 0; i < dialog.getDescriptions().length; i++) {
            assertNotNull(dialog.getDescriptions()[i]);
            assertTrue(dialog.getDescriptions()[i].contains(vars[i]));
            assertTrue(dialog.getDescriptions()[i].contains(descs[i]));
        }

        assertNotNull(dialog.getSelectedVariables());
        assertEquals(0, dialog.getSelectedVariables().length);
        assertNotNull(dialog.getViewer());
        dialog.getViewer().setChecked(dialog.getDescriptions()[0], true);
        dialog.getViewer().setChecked(dialog.getDescriptions()[2], true);
        Event e = new Event();
        e.detail = SWT.CHECK;
        e.item = dialog.getViewer().getTable().getItem(0);
        e.widget = dialog.getViewer().getTable();
        SelectionEvent se = new SelectionEvent(e);
        dialog.getViewer().handleSelect(se);
        assertNotNull(dialog.getSelectedVariables());
        assertEquals(2, dialog.getSelectedVariables().length);
        assertEquals(vars[0], dialog.getSelectedVariables()[0]);
        assertEquals(vars[2], dialog.getSelectedVariables()[1]);
        dialog.close();
    }

}
