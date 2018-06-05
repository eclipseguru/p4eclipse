/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.submitted;

import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.dialogs.ChangesPreferencesDialog;
import com.perforce.team.ui.views.DragData;
import com.perforce.team.ui.views.SubmittedDropAdapter;
import com.perforce.team.ui.views.SubmittedView;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedDropTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < 3; i++) {
            addFile(project.getFile("plugin.xml"));
            addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
        }
    }

    /**
     * Test invalid drops
     */
    public void testInvalidDrop() {
        SubmittedDropAdapter drop = new SubmittedDropAdapter(
                SubmittedView.showView().getPerforceViewControl());
        assertFalse(drop.validateDrop(null, 0, null));
        assertFalse(drop.performDrop(null));
        assertFalse(drop.doFileDrop(new String[] { null, null }));
        DragData.setConnection(null);
        assertFalse(drop.doFileDrop(new String[] { "test" }));
    }

    /**
     * Tests a resource drop on the history view
     */
    public void testResourceDrop() {
        SubmittedView view = SubmittedView.showView();
        SubmittedDropAdapter drop = new SubmittedDropAdapter(view.getPerforceViewControl());

        IFile localFile = project.getFile("plugin.xml");
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

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree table = viewer.getTree();
        assertNotNull(table);
        assertTrue(table.getItemCount() >= 3);
        assertNotNull(view.getChangelists());
        assertTrue(view.getChangelists().length >= 3);
        for (IP4SubmittedChangelist list : view.getChangelists()) {
            assertNotNull(list);
            assertNotNull(list.getChangelist());
            assertEquals(ChangelistStatus.SUBMITTED, list.getStatus());
        }
    }

    /**
     * Tests a resource adaptable drop on the history view
     */
    public void testAdaptableDrop() {
        SubmittedView view = SubmittedView.showView();
        SubmittedDropAdapter drop = new SubmittedDropAdapter(view.getPerforceViewControl());

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

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree table = viewer.getTree();
        assertNotNull(table);
        assertTrue(table.getItemCount() >= 3);
        assertNotNull(view.getChangelists());
        assertTrue(view.getChangelists().length >= 3);
        for (IP4SubmittedChangelist list : view.getChangelists()) {
            assertNotNull(list);
            assertNotNull(list.getChangelist());
            assertEquals(ChangelistStatus.SUBMITTED, list.getStatus());
        }
    }

    /**
     * Tests a string path drop on the history view
     */
    public void testPathDrop() {
        SubmittedView view = SubmittedView.showView();
        SubmittedDropAdapter drop = new SubmittedDropAdapter(view.getPerforceViewControl());

        IFile localFile = project.getFile("plugin.xml");
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

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree table = viewer.getTree();
        assertNotNull(table);
        assertTrue(table.getItemCount() >= 3);
        assertNotNull(view.getChangelists());
        assertTrue(view.getChangelists().length >= 3);
        for (IP4SubmittedChangelist list : view.getChangelists()) {
            assertNotNull(list);
            assertNotNull(list.getChangelist());
            assertEquals(ChangelistStatus.SUBMITTED, list.getStatus());
        }
    }

    /**
     * Tests a string path drop on the history view
     */
    public void testDepotDrop() {
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                Display.getCurrent().getActiveShell(),
                "com.perforce.team.ui.dialogs.ChangesPreferencesDialog",
                new String[0], null);
        Object page = dialog.getSelectedPage();
        assertNotNull(page);
        assertTrue(page instanceof ChangesPreferencesDialog);
        ChangesPreferencesDialog prefPage = (ChangesPreferencesDialog) page;
        prefPage.selectMaxElements();
        prefPage.setMaxElements("100");
        prefPage.performOk();
        dialog.close();

        SubmittedView view = SubmittedView.showView();
        SubmittedDropAdapter drop = new SubmittedDropAdapter(view.getPerforceViewControl());

        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(project);

        String path = "//depot";

        DragData.setConnection(connection);

        assertTrue(drop.doFileDrop(new String[] { path, }));

        Utils.sleep(.1);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree table = viewer.getTree();
        assertNotNull(table);
        assertEquals(6, table.getItemCount());
        assertNotNull(view.getChangelists());
        assertEquals(6, view.getChangelists().length);
        for (IP4SubmittedChangelist list : view.getChangelists()) {
            assertNotNull(list);
            assertNotNull(list.getChangelist());
            assertEquals(ChangelistStatus.SUBMITTED, list.getStatus());
        }
    }

    /**
     * Tests dropping a project
     */
    public void testProjectDrop() {
        SubmittedView view = SubmittedView.showView();
        SubmittedDropAdapter drop = new SubmittedDropAdapter(view.getPerforceViewControl());

        IP4Connection connection = createConnection();
        DragData.setConnection(connection);

        StructuredSelection selection = new StructuredSelection(project);
        assertTrue(drop.performDrop(selection));

        Utils.sleep(.1);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree table = viewer.getTree();
        assertNotNull(table);
        assertTrue(table.getItemCount() >= 4);
        assertNotNull(view.getChangelists());
        assertTrue(view.getChangelists().length >= 4);
        for (IP4SubmittedChangelist list : view.getChangelists()) {
            assertNotNull(list);
            assertNotNull(list.getChangelist());
            assertEquals(ChangelistStatus.SUBMITTED, list.getStatus());
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
