/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.history;

import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.editor.P4CompareEditorInput;
import com.perforce.team.ui.history.P4HistoryPage;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class HistoryTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        for (int i = 0; i < 4; i++) {
            addFile(project.getFile("plugin.xml"), new ByteArrayInputStream(
                    ("<plugin abc=\"" + i + "\"></plugin>").getBytes()));
        }
    }

    /**
     * Basic test of the history view
     */
    public void testHistory() {
        IHistoryView view = TeamUI.getHistoryView();
        assertNotNull(view);
        IFile file = project.getFile("plugin.xml");
        assertNotNull(file);
        assertTrue(file.exists());
        IHistoryPage page = view.showHistoryFor(file, true);
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);
        P4HistoryPage p4Page = (P4HistoryPage) page;

        while (p4Page.isLoading()) {
            Utils.sleep(.1);
        }

        TreeViewer viewer = (TreeViewer) p4Page.getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);
        assertTrue(tree.getItemCount() >= 3);
        for (TreeItem item : tree.getItems()) {
            assertNotNull(item);
            for (int i = 0; i < 7; i++) {
                assertNotNull(item.getText(i));
                assertTrue(item.getText(i).length() > 0);
            }
        }
    }

    /**
     * Test comparing old revisions
     */
    public void testCompare() {
        IHistoryView view = TeamUI.getHistoryView();
        assertNotNull(view);
        IFile file = project.getFile("plugin.xml");
        assertNotNull(file);
        assertTrue(file.exists());
        IHistoryPage page = view.showHistoryFor(file, true);
        assertNotNull(page);
        assertTrue(page instanceof P4HistoryPage);
        P4HistoryPage p4Page = (P4HistoryPage) page;

        while (p4Page.isLoading()) {
            Utils.sleep(.1);
        }
        TreeViewer viewer = (TreeViewer) p4Page.getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);
        assertTrue(tree.getItemCount() >= 3);
        Object d1 = tree.getItem(0).getData();
        assertNotNull(d1);
        Object d2 = tree.getItem(1).getData();
        assertNotNull(d2);
        viewer.setSelection(new StructuredSelection(new Object[] { d1, d2 }));
        p4Page.compareSelection(false);

        Utils.sleep(.1);

        IEditorReference[] refs = PerforceUIPlugin.getActivePage()
                .getEditorReferences();
        assertNotNull(refs);

        assertTrue(refs.length > 0);
        boolean compareFound = false;
        for (int i = 0; i < refs.length; i++) {
            if ("org.eclipse.compare.CompareEditor".equals(refs[i].getId())) {
                try {
                    if (P4CompareEditorInput.class.equals(refs[i]
                            .getEditorInput().getClass())) {
                        P4CompareEditorInput input = (P4CompareEditorInput) refs[i]
                                .getEditorInput();
                        assertNotNull(input.getLeft());
                        assertNotNull(input.getRight());
                        compareFound = true;
                        break;
                    }
                } catch (PartInitException e) {
                }
            }
        }
        assertTrue(compareFound);
        assertTrue(PerforceUIPlugin.getActivePage().closeEditors(refs, false));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
