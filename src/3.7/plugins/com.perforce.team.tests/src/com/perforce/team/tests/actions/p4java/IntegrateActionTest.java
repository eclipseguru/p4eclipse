/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4IntegrationOptions2;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.IntegrateAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        addFile(client, project.getFile("about.ini"));
        addFile(client, project.getFile("plugin.xml"));
        addFile(client, project.getFile("plugin.properties"));
    }

    /**
     * Test the integrate action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        IntegrateAction integrate = new IntegrateAction();
        assertFalse(wrap.isEnabled());
        IFile file = project.getFile("about2.ini");
        assertFalse(file.exists());
        integrate.selectionChanged(wrap, new StructuredSelection(file));
        assertFalse(wrap.isEnabled());
        file = project.getFile("about.ini");
        integrate.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Tests the integrate action
     */
    public void testAction() {
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        IP4File newFile = null;
        try {
            Action wrap = Utils.getDisabledAction();
            IntegrateAction integrate = new IntegrateAction();
            integrate.setAsync(false);
            integrate.selectionChanged(wrap, new StructuredSelection(file));
            assertTrue(wrap.isEnabled());

            String source = p4File.getActionPath();
            String target = "//depot/dev/testtest/about.ini";

            integrate.integrate(source, target, null, null, 0, P4IntegrationOptions.createInstance(p4File.getServer()));

            IP4PendingChangelist changelist = p4File.getConnection()
                    .getPendingChangelist(0);
            assertNotNull(changelist);
            changelist.refresh();
            IP4Resource[] members = changelist.members();
            assertNotNull(members);
            assertTrue(members.length > 0);
            for (IP4Resource member : members) {
                if (target.equals(member.getRemotePath())
                        && member instanceof IP4File) {
                    newFile = (IP4File) member;
                    break;
                }
            }
            assertNotNull(newFile);
            assertTrue(newFile.isOpened());
            assertTrue(newFile.openedForAdd());
        } finally {
            if (newFile != null) {
                newFile.revert();
                newFile.refresh();
                assertFalse(newFile.isOpened());
            }
        }
    }

    /**
     * Tests integrating a file over top an existing file
     */
    public void testIntegratedOver() {
        IFile from = project.getFile("plugin.xml");
        assertTrue(from.exists());
        IFile to = project.getFile("plugin.properties");
        assertTrue(to.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(from);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());

        IP4Resource toResource = P4Workspace.getWorkspace().getResource(to);
        assertNotNull(toResource);
        assertTrue(toResource instanceof IP4File);
        IP4File toP4File = (IP4File) toResource;
        assertFalse(toP4File.isOpened());

        try {
            Action wrap = Utils.getDisabledAction();
            IntegrateAction integrate = new IntegrateAction();
            integrate.setAsync(false);
            integrate.selectionChanged(wrap, new StructuredSelection(from));
            assertTrue(wrap.isEnabled());

            String source = p4File.getActionPath();
            String target = toP4File.getActionPath();

            IServer server = p4File.getConnection().getServer();
            P4IntegrationOptions options = P4IntegrationOptions.createInstance(server);
            if(options instanceof P4IntegrationOptions2){
            	((P4IntegrationOptions2) options).setBaselessMerge(true);
            }
            integrate.integrate(source, target, null, null, 0, options);
            
            assertFalse(p4File.isOpened());
            assertNull(p4File.getChangelist());
            
            assertTrue(toP4File.isOpened());
            assertTrue(toP4File.openedForEdit());
            assertEquals(FileAction.INTEGRATE, toP4File.getAction());
            assertNotNull(toP4File.getChangelist());
            assertEquals(0, toP4File.getChangelistId());
        } finally {
            if (toP4File != null) {
                toP4File.revert();
                toP4File.refresh();
                assertFalse(toP4File.isOpened());
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
