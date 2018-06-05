/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.IgnoredFiles;
import com.perforce.team.ui.p4java.actions.AddAction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IgnoredFilesTest extends ProjectBasedTestCase {

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
        addFile(client, project.getFile("build.properties"));
        addFile(client, project.getFile("plugin.properties"));
    }

    private void setIgnoreContents(String content) {
        if (content != null) {
            IFile ignore = project.getFile(".p4ignore");
            if (ignore.exists()) {
                try {
                    ignore.setContents(
                            new ByteArrayInputStream(content.getBytes()),
                            IResource.FORCE, null);
                } catch (CoreException e) {
                    assertFalse("Core exception thrown", true);
                }
            } else {
                try {
                    ignore.create(new ByteArrayInputStream(content.getBytes()),
                            IResource.FORCE, null);
                } catch (CoreException e) {
                    assertFalse("Core exception thrown", true);
                }
            }
        }
    }

    /**
     * Tests an ignored file
     */
    public void testFileIgnore() {
        setIgnoreContents("");
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        assertFalse(IgnoredFiles.isIgnored(file));
        setIgnoreContents("images\nplugin.xml\nsrc");
        assertTrue(IgnoredFiles.isIgnored(file));
    }

    /**
     * Test folder ignore
     */
    public void testFolderIgnore() {
        setIgnoreContents("");
        IFolder folder = project.getFolder("src");
        assertTrue(folder.exists());
        assertFalse(IgnoredFiles.isIgnored(folder));
        setIgnoreContents("images\nplugin.xml\nsrc");
        assertTrue(IgnoredFiles.isIgnored(folder));
    }

    /**
     * Tests * ignoring of a file
     */
    public void testIgnoreCharacterFile() {
        setIgnoreContents("");
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        assertFalse(IgnoredFiles.isIgnored(file));
        setIgnoreContents("image*\n*.xml\n");
        assertTrue(IgnoredFiles.isIgnored(file));
    }

    /**
     * Tests * ignorning of a folder
     */
    public void testIgnoreCharacterFolder() {
        setIgnoreContents("");
        IFolder folder = project.getFolder("src");
        assertTrue(folder.exists());
        assertFalse(IgnoredFiles.isIgnored(folder));
        setIgnoreContents("images\nplugin.xml\nsr*");
        assertTrue(IgnoredFiles.isIgnored(folder));
    }

    /**
     * Tests ? ignoring of a file
     */
    public void testIgnoreWildcardFile() {
        setIgnoreContents("");
        IFile file = project.getFile("plugin.properties");
        assertTrue(file.exists());
        assertFalse(IgnoredFiles.isIgnored(file));
        setIgnoreContents("pl?gin.properties\t\n*.xml");
        assertTrue(IgnoredFiles.isIgnored(file));
    }

    /**
     * Tests ? ignoring of a folder
     */
    public void testIgnoreWildcardFolder() {
        setIgnoreContents("");
        IFolder folder = project.getFolder("src");
        assertTrue(folder.exists());
        assertFalse(IgnoredFiles.isIgnored(folder));
        setIgnoreContents(" s?c \nplugin.xml\n");
        assertTrue(IgnoredFiles.isIgnored(folder));
    }

    /**
     * Tests a file that isn't ignored
     */
    public void testNoIgnoreFile() {
        setIgnoreContents("");
        IFile file = project.getFile("build.properties");
        assertTrue(file.exists());
        assertFalse(IgnoredFiles.isIgnored(file));
        setIgnoreContents("*.propertie\nbuild.?\nbuild.porperties\n*.?ropertie");
        assertFalse(IgnoredFiles.isIgnored(file));
    }

    /**
     * Tests a folder that isn't ignored
     */
    public void testNoIgnoreFolder() {
        setIgnoreContents("");
        IFolder folder = project.getFolder("src");
        assertTrue(folder.exists());
        assertFalse(IgnoredFiles.isIgnored(folder));
        setIgnoreContents("s??c\n*rrc\n s rc");
        assertFalse(IgnoredFiles.isIgnored(folder));
    }

    /**
     * Test bad input passed to IgnoredFiles
     */
    public void testBadContainer() {
        IgnoredFiles.addIgnore(ResourcesPlugin.getWorkspace().getRoot());
        assertFalse(IgnoredFiles.isIgnored(ResourcesPlugin.getWorkspace()
                .getRoot()));
    }

    /**
     * Test a derived folder that shouldn't be added
     */
    public void testDerivedFolder() {
        IFolder newFolder = project.getFolder("newFolder");
        assertFalse(newFolder.exists());
        try {
            newFolder.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(newFolder.exists());
        assertFalse(newFolder.isDerived());
        try {
            newFolder.setDerived(true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(newFolder.isDerived());

        IFile file = newFolder.getFile("test.txt");
        assertFalse(file.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            file.create(fileUrl.openStream(), true, null);
            assertTrue(file.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(newFolder));
        add.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
    }

    /**
     * Test a derived file that shouldn't be added
     */
    public void testDerivedFile() {
        IFile file = project.getFile("test.txt");
        assertFalse(file.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            file.create(fileUrl.openStream(), true, null);
            assertTrue(file.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertFalse(file.isDerived());
        try {
            file.setDerived(true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(file.isDerived());

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(file));
        add.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
    }

    /**
     * Test a non-derived folder that is in a derived folder
     */
    public void testNestedDerivedFolder() {
        IFolder newFolder = project.getFolder("newFolder");
        assertFalse(newFolder.exists());
        try {
            newFolder.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(newFolder.exists());
        assertFalse(newFolder.isDerived());
        try {
            newFolder.setDerived(true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(newFolder.isDerived());

        IFolder subNewFolder = newFolder.getFolder("subNewFolder");
        assertFalse(subNewFolder.exists());
        try {
            subNewFolder.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(subNewFolder.exists());

        IFile file = subNewFolder.getFile("test.txt");
        assertFalse(file.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            file.create(fileUrl.openStream(), true, null);
            assertTrue(file.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(subNewFolder));
        add.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
    }

    /**
     * Test a non-derived file in a non-derived folder in a derived folder
     */
    public void testNestedDerivedFile() {
        IFolder newFolder = project.getFolder("newFolder");
        assertFalse(newFolder.exists());
        try {
            newFolder.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(newFolder.exists());
        assertFalse(newFolder.isDerived());
        try {
            newFolder.setDerived(true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(newFolder.isDerived());

        IFolder subNewFolder = newFolder.getFolder("subNewFolder");
        assertFalse(subNewFolder.exists());
        try {
            subNewFolder.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(subNewFolder.exists());

        IFile file = subNewFolder.getFile("test.txt");
        assertFalse(file.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            file.create(fileUrl.openStream(), true, null);
            assertTrue(file.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(file));
        add.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
    }

    /**
     * Tests a project not being ignored since it isn't supported but should
     * still fail gracefully when attempted
     */
    public void testProjectIgnore() {
        IgnoredFiles.addIgnore(project);
        assertFalse(IgnoredFiles.isIgnored(project));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/com.perforce.team.plugin";
    }

}
