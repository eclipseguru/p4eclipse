/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.history;

import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.history.P4HistoryPage;
import com.perforce.team.ui.p4java.actions.ShowHistoryAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class HistoryActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
        for (int i = 0; i < 4; i++) {
            addFile(project.getFile("plugin.xml"));
        }
    }

    /**
     * Enable for single action selection of either a folder or a file
     */
    public void testEnablement() {
        Action wrapAction = Utils.getDisabledAction();
        ShowHistoryAction showHistory = new ShowHistoryAction();
        showHistory.setAsync(false);

        IFolder folder = this.project.getFolder("META-INF");
        assertTrue(folder.exists());

        showHistory.selectionChanged(wrapAction,
                new StructuredSelection(folder));
        assertTrue(wrapAction.isEnabled());

        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());
        showHistory.selectionChanged(wrapAction, new StructuredSelection(file));
        assertTrue(wrapAction.isEnabled());

        showHistory.selectionChanged(wrapAction, new StructuredSelection(
                new Object[] { folder, file }));
        assertFalse(wrapAction.isEnabled());
    }

    /**
     * Test the show history action
     */
    public void testShowHistory() {
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());
        StructuredSelection selection = new StructuredSelection(file);
        Action wrapAction = Utils.getDisabledAction();
        ShowHistoryAction showHistory = new ShowHistoryAction();
        showHistory.setAsync(false);
        showHistory.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());
        showHistory.run(wrapAction);

        IHistoryPage page = TeamUI.getHistoryView().getHistoryPage();
        assertTrue(page instanceof P4HistoryPage);

        P4HistoryPage p4Page = (P4HistoryPage) page;

        while (p4Page.isLoading()) {
            Utils.sleep(.1);
        }
        assertTrue(p4Page.getViewer() instanceof TreeViewer);
        assertTrue(((TreeViewer) p4Page.getViewer()).getTree().getItemCount() >= 3);
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
