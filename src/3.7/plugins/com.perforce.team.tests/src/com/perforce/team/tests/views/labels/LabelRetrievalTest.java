/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.labels;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.labels.LabelsPreferencePage;
import com.perforce.team.ui.labels.LabelsView;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelRetrievalTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        for (int i = 0; i < 150; i++) {
            createLabel();
        }
    }

    /**
     * Test the setting of max job retrievals to 140
     */
    public void testSet140() {
        IP4Connection connection = createConnection();
        assertNotNull(connection);
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                Display.getCurrent().getActiveShell(), LabelsPreferencePage.ID,
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof LabelsPreferencePage);
        LabelsPreferencePage prefPage = (LabelsPreferencePage) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("140");
        prefPage.performOk();
        dialog.close();
        LabelsView view = LabelsView.showView();
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
        assertNotNull(view.getPerforceViewControl().getTableControl());
        assertEquals(140, view.getPerforceViewControl().getTableControl().getItemCount());
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
                Display.getCurrent().getActiveShell(), LabelsPreferencePage.ID,
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof LabelsPreferencePage);
        LabelsPreferencePage prefPage = (LabelsPreferencePage) page;
        prefPage.selectAllElements();
        prefPage.performOk();
        dialog.close();
        LabelsView view = LabelsView.showView();
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
        assertNotNull(view.getPerforceViewControl().getTableControl());
        assertTrue(view.getPerforceViewControl().getTableControl().getItemCount() > 0);
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
                Display.getCurrent().getActiveShell(), LabelsPreferencePage.ID,
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof LabelsPreferencePage);
        LabelsPreferencePage prefPage = (LabelsPreferencePage) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("1");
        prefPage.performOk();
        dialog.close();
        LabelsView view = LabelsView.showView();
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
        assertNotNull(view.getPerforceViewControl().getTableControl());
        assertEquals(1, view.getPerforceViewControl().getTableControl().getItemCount());
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
                Display.getCurrent().getActiveShell(), LabelsPreferencePage.ID,
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof LabelsPreferencePage);
        LabelsPreferencePage prefPage = (LabelsPreferencePage) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("10");
        prefPage.performOk();
        dialog.close();
        LabelsView view = LabelsView.showView();
        assertNotNull(view);
        assertSame(view, LabelsView.getView());
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
        assertNotNull(view.getPerforceViewControl().getTableControl());
        assertEquals(10, view.getPerforceViewControl().getTableControl().getItemCount());

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

        assertEquals(20, view.getPerforceViewControl().getTableControl().getItemCount());

        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
    }

}
