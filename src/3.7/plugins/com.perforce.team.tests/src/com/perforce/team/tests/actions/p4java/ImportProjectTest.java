/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.p4java.actions.ImportProjectAction;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ImportProjectTest extends ConnectionBasedTestCase {

    /**
     * Tests importing a project not in the client view
     */
    public void testImportFailed() {
        String path = "//bad_path/not_in_depot";
        ImportProjectAction checkout = new ImportProjectAction();
        IP4Connection connection = new P4Connection(parameters);
        connection.setOffline(false);
        connection.login(parameters.getPassword());
        connection.connect();
        assertFalse(connection.isOffline());
        assertTrue(connection.isConnected());
        IP4Folder projectP4Folder = new P4Folder(connection, null, path);
        assertNotNull(projectP4Folder.getClient());
        assertNotNull(projectP4Folder.getRemotePath());
        projectP4Folder.updateLocation();
        assertNull(projectP4Folder.getLocalPath());
        StructuredSelection selection = new StructuredSelection(projectP4Folder);
        Action wrapAction = new Action() {
        };
        wrapAction.setEnabled(false);
        checkout.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());

        checkout.runAction(new NullProgressMonitor(), false);

        String name = projectP4Folder.getName();

        P4Collection collection = new P4Collection(
                new IP4Resource[] { projectP4Folder });
        collection.forceSync(new NullProgressMonitor());

        IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        assertNotNull(project);
        assertFalse(project.exists());
        assertFalse(project.isAccessible());
        assertFalse(project.isOpen());
        assertNull(P4Workspace.getWorkspace().getConnection(project));
    }

    /**
     * Test that a project is successfully managed
     */
    public void testProjectManaged() {
        IProject project = null;
        try {
            String path = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
            ImportProjectAction checkout = new ImportProjectAction();
            checkout.setAsync(false);
            IP4Connection connection = new P4Connection(parameters);
            connection.setOffline(false);
            connection.login(parameters.getPassword());
            connection.connect();
            assertFalse(connection.isOffline());
            assertTrue(connection.isConnected());
            IP4Folder projectP4Folder = new P4Folder(connection, null, path);
            assertNotNull(projectP4Folder.getClient());
            assertNotNull(projectP4Folder.getRemotePath());
            projectP4Folder.updateLocation();
            assertNotNull(projectP4Folder.getLocalPath());
            StructuredSelection selection = new StructuredSelection(
                    projectP4Folder);
            Action wrapAction = new Action() {
            };
            wrapAction.setEnabled(false);
            checkout.selectionChanged(wrapAction, selection);
            assertTrue(wrapAction.isEnabled());

            checkout.runAction(new NullProgressMonitor(), false);

            String name = projectP4Folder.getName();

            P4Collection collection = new P4Collection(
                    new IP4Resource[] { projectP4Folder });
            collection.forceSync(new NullProgressMonitor());

            project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
            assertNotNull(project);
            assertTrue(project.exists());
            assertTrue(project.isAccessible());
            assertTrue(project.isOpen());
            assertNotNull(PerforceProviderPlugin
                    .getPerforceProviderFor(project));
            assertNotNull(PerforceTeamProvider.getProvider(project));
            assertTrue(PerforceTeamProvider.isShared(project));
            assertNotNull(P4Workspace.getWorkspace().getConnection(project));
        } finally {
            if (project != null) {
                try {
                    project.delete(true, true, null);
                } catch (CoreException e) {
                    assertFalse("Core exception thrown: " + e.getMessage(),
                            true);
                }
            }
        }
    }

}
