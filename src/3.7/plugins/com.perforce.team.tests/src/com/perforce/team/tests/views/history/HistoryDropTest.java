/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.history;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.history.P4HistoryPage;
import com.perforce.team.ui.history.P4HistoryPageSource;
import com.perforce.team.ui.views.DragData;
import com.perforce.team.ui.views.HistoryDropAdapter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class HistoryDropTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        for (int i = 0; i < 4; i++) {
            addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
            addFile(project.getFile("plugin.xml"));
        }
    }

    /**
     * Test invalid drops
     */
    public void testInvalidDrop() {
        IFile localFile = project.getFile("plugin.xml");
        IHistoryView view = TeamUI.showHistoryFor(
                PerforceUIPlugin.getActivePage(), localFile,
                new P4HistoryPageSource());
        assertNotNull(view);
        IHistoryPage page = view.getHistoryPage();
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);

        HistoryDropAdapter drop = new HistoryDropAdapter((P4HistoryPage) page);
        assertFalse(drop.validateDrop(null, 0, null));
        assertFalse(drop.performDrop(null));
        assertFalse(drop.doFileDrop(null));
        assertFalse(drop.doFileDrop(new String[] { null, null }));
        DragData.setConnection(null);
        assertFalse(drop.doFileDrop(new String[] { "test" }));
        DragData.setConnection(createConnection());
        assertFalse(drop.doFileDrop(new String[] { "test" }));
    }

    /**
     * Tests a resource drop on the history view
     */
    public void testResourceDrop() {
        IFile localFile = project.getFile("plugin.xml");
        assertNotNull(localFile);
        assertTrue(localFile.exists());
        IHistoryView view = TeamUI.showHistoryFor(
                PerforceUIPlugin.getActivePage(), localFile,
                new P4HistoryPageSource());
        assertNotNull(view);
        IHistoryPage page = view.getHistoryPage();
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);
        HistoryDropAdapter drop = new HistoryDropAdapter((P4HistoryPage) page);

        IP4Connection connection = createConnection();
        DragData.setConnection(connection);

        StructuredSelection selection = new StructuredSelection(localFile);
        assertTrue(drop.performDrop(selection));

        Utils.sleep(.1);

        assertNotSame(page, view.getHistoryPage());
        page = view.getHistoryPage();
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);

        while (((P4HistoryPage) page).isLoading()) {
            Utils.sleep(.1);
        }

        assertEquals(localFile.getName(), page.getName());

        TreeViewer viewer = (TreeViewer) ((P4HistoryPage) page).getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);
        assertTrue(tree.getItemCount() >= 3);
        for (TreeItem item : tree.getItems()) {
            assertNotNull(item);
            for (int i = 0; i < tree.getColumnCount(); i++) {
                assertNotNull(item.getText(i));
                assertTrue(item.getText(i).length() > 0);
            }
        }
    }

    /**
     * Test dropping an iadaptable object, job032427
     */
    public void testAdaptableDrop() {
        final IFile localFile = project
                .getFile(new Path("META-INF/MANIFEST.MF"));
        assertNotNull(localFile);
        assertTrue(localFile.exists());
        IHistoryView view = TeamUI.showHistoryFor(
                PerforceUIPlugin.getActivePage(), localFile,
                new P4HistoryPageSource());
        assertNotNull(view);
        IHistoryPage page = view.getHistoryPage();
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);
        HistoryDropAdapter drop = new HistoryDropAdapter((P4HistoryPage) page);

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

        assertNotSame(page, view.getHistoryPage());
        page = view.getHistoryPage();
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);
        while (((P4HistoryPage) page).isLoading()) {
            Utils.sleep(.1);
        }

        assertEquals(localFile.getName(), page.getName());

        TreeViewer viewer = (TreeViewer) ((P4HistoryPage) page).getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);
        assertTrue(tree.getItemCount() >= 3);
        for (TreeItem item : tree.getItems()) {
            assertNotNull(item);
            for (int i = 0; i < tree.getColumnCount(); i++) {
                assertNotNull(item.getText(i));
                assertTrue(item.getText(i).length() > 0);
            }
        }
    }

    /**
     * Tests a string path drop on the history view
     */
    public void testPathDrop() {
        IFile localFile = project.getFile("plugin.xml");
        IP4Resource resource = P4Workspace.getWorkspace()
                .getResource(localFile);
        assertNotNull(resource);
        assertNotNull(localFile);
        assertTrue(localFile.exists());
        IHistoryView view = TeamUI.showHistoryFor(
                PerforceUIPlugin.getActivePage(), localFile,
                new P4HistoryPageSource());
        assertNotNull(view);
        IHistoryPage page = view.getHistoryPage();
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);
        HistoryDropAdapter drop = new HistoryDropAdapter((P4HistoryPage) page);

        assertNotNull(resource.getConnection());
        DragData.setConnection(resource.getConnection());

        assertTrue(drop.doFileDrop(new String[] { localFile.getLocation()
                .makeAbsolute().toOSString(), }));

        Utils.sleep(.1);

        assertNotSame(page, view.getHistoryPage());
        page = view.getHistoryPage();
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);
        while (((P4HistoryPage) page).isLoading()) {
            Utils.sleep(.1);
        }

        assertEquals(localFile.getName(), page.getName());

        TreeViewer viewer = (TreeViewer) ((P4HistoryPage) page).getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);
        assertTrue(tree.getItemCount() >= 3);
        for (TreeItem item : tree.getItems()) {
            assertNotNull(item);
            for (int i = 0; i < tree.getColumnCount(); i++) {
                assertNotNull(item.getText(i));
                assertTrue(item.getText(i).length() > 0);
            }
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
