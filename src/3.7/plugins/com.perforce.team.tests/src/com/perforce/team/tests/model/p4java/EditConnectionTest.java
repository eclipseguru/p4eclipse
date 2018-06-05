/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.P4ConnectionManager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditConnectionTest extends ProjectBasedTestCase {

    /**
     * Test editing a connection
     */
    public void testEdit1() {
        IP4Connection old = P4ConnectionManager.getManager().getConnection(
                project);
        assertNotNull(old);

        int previous = P4ConnectionManager.getManager().getConnections().length;

        ConnectionParameters newParams = new ConnectionParameters();
        newParams.setUser("diff_user");
        newParams.setPort("diffport:1234");
        newParams.setClient("diff_client");
        try {
            P4ConnectionManager.getManager().editConnection(old, newParams);
            IP4Connection newConnection = P4ConnectionManager.getManager()
                    .getConnection(project);
            assertNotNull(newConnection);
            assertNotSame(old, newConnection);

            int current = P4ConnectionManager.getManager().getConnections().length;
            assertSame(previous, current);

            assertFalse(P4Workspace.getWorkspace().hasMappedProjects(old));
            IProject[] oldMapped = P4Workspace.getWorkspace()
                    .getMappedProjects(old);
            assertNotNull(oldMapped);
            assertEquals(0, oldMapped.length);

            assertTrue(P4Workspace.getWorkspace().hasMappedProjects(
                    newConnection));
            IProject[] newMapped = P4Workspace.getWorkspace()
                    .getMappedProjects(newConnection);
            assertNotNull(newMapped);
            assertEquals(1, newMapped.length);
            assertEquals(project, newMapped[0]);

            assertEquals(newParams, newConnection.getParameters());
            assertSame(old.getErrorHandler(), newConnection.getErrorHandler());
        } finally {
            P4ConnectionManager.getManager().removeConnection(newParams);
        }

    }

    /**
     * Test editing a connection
     */
    public void testEdit2() {
        ConnectionParameters newParams = new ConnectionParameters();
        newParams.setUser("diff_user");
        newParams.setPort("diffport:4321");
        newParams.setClient("diff_client");

        IProject project2 = ResourcesPlugin.getWorkspace().getRoot()
                .getProject("EditConnectionTest_testEdit2");
        try {
            assertFalse(project2.exists());
            try {
                project2.create(null);
                project2.open(null);
            } catch (CoreException e) {
                assertFalse("Core exception thrown", true);
            }
            assertTrue(project2.exists());
            assertTrue(project2.isOpen());

            assertTrue(PerforceProviderPlugin.manageProject(project2,
                    parameters));

            IP4Connection old = P4ConnectionManager.getManager().getConnection(
                    project);
            assertNotNull(old);

            int previousMapped = P4Workspace.getWorkspace().getMappedProjects(
                    old).length;
            assertEquals(2, previousMapped);

            int previous = P4ConnectionManager.getManager().getConnections().length;

            P4ConnectionManager.getManager().editConnection(old, newParams);
            IP4Connection newConnection = P4ConnectionManager.getManager()
                    .getConnection(project);
            assertNotNull(newConnection);
            assertNotSame(old, newConnection);

            int current = P4ConnectionManager.getManager().getConnections().length;
            assertSame(previous, current);

            assertFalse(P4Workspace.getWorkspace().hasMappedProjects(old));
            IProject[] oldMapped = P4Workspace.getWorkspace()
                    .getMappedProjects(old);
            assertNotNull(oldMapped);
            assertEquals(0, oldMapped.length);

            assertTrue(P4Workspace.getWorkspace().hasMappedProjects(
                    newConnection));
            IProject[] newMapped = P4Workspace.getWorkspace()
                    .getMappedProjects(newConnection);
            assertNotNull(newMapped);
            assertEquals(2, newMapped.length);
            boolean project1Found = false;
            boolean project2Found = false;
            for (IProject newProject : newMapped) {
                if (newProject.equals(project)) {
                    project1Found = true;
                } else if (newProject.equals(project2)) {
                    project2Found = true;
                }
            }
            assertTrue(project1Found);
            assertTrue(project2Found);

            assertEquals(newParams, newConnection.getParameters());
            assertSame(old.getErrorHandler(), newConnection.getErrorHandler());
        } finally {
            P4ConnectionManager.getManager().removeConnection(newParams);
            if (project2 != null && project2.exists()) {
                try {
                    project2.delete(true, true, null);
                } catch (CoreException e) {
                    assertFalse("Core exception thrown", true);
                }
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
