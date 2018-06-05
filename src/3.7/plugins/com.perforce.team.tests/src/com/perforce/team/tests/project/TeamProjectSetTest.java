/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceProjectSetSerializer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TeamProjectSetTest extends ConnectionBasedTestCase {

    /**
     * Test importing a team project set file
     */
    public void testImportValid() {
        String name = "testImportValid";
        IProject checkExisting = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        assertFalse(checkExisting.exists());
        assertFalse(checkExisting.isAccessible());
        assertFalse(checkExisting.isOpen());
        String depotPath = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
        IP4Connection connection = createConnection();
        IP4Folder folder = connection.getFolder(depotPath);
        assertNotNull(folder);
        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.forceSync(new NullProgressMonitor());
        collection.sync("#0",new NullProgressMonitor());
        PerforceProjectSetSerializer serializer = new PerforceProjectSetSerializer();
        StringBuffer valid = new StringBuffer();
        valid.append("PORT=");
        valid.append(connection.getParameters().getPort());
        valid.append(";");
        valid.append("NAME=");
        valid.append("testImportValid");
        valid.append(";");
        valid.append("PATH=" + depotPath);
        valid.append("");
        IProject[] projects = null;
        try {
            projects = serializer.addToWorkspace(
                    new String[] { valid.toString() }, null, null, parameters);
            assertNotNull(projects);
            assertEquals(1, projects.length);
            assertTrue(projects[0].exists());
            assertEquals("testImportValid", projects[0].getName());
            String localPath = folder.getLocalPath();
            assertNotNull(localPath);
            if (P4Connection.shouldConvertPath(localPath)) {
                localPath = P4Connection.convertPath(localPath);
            }
            assertEquals(localPath, projects[0].getLocation().makeAbsolute()
                    .toOSString());
            assertNotNull(P4Workspace.getWorkspace().getConnection(projects[0]));
            assertEquals(parameters,
                    P4Workspace.getWorkspace().getConnection(projects[0])
                            .getParameters());
            IFile file = projects[0].getFile("plugin.xml");
            assertTrue(file.exists());
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        } finally {
            try {
                Utils.deleteAndRevert(projects);
            } catch (CoreException ce) {
                handle(ce);
            }
        }
    }

    /**
     * Test importing a project set to an offline connection
     */
    public void testImportOfflineConnection() {
        String name = "testImportValid";
        IProject checkExisting = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        assertFalse(checkExisting.exists());
        assertFalse(checkExisting.isAccessible());
        assertFalse(checkExisting.isOpen());
        String depotPath = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
        IP4Connection connection = createConnection();
        IP4Folder folder = connection.getFolder(depotPath);
        assertNotNull(folder);
        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.forceSync(new NullProgressMonitor());
        collection.sync("#0",new NullProgressMonitor());
        connection.setOffline(true);
        PerforceProjectSetSerializer serializer = new PerforceProjectSetSerializer();
        StringBuffer valid = new StringBuffer();
        valid.append("PORT=");
        valid.append(connection.getParameters().getPort());
        valid.append(";");
        valid.append("NAME=");
        valid.append("testImportValid");
        valid.append(";");
        valid.append("PATH=" + depotPath);
        valid.append("");
        IProject[] projects = null;
        try {
            projects = serializer.addToWorkspace(
                    new String[] { valid.toString() }, null, null, parameters);
            assertNotNull(projects);
            assertEquals(1, projects.length);
            assertTrue(projects[0].exists());
            assertEquals("testImportValid", projects[0].getName());
            String localPath = folder.getLocalPath();
            assertNotNull(localPath);
            if (P4Connection.shouldConvertPath(localPath)) {
                localPath = P4Connection.convertPath(localPath);
            }
            assertEquals(localPath, projects[0].getLocation().makeAbsolute()
                    .toOSString());
            assertEquals(connection, P4ConnectionManager.getManager()
                    .getConnection(projects[0]));
            IFile file = projects[0].getFile("plugin.xml");
            assertTrue(file.exists());
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        } finally {
            connection.setOffline(false);
            try {
                Utils.deleteAndRevert(projects);
            } catch (CoreException ce) {
                assertFalse("Core exception thrown", true);
            }
        }
    }

    /**
     * Test import with invalid name field which should fall back to container
     * name
     */
    public void testImportInvalidName() {
        String name = "com.perforce.team.plugin";
        IProject checkExisting = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        assertFalse(checkExisting.exists());
        assertFalse(checkExisting.isAccessible());
        assertFalse(checkExisting.isOpen());
        String path = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
        IP4Connection connection = createConnection();
        IP4Folder folder = connection.getFolder(path);
        assertNotNull(folder);
        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.forceSync(new NullProgressMonitor());
        collection.sync("#0",new NullProgressMonitor());
        PerforceProjectSetSerializer serializer = new PerforceProjectSetSerializer();
        StringBuffer valid = new StringBuffer();
        valid.append("PORT=");
        valid.append(connection.getParameters().getPort());
        valid.append(";");
        valid.append("NAME234=");
        valid.append("testImportValid");
        valid.append(";");
        valid.append("PATH=" + path);
        valid.append("");
        IProject[] projects = null;
        try {
            projects = serializer.addToWorkspace(
                    new String[] { valid.toString() }, null, null, parameters);
            assertNotNull(projects);
            assertEquals(1, projects.length);
            assertTrue(projects[0].exists());
            String localPath = folder.getLocalPath();
            assertNotNull(localPath);
            if (P4Connection.shouldConvertPath(localPath)) {
                localPath = P4Connection.convertPath(localPath);
            }
            assertEquals(localPath, projects[0].getLocation().makeAbsolute()
                    .toOSString());
            assertEquals(name, projects[0].getName());
            IFile file = projects[0].getFile("plugin.xml");
            assertTrue(file.exists());
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        } finally {
            try {
                Utils.deleteAndRevert(projects);
            } catch (CoreException ce) {
                assertFalse("Core exception thrown", true);
            }
        }
    }

    /**
     * Test exporting a team project set file
     */
    public void testExport() {
        String name = "com.perforce.team.core";
        IProject checkExisting = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        assertFalse(checkExisting.exists());
        assertFalse(checkExisting.isAccessible());
        assertFalse(checkExisting.isOpen());
        String path = "//depot/p08.1/p4-eclipse/com.perforce.team.core";
        IP4Connection connection = createConnection();

        IProject project = createProject(path);
        PerforceProjectSetSerializer serializer = new PerforceProjectSetSerializer();
        String[] outputs;
        IProject[] recreated = null;
        try {
            outputs = serializer.asReference(new IProject[] { project }, null,
                    null);
            assertNotNull(outputs);
            assertEquals(1, outputs.length);
            assertNotNull(outputs[0]);
            assertTrue(outputs[0].length() > 0);

            Utils.deleteAndRevert(project);
            IP4Folder folder = connection.getFolder(path);
            assertNotNull(folder);
            P4Collection collection = new P4Collection(
                    new IP4Resource[] { folder });
            collection.forceSync(new NullProgressMonitor());
            collection.sync("#0",new NullProgressMonitor());

            assertFalse(project.exists());
            recreated = serializer.addToWorkspace(outputs, null, null,
                    parameters);
            assertNotNull(recreated);
            assertEquals(1, recreated.length);
            assertTrue(recreated[0].exists());
            String localPath = folder.getLocalPath();
            assertNotNull(localPath);
            if (P4Connection.shouldConvertPath(localPath)) {
                localPath = P4Connection.convertPath(localPath);
            }
            assertEquals(localPath, recreated[0].getLocation().makeAbsolute()
                    .toOSString());
            assertEquals(name, recreated[0].getName());
            assertTrue(recreated[0].members().length > 0);
            IFile file = recreated[0].getFile("plugin.xml");
            assertTrue(file.exists());
        } catch (TeamException e) {
            e.printStackTrace();
            assertFalse("Team exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        } finally {
            if (recreated != null) {
                try {
                    Utils.deleteAndRevert(recreated);
                } catch (CoreException e) {
                    assertFalse("Core exception thrown", true);
                }
            }
        }
    }
}
