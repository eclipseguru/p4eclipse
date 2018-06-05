/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4IntegrationOptions2;
import com.perforce.team.ui.p4java.actions.ImportProjectAction;
import com.perforce.team.ui.p4java.actions.RevertAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ProjectBasedTestCase extends ConnectionBasedTestCase {

    /**
     * Project for use by sub-classes
     */
    protected IProject project = null;

    /**
     * P4 Folder mapped to the project
     */
    protected IP4Folder projectP4Folder = null;

    /**
     * Create project
     * 
     * @throws Exception
     */
    protected void createProject() throws Exception {
        String path = getPath();
        IP4Connection connection = createConnection();
        IP4Folder folder = connection.getFolder(path);
        assertNotNull(folder);
        folder.updateLocation();
        assertNotNull(folder.getLocalPath());
        File projectFolder = new File(folder.getLocalPath());
        projectFolder.mkdirs();
        assertTrue(projectFolder.isDirectory());
        assertTrue(projectFolder.exists());
        File project = new File(projectFolder, ".project");
        String projectFile = Utils.getBundlePath("projects/.generalproject");
        assertNotNull(projectFile);
        P4CoreUtils.copyFile(new File(projectFile), project);

        IP4File p4File = connection.getFile(project.getAbsolutePath());
        assertNotNull(p4File);
        p4File.add(0);
        p4File.refresh();
        assertTrue(p4File.openedForAdd());
        assertNotNull(p4File);
        IP4PendingChangelist defaultList = connection
                .getPendingChangelist(IChangelist.DEFAULT);
        assertNotNull(defaultList);
        int submitted = defaultList.submit("adding project",
                new IP4File[] { p4File },new NullProgressMonitor());
        assertTrue(submitted > 0);
    }

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertNotNull(getPath());
        ImportProjectAction checkout = new ImportProjectAction();
        checkout.setAsync(false);
        IP4Connection connection = createConnection();
        connection.setOffline(false);
        assertFalse(connection.isOffline());
        projectP4Folder = new P4Folder(connection, null, getPath());
        assertNotNull(projectP4Folder.getClient());
        assertNotNull(projectP4Folder.getRemotePath());
        projectP4Folder.updateLocation();
        assertNotNull(projectP4Folder.getLocalPath());
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

        project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        assertNotNull(project);
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
            Utils.waitForRefresh();
        } catch (CoreException e1) {
            assertFalse(true);
        }
        assertTrue(project.exists());
        assertTrue(project.isAccessible());
        assertTrue(project.isOpen());
    }

    /**
     * @throws Exception
     * @see com.perforce.team.tests.ServerBasedTestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        try {
            RevertAction revertAction = new RevertAction();
            revertAction.setAsync(false);
            if (project != null) {
                revertAction.selectionChanged(null, new StructuredSelection(
                        project));
            } else {
                revertAction.selectionChanged(null, new StructuredSelection(
                        projectP4Folder));
            }
            revertAction.runAction(false);

            if (project != null) {
                try {
                    project.refreshLocal(IResource.DEPTH_INFINITE, null);
                    Utils.waitForRefresh();
                    project.accept(new IResourceVisitor() {

                        public boolean visit(IResource resource)
                                throws CoreException {
                            ResourceAttributes attrs = resource
                                    .getResourceAttributes();
                            if (attrs != null) {
                                attrs.setReadOnly(false);
                                try {
                                    resource.setResourceAttributes(attrs);
                                } catch (CoreException e) {
                                }
                            }
                            return true;
                        }
                    });
                    project.delete(true, true, null);
                } catch (CoreException e) {
                    assertFalse("Core exception thrown: " + e.getMessage(),
                            true);
                }
                assertFalse(project.exists());
            }
        } finally {
            super.tearDown();
        }
    }

    /**
     * Gets the depot path of the project to use locally
     * 
     * @return - depot path of project to checkout
     */
    public abstract String getPath();

    /**
     * Add file to depot
     * 
     * @param client
     * 
     * @param file
     * @throws Exception
     */
    protected void addFile(IClient client, IFile file) throws Exception {
        addFile(client, file.getLocation().makeAbsolute().toOSString());
        file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        Utils.waitForRefresh();
    }

    /**
     * Add file to depot
     * 
     * @param client
     * 
     * @param file
     * @throws Exception
     */
    protected void deleteFile(IClient client, IFile file) throws Exception {
        deleteFile(client, file.getLocation().makeAbsolute().toOSString());
        file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        Utils.waitForRefresh();
    }

    /**
     * Add file to depot and close stream.
     * 
     * @param client
     * 
     * @param file
     * @param stream
     * @throws Exception
     */
    protected void addFile(IClient client, IFile file, InputStream stream)
            throws Exception {
        addFile(client, file.getLocation().makeAbsolute().toOSString(), stream);
        try {
        	stream.close(); // close stream, and caller should not worry this.
		} catch (Exception e) {
		}

        file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        Utils.waitForRefresh();
    }

    /**
     * Add file to depot
     * 
     * @param file
     * @throws Exception
     */
    protected void addFile(IFile file) throws Exception {
        addFile(createConnection().getClient(), file);
    }

    /**
     * Add file to depot. and close stream.
     * 
     * @param file
     * @param stream
     * @throws Exception
     */
    protected void addFile(IFile file, InputStream stream) throws Exception {
        addFile(createConnection().getClient(), file.getLocation()
                .makeAbsolute().toOSString(), stream);
        try {
        	stream.close(); // close stream, and caller should not worry this.
		} catch (Exception e) {
		}
        file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        Utils.waitForRefresh();
    }

    protected P4IntegrationOptions getBaselessOptions(IP4Resource p4File, boolean baseless) {
        IServer server = p4File.getConnection().getServer();
        P4IntegrationOptions options = P4IntegrationOptions.createInstance(server);
        if(options instanceof P4IntegrationOptions2){
        	((P4IntegrationOptions2) options).setBaselessMerge(baseless);
        }
		return options;
	}

}
