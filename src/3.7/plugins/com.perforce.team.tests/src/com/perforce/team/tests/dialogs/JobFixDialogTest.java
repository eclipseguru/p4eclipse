/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.dialogs.JobFixDialog;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobFixDialogTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createJob();
    }

    /**
     * Test job fix dialog
     */
    public void testDialog() {
        IP4Connection connection = createConnection();
        JobFixDialog dialog = new JobFixDialog(Utils.getShell(), connection);
        dialog.setBlockOnOpen(false);
        dialog.open();
        try {
            assertNull(dialog.getSelectedJobs());
            while (dialog.isLoading()) {
                Utils.sleep(.1);
            }
            TableViewer viewer = dialog.getViewer();
            assertNotNull(viewer);
            assertTrue(viewer.getTable().getItemCount() > 0);
            TableItem item = viewer.getTable().getItem(0);
            assertNotNull(item);
            assertNotNull(item.getData());
            assertTrue(item.getData() instanceof IP4Job);
            viewer.getTable().setSelection(0);
            dialog.doubleClick();
            assertNotNull(dialog.getSelectedJobs());
            assertEquals(1, dialog.getSelectedJobs().length);
            assertNotNull(dialog.getSelectedJobs()[0]);
        } finally {
            dialog.close();
        }
    }

}
