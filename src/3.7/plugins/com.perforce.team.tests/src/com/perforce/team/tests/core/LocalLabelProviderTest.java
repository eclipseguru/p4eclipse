/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.PerforceLabelProvider;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LocalLabelProviderTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IFile file = project.getFile("plugin.xml");
        IClient client = createConnection().getClient();
        addFile(client, file);
        addFile(client, project.getFile(new Path("src/test.txt")));
    }

    /**
     * Test file label
     */
    public void testFile() {
        PerforceLabelProvider provider = new PerforceLabelProvider(true);
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertNotNull(provider.getColumnText(resource, 0));
        assertNotNull(provider.getColumnImage(resource, 0));
    }

    /**
     * Test folder label
     */
    public void testFolderRemote() {
        PerforceLabelProvider provider = new PerforceLabelProvider(true);
        provider.setDecorateLocalFolders(false);
        IContainer folder = project.getFolder("src");
        assertTrue(folder.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(folder);
        assertNotNull(resource);
        assertNotNull(provider.getColumnText(resource, 0));
        assertNotNull(provider.getColumnImage(resource, 0));
    }

    /**
     * Test folder label
     */
    public void testFolderLocal() {
        PerforceLabelProvider provider = new PerforceLabelProvider(true);
        provider.setDecorateLocalFolders(true);
        IContainer folder = project.getFolder("src");
        assertTrue(folder.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(folder);
        assertNotNull(resource);
        assertNotNull(provider.getColumnText(resource, 0));
        assertNotNull(provider.getColumnImage(resource, 0));
    }

    /**
     * Test folder label
     */
    public void testProjectLocal() {
        PerforceLabelProvider provider = new PerforceLabelProvider(true);
        provider.setDecorateLocalFolders(true);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(project);
        assertNotNull(resource);
        assertNotNull(provider.getColumnText(resource, 0));
        assertNotNull(provider.getColumnImage(resource, 0));
        assertEquals(
                PlatformUI.getWorkbench().getSharedImages()
                        .getImage(SharedImages.IMG_OBJ_PROJECT),
                provider.getColumnImage(resource, 0));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/com.perforce.team.plugin";
    }

}
