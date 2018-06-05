/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.jobs;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.JobColumnsDialog;
import com.perforce.team.ui.views.JobView;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobColumnsDialogTest extends ConnectionBasedTestCase {

    /**
     * Test the adding and removing columns in the jobs column dialog
     */
    public void testJobsColumnDialog() {
        IP4Connection connection = createConnection();
        IJobSpec spec = connection.getJobSpec();
        assertNotNull(spec);
        int fieldCount = spec.getFields().size();
        JobView view = JobView.showView();
        assertNotNull(view);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        JobColumnsDialog dialog = new JobColumnsDialog(PerforceUIPlugin
                .getDisplay().getActiveShell(), view.getJobsDialog());
        dialog.setBlockOnOpen(false);
        dialog.open();
        String[] originalAvailable = dialog.getAvailableList();
        String[] originalShow = dialog.getShowList();
        assertNotNull(originalAvailable);
        assertNotNull(originalShow);
        assertEquals(fieldCount, originalAvailable.length + originalShow.length);

        for (String avail : originalAvailable) {
            dialog.selectAvailableColumn(avail);
            dialog.addSelection();
        }
        assertEquals(0, dialog.getAvailableList().length);
        assertEquals(originalAvailable.length + originalShow.length,
                dialog.getShowList().length);
        originalAvailable = dialog.getAvailableList();
        originalShow = dialog.getShowList();
        for (String show : originalShow) {
            dialog.selectShowColumn(show);
            dialog.removeSelection();
        }
        assertEquals(1, dialog.getShowList().length);
        assertEquals(originalAvailable.length + originalShow.length - 1,
                dialog.getAvailableList().length);
        dialog.submit();
    }

    /**
     * Tests the jobs view with the single required column of Job
     */
    public void testSingleColumn() {
        IP4Connection connection = createConnection();
        JobView view = JobView.showView();
        assertNotNull(view);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        JobColumnsDialog dialog = new JobColumnsDialog(PerforceUIPlugin
                .getDisplay().getActiveShell(), view.getJobsDialog());
        dialog.setBlockOnOpen(false);
        dialog.open();
        String[] originalShow = dialog.getShowList();
        for (String show : originalShow) {
            dialog.selectShowColumn(show);
            dialog.removeSelection();
        }
        dialog.submit();
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
        view = JobView.showView();
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        assertEquals(1, view.getTableControl().getColumnCount());

    }

    /**
     * Tests the jobs view with all available columns from the job spec
     */
    public void testAllColumns() {
        IP4Connection connection = createConnection();
        JobView view = JobView.showView();
        assertNotNull(view);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        JobColumnsDialog dialog = new JobColumnsDialog(PerforceUIPlugin
                .getDisplay().getActiveShell(), view.getJobsDialog());
        dialog.setBlockOnOpen(false);
        dialog.open();
        String[] originalAvailable = dialog.getAvailableList();
        String[] originalShow = dialog.getShowList();
        assertNotNull(originalAvailable);
        assertTrue(originalAvailable.length > 0);
        assertNotNull(originalShow);
        assertTrue(originalShow.length > 0);
        for (String avail : originalAvailable) {
            dialog.selectAvailableColumn(avail);
            dialog.addSelection();
        }
        dialog.submit();
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
        view = JobView.showView();
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        assertEquals(originalShow.length + originalAvailable.length, view
                .getTableControl().getColumnCount());
    }
}
