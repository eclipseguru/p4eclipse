/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.pending;

import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.views.PendingView;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingViewMissingTest extends ProjectBasedTestCase {

    /**
     * Test refresh pending view and it refreshing file information for file
     * that are no longer opened because they were changed outside of p4eclipse
     */
    public void testOutsideRevertedDetection() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        PendingView view = PendingView.showView();
        view.showOtherChanges(false);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

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

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);

        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertFalse(p4File.isOpened());
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        assertTrue(p4File.isOpened());
        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertTrue(Arrays.asList(defaultList.members()).contains(p4File));

        try {
            connection.getClient().revertFiles(
                    P4FileSpecBuilder.makeFileSpecList(new String[] { p4File
                            .getLocalPath() }), false, -1, false, false);
        } catch (Exception e) {
            assertFalse("Exception thrown reverting", false);
        }

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

        assertFalse(p4File.isOpened());
        defaultList = connection.getPendingChangelist(0);
        assertFalse(Arrays.asList(defaultList.members()).contains(p4File));
    }

    /**
     * Test refresh pending view and it refreshing file information for file
     * that are no longer opened because they were changed outside of p4eclipse
     */
    public void testOutsideEditDetection() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        PendingView view = PendingView.showView();
        view.showOtherChanges(false);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

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

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        Tree tree = viewer.getTree();
        assertNotNull(tree);

        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertFalse(p4File.isOpened());

        try {
            connection.getClient().editFiles(
                    P4FileSpecBuilder.makeFileSpecList(new String[] { p4File
                            .getLocalPath() }), false, false, -1, null);
        } catch (Exception e) {
            assertFalse("Exception thrown reverting", false);
        }

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

        assertTrue(p4File.isOpened());
        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertTrue(Arrays.asList(defaultList.members()).contains(p4File));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
