/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.RevertAction;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LinkedResourcesTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
    }

    private void revertProjectFile() {
        // revert .project file
        RevertAction action = new RevertAction();
        action.selectionChanged(null,
                new StructuredSelection(project.getFile(".project")));
        action.setAsync(false);
        action.runAction(false);
    }

    /**
     * Test adding a linked folder
     */
    public void testAddLinkedFolder() {
        IP4File p4File = null;
        IFile addedFile = null;
        IFolder linkedFolder = project.getFolder("linkedFolder");
        String linked = "//depot/p08.1/p4-eclipse/com.perforce.team.ui/META-INF";
        IP4Connection connection = createConnection();
        IP4Folder p4Folder = connection.getFolder(linked, true);
        new P4Collection(new IP4Resource[] { p4Folder }).forceSync(new NullProgressMonitor());
        assertNotNull(p4Folder.getLocalPath());
        assertFalse(linkedFolder.exists());
        try {
            linkedFolder.createLink(new Path(p4Folder.getLocalPath()), 0, null);
            assertTrue(linkedFolder.exists());
            assertEquals(p4Folder.getLocalPath(), linkedFolder.getLocation()
                    .makeAbsolute().toOSString());
            addedFile = linkedFolder.getFile("actual.txt");
            assertFalse(addedFile.exists());
            Utils.fillFile(addedFile);
            assertTrue(addedFile.exists());
            assertTrue(addedFile.getLocation().makeAbsolute().toOSString()
                    .startsWith(p4Folder.getLocalPath()));
            AddAction add = new AddAction();
            add.setAsync(false);
            Action wrap = Utils.getDisabledAction();
            add.selectionChanged(wrap, new StructuredSelection(linkedFolder));
            assertTrue(wrap.isEnabled());
            add.run(null);
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    addedFile);
            assertNotNull(resource);
            assertEquals(addedFile.getLocation().makeAbsolute().toOSString(),
                    resource.getLocalPath());
            assertTrue(resource instanceof IP4File);
            p4File = (IP4File) resource;
            assertTrue(p4File.openedForAdd());
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        } finally {
            if (p4File != null) {
                p4File.revert();
                p4File.refresh();
                assertFalse(p4File.isOpened());
            }
            if (addedFile != null) {
                try {
                    addedFile.delete(true, null);
                } catch (CoreException e) {
                    assertFalse("Core Exception thrown", true);
                }
            }
            revertProjectFile();
            if (p4Folder != null) {
                p4Folder.revert();
            }
        }
    }

    /**
     * Test adding a linked file
     */
    public void testAddLinkedFile() {
        IP4File p4File = null;
        IFile addedFile = project.getFile("linkedFile.txt");
        String linked = "//depot/p08.1/p4-eclipse/com.perforce.team.ui/META-INF";
        IP4Connection connection = createConnection();
        IP4Folder p4Folder = connection.getFolder(linked, true);
        new P4Collection(new IP4Resource[] { p4Folder }).forceSync(new NullProgressMonitor());
        assertNotNull(p4Folder.getLocalPath());
        String newFile = "actualFile.txt";
        File file = new File(p4Folder.getLocalPath(), newFile);
        try {
            file.createNewFile();
            addedFile.createLink(
                    new Path(p4Folder.getLocalPath()).append(newFile), 0, null);
            assertTrue(addedFile.exists());
            assertTrue(addedFile.getLocation().makeAbsolute().toOSString()
                    .startsWith(p4Folder.getLocalPath()));
            AddAction add = new AddAction();
            add.setAsync(false);
            Action wrap = Utils.getDisabledAction();
            add.selectionChanged(wrap, new StructuredSelection(addedFile));
            assertTrue(wrap.isEnabled());
            add.run(null);
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    addedFile);
            assertNotNull(resource);
            assertEquals(addedFile.getLocation().makeAbsolute().toOSString(),
                    resource.getLocalPath());
            assertTrue(resource instanceof IP4File);
            p4File = (IP4File) resource;
            assertTrue(p4File.openedForAdd());
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        } finally {
            if (p4File != null) {
                p4File.revert();
                p4File.refresh();
                assertFalse(p4File.isOpened());
            }
            if (file.exists()) {
                assertTrue(file.delete());
            }
            revertProjectFile();
            if (p4Folder != null) {
                p4Folder.revert();
            }
        }
    }

    /**
     * Test editing a linked folder
     */
    public void testEditLinkedFolder() {
        IFolder linkedFolder = project.getFolder("linkedFolder");
        String linked = "//depot/p08.1/p4-eclipse/com.perforce.team.ui/META-INF";
        IP4Connection connection = createConnection();
        IP4Folder p4Folder = connection.getFolder(linked, true);
        new P4Collection(new IP4Resource[] { p4Folder }).forceSync(new NullProgressMonitor());
        assertNotNull(p4Folder.getLocalPath());
        assertFalse(linkedFolder.exists());
        try {
            linkedFolder.createLink(new Path(p4Folder.getLocalPath()), 0, null);
            assertTrue(linkedFolder.exists());
            assertEquals(p4Folder.getLocalPath(), linkedFolder.getLocation()
                    .makeAbsolute().toOSString());
            EditAction edit = new EditAction();
            edit.setAsync(false);
            Action wrap = Utils.getDisabledAction();
            edit.selectionChanged(wrap, new StructuredSelection(linkedFolder));
            assertTrue(wrap.isEnabled());
            edit.run(null);

            p4Folder.refresh();
            for (IP4Resource resource : p4Folder.members()) {
                if (resource instanceof IP4File) {
                    assertTrue(((IP4File) resource).openedForEdit());
                }
            }
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        } finally {
            revertProjectFile();
            p4Folder.revert();
            IP4Folder folder = P4Workspace.getWorkspace()
                    .getConnection(project).getFolder(linked, true);
            folder.refresh();
        }
    }

    /**
     * Test editing a linked file
     */
    public void testEditLinkedFile() {
        IFile editedFile = project.getFile("linkedFile.txt");
        String linked = "//depot/p08.1/p4-eclipse/com.perforce.team.ui/META-INF";
        IP4Connection connection = createConnection();
        IP4Folder p4Folder = connection.getFolder(linked, true);
        new P4Collection(new IP4Resource[] { p4Folder }).forceSync(new NullProgressMonitor());
        assertNotNull(p4Folder.getLocalPath());
        try {
            editedFile.createLink(
                    new Path(p4Folder.getLocalPath()).append("MANIFEST.MF"), 0,
                    null);
            assertTrue(editedFile.exists());
            assertTrue(editedFile.getLocation().makeAbsolute().toOSString()
                    .startsWith(p4Folder.getLocalPath()));
            EditAction edit = new EditAction();
            edit.setAsync(false);
            Action wrap = Utils.getDisabledAction();
            edit.selectionChanged(wrap, new StructuredSelection(editedFile));
            assertTrue(wrap.isEnabled());
            edit.run(null);
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    editedFile);
            assertNotNull(resource);
            assertEquals(editedFile.getLocation().makeAbsolute().toOSString(),
                    resource.getLocalPath());
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            assertTrue(p4File.openedForEdit());
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        } finally {
            revertProjectFile();
            p4Folder.revert();
            IP4Folder folder = P4Workspace.getWorkspace()
                    .getConnection(project).getFolder(linked, true);
            folder.refresh();
        }
    }

}
