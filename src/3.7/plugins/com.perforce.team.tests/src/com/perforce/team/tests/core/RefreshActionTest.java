/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.RefreshAction;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RefreshActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IFile file = project.getFile("plugin.xml");
        IClient client = createConnection().getClient();
        addFile(client, file);
        addFile(client, project.getFile("plugin.properties"));
        addFile(client, project.getFile("build.properties"));
        addFile(client, project.getFile("about.ini"));
    }

    /**
     * Tests the refresh action
     */
    public void testRefresh() {
        final IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());
        Action wrap = Utils.getDisabledAction();
        RefreshAction refresh = new RefreshAction();
        refresh.setAsync(false);
        StructuredSelection selection = new StructuredSelection(file);
        refresh.selectionChanged(wrap, selection);
        assertTrue(wrap.isEnabled());
        IP4Resource p4Resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);
        IP4File p4File = (IP4File) p4Resource;
        p4File.revert();
        p4File.refresh();
        assertFalse(p4File.isOpened());
        assertFalse(p4File.openedForEdit());

        p4File.edit();

        assertFalse(p4File.isOpened());
        assertFalse(p4File.openedForEdit());

        refresh.run(wrap);

        assertTrue(p4File.isOpened());
        assertTrue(p4File.openedForEdit());

    }

    /**
     * Test the refresh action for files modified outside of the standard
     * p4eclipse apis
     */
    public void testOutsideRefresh() {
        IFile file1 = this.project.getFile("plugin.properties");
        IFile file2 = this.project.getFile("build.properties");
        IFile file3 = this.project.getFile("newFile.txt");
        IFile file4 = this.project.getFile("about.ini");
        try {
            Utils.fillFile(file3);
        } catch (Exception e1) {
            assertFalse("Exception creating file", true);
        }

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(new Object[] {
                file1, file2 }));
        edit.run(null);

        IP4Resource p4Resource1 = P4Workspace.getWorkspace().getResource(file1);
        assertNotNull(p4Resource1);
        assertTrue(p4Resource1 instanceof IP4File);
        IP4File p4File1 = (IP4File) p4Resource1;

        IP4Resource p4Resource2 = P4Workspace.getWorkspace().getResource(file2);
        assertNotNull(p4Resource2);
        assertTrue(p4Resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) p4Resource2;

        IP4Resource p4Resource3 = P4Workspace.getWorkspace().getResource(file3);
        assertNotNull(p4Resource3);
        assertTrue(p4Resource3 instanceof IP4File);
        IP4File p4File3 = (IP4File) p4Resource3;

        IP4Resource p4Resource4 = P4Workspace.getWorkspace().getResource(file4);
        assertNotNull(p4Resource4);
        assertTrue(p4Resource4 instanceof IP4File);
        IP4File p4File4 = (IP4File) p4Resource4;

        assertTrue(p4File1.isOpened());
        assertTrue(p4File2.isOpened());
        assertFalse(p4File3.isOpened());
        assertFalse(p4File4.isOpened());

        IP4Connection connection = createConnection();
        IClient client = connection.getClient();
        assertNotNull(client);
        assertNotNull(p4File1.getActionPath());
        assertNotNull(p4File2.getActionPath());
        assertNotNull(p4File3.getActionPath());
        assertNotNull(p4File4.getActionPath());

        List<IFileSpec> specs = P4FileSpecBuilder.makeFileSpecList(new String[] {
                p4File1.getActionPath(), p4File2.getActionPath() });
        try {
            client.revertFiles(specs, false, -1, false, false);
        } catch (P4JavaException e) {
            assertFalse("P4J exception thrown", true);
        }
        specs = P4FileSpecBuilder.makeFileSpecList(new String[] { p4File3
                .getActionPath() });
        try {
            client.addFiles(specs, false, -1, null, false);
        } catch (P4JavaException e) {
            assertFalse("P4J exception thrown", true);
        }
        specs = P4FileSpecBuilder.makeFileSpecList(new String[] { p4File4
                .getActionPath() });
        try {
            client.editFiles(specs, false, false, 0, null);
        } catch (P4JavaException e) {
            assertFalse("P4J exception thrown", true);
        }

        RefreshAction refresh = new RefreshAction();
        refresh.setAsync(false);
        refresh.selectionChanged(null, new StructuredSelection(project));
        refresh.run(null);

        assertFalse(p4File1.isOpened());
        assertFalse(p4File2.isOpened());
        p4Resource3 = P4Workspace.getWorkspace().getResource(file3);
        assertNotNull(p4Resource3);
        assertTrue(p4Resource3 instanceof IP4File);
        p4File3 = (IP4File) p4Resource3;
        assertTrue(p4File3.isOpened());
        assertTrue(p4File3.openedForAdd());
        assertTrue(p4File4.isOpened());
        assertTrue(p4File4.openedForEdit());

        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        List<IP4Resource> members = Arrays.asList(defaultList.members());
        assertFalse(members.contains(p4File1));
        assertFalse(members.contains(p4File2));
        assertTrue(members.contains(p4File3));
        assertTrue(members.contains(p4File4));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
