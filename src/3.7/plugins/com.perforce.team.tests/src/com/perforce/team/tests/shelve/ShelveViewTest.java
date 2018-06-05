/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.shelve;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.shelve.ShelveTable;
import com.perforce.team.ui.shelve.ShelveView;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelveViewTest extends ConnectionBasedTestCase {

    /**
     * Test displaying the shelving view
     */
    public void testView() {
        IP4Connection connection = createConnection();
        assertTrue(connection.isShelvingSupported());
        try {
            IViewPart view = PerforceUIPlugin.getActivePage().showView(
                    ShelveView.VIEW_ID);
            assertNotNull(view);
            assertTrue(view instanceof ShelveView);
            ShelveView shelveView = (ShelveView) view;
            shelveView.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
            while (shelveView.isLoading()) {
                Utils.sleep(.1);
            }
            ShelveTable table = shelveView.getTable();
            assertNotNull(table);
            assertFalse(table.isLoading());
            assertNotNull(table.getTree());
            assertNotNull(table.getChangelists());
        } catch (PartInitException e) {
            handle(e);
        }
    }

}
