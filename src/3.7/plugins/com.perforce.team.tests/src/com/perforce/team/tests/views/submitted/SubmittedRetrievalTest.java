/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.submitted;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.dialogs.ChangesPreferencesDialog;
import com.perforce.team.ui.views.SubmittedView;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedRetrievalTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        createJob();
        IClient client = createConnection().getClient();
        for (int i = 0; i < 160; i++) {
            addDepotFile(client, "//depot/SubmittedRetrievalTest/all.txt");
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
                "com.perforce.team.ui.dialogs.ChangesPreferencesDialog",
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof ChangesPreferencesDialog);
        ChangesPreferencesDialog prefPage = (ChangesPreferencesDialog) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("150");
        prefPage.performOk();
        dialog.close();
        SubmittedView view = SubmittedView.showView();
        assertNotNull(view);
        view.showChangelists(null);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        try {
            while (Display.getCurrent().readAndDispatch())
                ;
        } catch (Exception e) {
        } catch (Error e) {
        }

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        assertNotNull(view.getViewer());
        assertNotNull(view.getViewer().getTree());
        assertEquals(150, view.getViewer().getTree().getItemCount());
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
    }

    /**
     * Test the setting of setting retrieval preference to all jobs
     */
    public void testSetAll() {
        // IP4Connection connection = createConnection();
        // assertNotNull(connection);
        // PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
        // Display.getCurrent().getActiveShell(),
        // "com.perforce.team.ui.dialogs.ChangesPreferencesDialog",
        // new String[0], null);
        // Object page = dialog.getSelectedPage();
        // assertNotNull(page);
        // assertTrue(page instanceof ChangesPreferencesDialog);
        // ChangesPreferencesDialog prefPage = (ChangesPreferencesDialog) page;
        // prefPage.selectAllChanges();
        // prefPage.performOk();
        // dialog.close();
        // SubmittedView view = SubmittedView.showView();
        // assertNotNull(view);
        // view.showChangelists(null);
        // view.selectionChanged(null, new StructuredSelection(connection));
        //
        // try {
        // while (Display.getCurrent().readAndDispatch())
        // ;
        // } catch (Exception e) {
        // } catch (Error e) {
        // }
        //
        // while (view.isLoading()) {
        // try {
        // while (Display.getCurrent().readAndDispatch())
        // ;
        // } catch (Exception e) {
        // } catch (Error e) {
        // }
        // try {
        // Thread.sleep(100);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        //
        // assertNotNull(view.getViewer());
        // assertNotNull(view.getViewer().getTree());
        // assertTrue(view.getViewer().getTree().getItemCount() > 1);
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
        // .hideView(view);
    }

    /**
     * Test the setting of max job retrievals to 1
     */
    public void testSet1() {
        IP4Connection connection = createConnection();
        assertNotNull(connection);
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                Display.getCurrent().getActiveShell(),
                "com.perforce.team.ui.dialogs.ChangesPreferencesDialog",
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof ChangesPreferencesDialog);
        ChangesPreferencesDialog prefPage = (ChangesPreferencesDialog) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("1");
        prefPage.performOk();
        dialog.close();
        SubmittedView view = SubmittedView.showView();
        assertNotNull(view);
        view.showChangelists(null);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        try {
            while (Display.getCurrent().readAndDispatch())
                ;
        } catch (Exception e) {
        } catch (Error e) {
        }

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        assertNotNull(view.getViewer());
        assertNotNull(view.getViewer().getTree());
        assertEquals(1, view.getViewer().getTree().getItemCount());
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
                "com.perforce.team.ui.dialogs.ChangesPreferencesDialog",
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof ChangesPreferencesDialog);
        ChangesPreferencesDialog prefPage = (ChangesPreferencesDialog) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("10");
        prefPage.performOk();
        dialog.close();
        SubmittedView view = SubmittedView.showView();
        assertNotNull(view);
        view.showChangelists(null);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        try {
            while (Display.getCurrent().readAndDispatch())
                ;
        } catch (Exception e) {
        } catch (Error e) {
        }

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        assertNotNull(view.getViewer().getTree());
        assertEquals(10, view.getViewer().getTree().getItemCount());
        view.showMore();

        try {
            while (Display.getCurrent().readAndDispatch())
                ;
        } catch (Exception e) {
        } catch (Error e) {
        }

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        assertEquals(20, view.getViewer().getTree().getItemCount());

        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .hideView(view);
    }

}
