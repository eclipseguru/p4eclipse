/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.jobs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.dialogs.JobsPreferencesDialog;
import com.perforce.team.ui.views.DragData;
import com.perforce.team.ui.views.JobView;
import com.perforce.team.ui.views.JobsDropAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobDropTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        createJob();
        createJob();
        addFile(project.getFile("about.ini"));
        addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
        IP4Connection connection = createConnection();
        for (IP4SubmittedChangelist submitted : connection
                .getSubmittedChangelists()) {
            new P4Collection(connection.getJobs()).fix(submitted);
        }
    }

    /**
     * Test invalid drops
     */
    public void testInvalidDrop() {
        JobsDropAdapter drop = new JobsDropAdapter(JobView.showView().getPerforceViewControl());
        assertFalse(drop.validateDrop(null, 0, null));
        assertFalse(drop.performDrop(null));
        assertFalse(drop.doFileDrop(null));
        assertFalse(drop.doFileDrop(new String[] { null, null }));
        DragData.setConnection(null);
        assertFalse(drop.doFileDrop(new String[] { "test" }));
    }

    /**
     * Tests a resource drop on the history view
     */
    public void testResourceDrop() {
        JobView view = JobView.showView();
        JobsDropAdapter drop = new JobsDropAdapter(view.getPerforceViewControl());

        IFile localFile = project.getFile("about.ini");
        assertNotNull(localFile);
        assertTrue(localFile.exists());

        IP4Connection connection = createConnection();
        DragData.setConnection(connection);

        StructuredSelection selection = new StructuredSelection(localFile);
        assertTrue(drop.performDrop(selection));

        Utils.sleep(.1);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }
        Table table = view.getJobsDialog().getTableControl();
        assertNotNull(table);
        assertTrue(table.getItemCount() >= 1);
        for (TableItem item : table.getItems()) {
            Object data = item.getData();
            assertNotNull(data);
            assertTrue(data instanceof IP4Job);
        }
    }

    /**
     * Tests a resource adaptable drop on the history view
     */
    public void testAdaptableDrop() {
        JobView view = JobView.showView();
        JobsDropAdapter drop = new JobsDropAdapter(view.getPerforceViewControl());

        final IFile localFile = project
                .getFile(new Path("META-INF/MANIFEST.MF"));
        assertNotNull(localFile);
        assertTrue(localFile.exists());

        IP4Connection connection = createConnection();
        DragData.setConnection(connection);

        IAdaptable adaptable = new IAdaptable() {

            public Object getAdapter(Class adapter) {
                if (IResource.class.equals(adapter)) {
                    return localFile;
                }
                return null;
            }

        };

        StructuredSelection selection = new StructuredSelection(adaptable);
        assertTrue(drop.performDrop(selection));

        Utils.sleep(.1);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }
        Table table = view.getJobsDialog().getTableControl();
        assertNotNull(table);
        assertTrue(table.getItemCount() >= 1);
        for (TableItem item : table.getItems()) {
            Object data = item.getData();
            assertNotNull(data);
            assertTrue(data instanceof IP4Job);
        }
    }

    /**
     * Tests a string path drop on the history view
     */
    public void testPathDrop() {
        JobView view = JobView.showView();
        JobsDropAdapter drop = new JobsDropAdapter(view.getPerforceViewControl());

        IFile localFile = project.getFile("about.ini");
        IP4Resource resource = P4Workspace.getWorkspace()
                .getResource(localFile);
        assertNotNull(resource);
        assertNotNull(localFile);
        assertTrue(localFile.exists());

        assertNotNull(resource.getConnection());
        DragData.setConnection(resource.getConnection());

        assertTrue(drop.doFileDrop(new String[] { localFile.getLocation()
                .makeAbsolute().toOSString(), }));

        Utils.sleep(.1);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }
        Table table = view.getJobsDialog().getTableControl();
        assertNotNull(table);
        assertTrue(table.getItemCount() >= 1);
        for (TableItem item : table.getItems()) {
            Object data = item.getData();
            assertNotNull(data);
            assertTrue(data instanceof IP4Job);
        }
    }

    /**
     * Test a drop using a depot path
     */
    public void testDepotDrop() {
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
        JobsDropAdapter drop = new JobsDropAdapter(view.getPerforceViewControl());

        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(project);

        String path = "//depot";

        DragData.setConnection(connection);

        assertTrue(drop.doFileDrop(new String[] { path, }));

        Utils.sleep(.1);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        Table table = view.getJobsDialog().getTableControl();
        assertNotNull(table);
        assertEquals(2, table.getItemCount());
        for (TableItem item : table.getItems()) {
            Object data = item.getData();
            assertNotNull(data);
            assertTrue(data instanceof IP4Job);
        }
    }

    /**
     * Tests dropping a project
     */
    public void testProjectDrop() {
        JobView view = JobView.showView();
        JobsDropAdapter drop = new JobsDropAdapter(view.getPerforceViewControl());

        IP4Connection connection = createConnection();
        DragData.setConnection(connection);

        StructuredSelection selection = new StructuredSelection(project);
        assertTrue(drop.performDrop(selection));

        Utils.sleep(.1);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }
        Table table = view.getJobsDialog().getTableControl();
        assertNotNull(table);
        assertTrue(table.getItemCount() >= 1);
        for (TableItem item : table.getItems()) {
            Object data = item.getData();
            assertNotNull(data);
            assertTrue(data instanceof IP4Job);
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
