/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.jobs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.dialogs.JobsPreferencesDialog;
import com.perforce.team.ui.views.JobView;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobRetrievalTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        createJob();
        for (int i = 0; i < 150; i++) {
            createJob();
        }
    }

    /**
     * Test the setting of max job retrievals to 150
     */
    public void testSet150() {
        IP4Connection connection = createConnection();
        assertNotNull(connection);
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                Display.getCurrent().getActiveShell(),
                "com.perforce.team.ui.dialogs.JobsPreferencesDialog",
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof JobsPreferencesDialog);
        JobsPreferencesDialog prefPage = (JobsPreferencesDialog) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("150");
        prefPage.performOk();
        dialog.close();
        JobView view = JobView.showView();
        assertNotNull(view);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        view.refreshRetrieveCount();
        view.refresh();
        while (view.isLoading()) {
            try {
                while (Display.getCurrent().readAndDispatch())
                    ;
            } catch (Exception e) {
            } catch (Error e) {
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        assertNotNull(view.getTableControl());
        assertEquals(150, view.getTableControl().getItemCount());
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
    }

    /**
     * Test the setting of setting retrieval preference to all jobs
     */
    public void testSetAll() {
        IP4Connection connection = createConnection();
        assertNotNull(connection);
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                Display.getCurrent().getActiveShell(),
                "com.perforce.team.ui.dialogs.JobsPreferencesDialog",
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof JobsPreferencesDialog);
        JobsPreferencesDialog prefPage = (JobsPreferencesDialog) page;
        prefPage.selectAllElements();
        prefPage.performOk();
        dialog.close();
        JobView view = JobView.showView();
        assertNotNull(view);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        view.refreshRetrieveCount();
        view.refresh();
        while (view.isLoading()) {
            try {
                while (Display.getCurrent().readAndDispatch())
                    ;
            } catch (Exception e) {
            } catch (Error e) {
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        assertNotNull(view.getTableControl());
        assertTrue(view.getTableControl().getItemCount() > 0);
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
    }

    /**
     * Test the setting of max job retrievals to 1
     */
    public void testSet1() {
        IP4Connection connection = createConnection();
        assertNotNull(connection);
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                Display.getCurrent().getActiveShell(),
                "com.perforce.team.ui.dialogs.JobsPreferencesDialog",
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof JobsPreferencesDialog);
        JobsPreferencesDialog prefPage = (JobsPreferencesDialog) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("1");
        prefPage.performOk();
        dialog.close();
        JobView view = JobView.showView();
        assertNotNull(view);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        view.refreshRetrieveCount();
        view.refresh();
        while (view.isLoading()) {
            try {
                while (Display.getCurrent().readAndDispatch())
                    ;
            } catch (Exception e) {
            } catch (Error e) {
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        assertNotNull(view.getTableControl());
        assertEquals(1, view.getTableControl().getItemCount());
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
    }

    /**
     * Test the show more item
     */
    public void testShowMore() {
        IP4Connection connection = createConnection();
        assertNotNull(connection);
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                Display.getCurrent().getActiveShell(),
                "com.perforce.team.ui.dialogs.JobsPreferencesDialog",
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof JobsPreferencesDialog);
        JobsPreferencesDialog prefPage = (JobsPreferencesDialog) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("10");
        prefPage.performOk();
        dialog.close();
        JobView view = JobView.showView();
        assertNotNull(view);
        assertSame(view, JobView.getView());
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));
        view.refreshRetrieveCount();
        view.refresh();
        while (view.isLoading()) {
            try {
                while (Display.getCurrent().readAndDispatch())
                    ;
            } catch (Exception e) {
            } catch (Error e) {
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        assertNotNull(view.getTableControl());
        assertEquals(10, view.getTableControl().getItemCount());

        view.showMore();

        while (view.isLoading()) {
            try {
                while (Display.getCurrent().readAndDispatch())
                    ;
            } catch (Exception e) {
            } catch (Error e) {
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        assertEquals(20, view.getTableControl().getItemCount());

        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
    }

}
