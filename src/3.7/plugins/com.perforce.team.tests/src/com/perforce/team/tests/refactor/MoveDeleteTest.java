/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.refactor;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveProjectOperation;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.ImportProjectAction;
import com.perforce.team.ui.p4java.actions.RevertAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MoveDeleteTest extends ProjectBasedTestCase {

    /**
     * Test moving a folder to an existing folder
     */
    public void testMoveFolderExisting() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("images");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        int fromSize = fromFolder.members().length;

        IFolder folderTo = project.getFolder("META-INF");
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        IP4Resource[] fromFiles = fromFolder.members();
        assertNotNull(fromFiles);
        assertTrue(fromFiles.length > 0);

        int deleteSize = check(FileAction.DELETE, fromFolder);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        newFolder.refresh();
        IP4Resource[] toFiles = newFolder.members();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.ADD, newFolder);
        assertEquals(fromSize, toSize);
    }

    /**
     * Test moving a folder to a new folder
     */
    public void testMoveFolderNew() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("images");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        int fromSize = fromFolder.members().length;

        IFolder folderTo = project.getFolder("images2");
        assertFalse(folderTo.exists());
        try {
            folderTo.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        IP4Resource[] fromFiles = fromFolder.members();
        assertNotNull(fromFiles);
        assertTrue(fromFiles.length > 0);

        int deleteSize = check(FileAction.DELETE, fromFolder);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        newFolder.refresh();
        IP4Resource[] toFiles = newFolder.members();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.ADD, newFolder);
        assertEquals(fromSize, toSize);
    }

    /**
     * Tests moving a file
     */
    public void testMoveFile() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IFolder toFolder = project.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.DELETE, p4File);

        IFile toFile = toFolder.getFile(fromFile.getName());
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.ADD, p4File2);
    }

    /**
     * Test moving an un-added file
     */
    public void testMoveFileUnadded() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse2.properties");
        assertFalse(fromFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            fromFile.create(fileUrl.openStream(), true, null);
            assertTrue(fromFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        IFolder toFolder = project.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertNull(p4File.getP4JFile());

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        assertNull(p4File.getP4JFile());
        assertFalse(p4File.isOpened());

        try {
            fromFile.refreshLocal(IResource.DEPTH_ONE,
                    new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertFalse(fromFile.exists());

        IFile toFile = toFolder.getFile(fromFile.getName());
        try {
            toFile.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        assertNull(p4File2.getP4JFile());
        assertFalse(p4File2.isOpened());
    }

    /**
     * Tests moving a folder that contains at least one sub-folder to an
     * existing folder
     */
    public void testMoveNestedFoldersExisting() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("src");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        IP4File[] allFiles = fromFolder.getAllLocalFiles();
        int fromSize = allFiles.length;
        assertTrue(fromSize > 0);

        IFolder folderTo = project.getFolder("images");
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        int deleteSize = check(FileAction.DELETE, allFiles);
        assertTrue(deleteSize > 0);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        IP4File[] toFiles = newFolder.getAllLocalFiles();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.ADD, toFiles);
        assertEquals(fromSize, toSize);
    }

    /**
     * Tests moving a folder that contains at least one sub-folder to a new
     * folder
     */
    public void testMoveNestedFoldersNew() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFolder folderFrom = project.getFolder("src");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;
        IP4File[] allFiles = fromFolder.getAllLocalFiles();
        int fromSize = allFiles.length;
        assertTrue(fromSize > 0);

        IFolder folderTo = project.getFolder("src2");
        assertFalse(folderTo.exists());
        try {
            folderTo.create(true, true, null);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        assertTrue(folderTo.exists());

        try {
            folderFrom.move(folderTo.getFolder(folderFrom.getName())
                    .getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        int deleteSize = check(FileAction.DELETE, allFiles);
        assertTrue(deleteSize > 0);
        assertEquals(fromSize, deleteSize);

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        IP4File[] toFiles = newFolder.getAllLocalFiles();
        assertNotNull(toFiles);
        assertTrue(toFiles.length > 0);

        int toSize = check(FileAction.ADD, toFiles);
        assertEquals(fromSize, toSize);
    }

    /**
     * Tests deleting a file
     */
    public void testDeleteFile() {
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, true);
        try {
            IFile file = project.getFile("plugin.xml");
            assertTrue(file.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;

            try {
                file.delete(true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }

            check(FileAction.DELETE, p4File);
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, false);
        }
    }

    /**
     * Tests deleting a folder and underlying p4 open for deletes occurring
     */
    public void testDeleteFolder() {
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, true);
        try {
            IFolder folder = project.getFolder("images");
            assertTrue(folder.exists());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    folder);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4Folder);
            IP4Folder p4Folder = (IP4Folder) resource;
            int fromSize = p4Folder.members().length;

            try {
                folder.delete(true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }

            IP4Resource[] fromFiles = p4Folder.members();
            assertNotNull(fromFiles);
            assertTrue(fromFiles.length > 0);

            int deleteSize = check(FileAction.DELETE, p4Folder);
            assertEquals(fromSize, deleteSize);
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, false);
        }
    }

    /**
     * Tests deleting a project that will do an open for delete
     * 
     * @throws Exception
     *             - exception
     */
    public void testDeleteProject() throws Exception {
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, true);
        try {
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    project);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4Folder);
            IP4Folder p4Folder = (IP4Folder) resource;
            IP4File[] allFiles = p4Folder.getAllLocalFiles();
            int fromSize = allFiles.length;

            try {
                project.delete(true, true, null);
            } catch (CoreException e) {
                assertFalse("Core exception thrown", true);
            }

            IP4Resource[] fromFiles = p4Folder.members();
            assertNotNull(fromFiles);
            assertTrue(fromFiles.length > 0);

            int deleteSize = check(FileAction.DELETE, allFiles);
            assertEquals(fromSize, deleteSize);
        } finally {
            super.setUp();
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, false);
        }
    }

    /**
     * Tests moving an existing file across projects
     */
    public void testMoveExistingFileAcrossProjects() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IProject project2 = createOtherProject("//depot/p08.1/p4-eclipse/com.perforce.team.ui");
        try {
            IFile fromFile = project.getFile("p4eclipse.properties");
            assertTrue(fromFile.exists());

            IFolder toFolder = project2.getFolder("src/com/perforce");
            assertTrue(toFolder.exists());

            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    fromFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;

            try {
                fromFile.move(toFolder.getFile(fromFile.getName())
                        .getFullPath(), true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }

            check(FileAction.DELETE, p4File);

            IFile toFile = toFolder.getFile(fromFile.getName());
            assertTrue(toFile.exists());

            IP4Resource resource2 = P4Workspace.getWorkspace().getResource(
                    toFile);
            assertNotNull(resource2);
            assertTrue(resource2 instanceof IP4File);
            IP4File p4File2 = (IP4File) resource2;

            check(FileAction.ADD, p4File2);
        } finally {
            revertOtherProject(project2);
        }
    }

    /**
     * Tests moving a new file across projects
     */
    public void testMoveNewFileAcrossProjects() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IProject project2 = createOtherProject("//depot/p08.1/p4-eclipse/com.perforce.team.ui");
        try {
            IFile fromFile = project.getFile("p4eclipse2.properties");
            assertFalse(fromFile.exists());
            URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                    .getEntry("/resources/Test.txt");
            assertNotNull(fileUrl);
            try {
                fileUrl = FileLocator.toFileURL(fileUrl);
            } catch (IOException e) {
                assertFalse("IO exception thrown", true);
            }
            try {
                fromFile.create(fileUrl.openStream(), true, null);
                assertTrue(fromFile.exists());
            } catch (IOException e) {
                assertFalse("IO exception thrown", true);
            } catch (CoreException e) {
                assertFalse("Core exception thrown", true);
            }

            IFolder toFolder = project2.getFolder("src/com/perforce");
            assertTrue(toFolder.exists());

            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    fromFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            assertNull(p4File.getP4JFile());

            try {
                fromFile.move(toFolder.getFile(fromFile.getName())
                        .getFullPath(), true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }

            assertNull(p4File.getP4JFile());
            assertFalse(p4File.isOpened());

            IFile toFile = toFolder.getFile(fromFile.getName());
            assertTrue(toFile.exists());

            IP4Resource resource2 = P4Workspace.getWorkspace().getResource(
                    toFile);
            assertNotNull(resource2);
            assertTrue(resource2 instanceof IP4File);
            IP4File p4File2 = (IP4File) resource2;

            assertNull(p4File2.getP4JFile());
            assertFalse(p4File2.isOpened());
        } finally {
            revertOtherProject(project2);
        }
    }

    /**
     * Tests moving a folder across projects
     */
    public void testMoveFolderAcrossProjects() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IProject project2 = createOtherProject("//depot/p08.1/p4-eclipse/com.perforce.team.ui");
        try {
            IFolder folderFrom = project.getFolder("images");
            assertTrue(folderFrom.exists());
            IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                    folderFrom);
            assertNotNull(fromResource);
            assertTrue(fromResource instanceof IP4Folder);
            IP4Folder fromFolder = (IP4Folder) fromResource;
            int fromSize = fromFolder.members().length;

            IFolder folderTo = project2.getFolder("META-INF");
            assertTrue(folderTo.exists());

            try {
                folderFrom.move(folderTo.getFolder(folderFrom.getName())
                        .getFullPath(), true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }

            IP4Resource[] fromFiles = fromFolder.members();
            assertNotNull(fromFiles);
            assertTrue(fromFiles.length > 0);

            int deleteSize = check(FileAction.DELETE, fromFolder);
            assertEquals(fromSize, deleteSize);

            IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                    folderTo.getFolder(folderFrom.getName()));
            assertNotNull(newResource);
            assertTrue(newResource instanceof IP4Folder);
            IP4Folder newFolder = (IP4Folder) newResource;
            newFolder.refresh();
            IP4Resource[] toFiles = newFolder.members();
            assertNotNull(toFiles);
            assertTrue(toFiles.length > 0);

            int toSize = check(FileAction.ADD, newFolder);
            assertEquals(fromSize, toSize);
        } finally {
            revertOtherProject(project2);
        }
    }

    private IProject createOtherProject(String path) {
        IProject project = null;
        ImportProjectAction checkout = new ImportProjectAction();
        IP4Connection connection = new P4Connection(parameters);
        connection.setOffline(false);
        connection.login(parameters.getPassword());
        connection.connect();
        assertFalse(connection.isOffline());
        assertTrue(connection.isConnected());
        IP4Folder folder = new P4Folder(connection, null, path);
        assertNotNull(folder.getClient());
        assertNotNull(folder.getRemotePath());
        folder.updateLocation();
        assertNotNull(folder.getLocalPath());
        StructuredSelection selection = new StructuredSelection(folder);
        Action wrapAction = new Action() {
        };
        wrapAction.setEnabled(false);
        checkout.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());

        checkout.runAction(new NullProgressMonitor(), false);

        String name = folder.getName();

        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.forceSync(new NullProgressMonitor());

        project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        assertNotNull(project);
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e1) {
            assertFalse(true);
        }
        assertTrue(project.exists());
        assertTrue(project.isAccessible());
        assertTrue(project.isOpen());
        return project;
    }

    /**
     * Tests renaming an unopened file
     */
    public void testRenameUnopenedExistingFile() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("plugin.xml");
        assertTrue(fromFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());

        try {
            fromFile.move(project.getFile("plugin2.xml").getFullPath(), true,
                    null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.DELETE, p4File);

        IFile toFile = project.getFile("plugin2.xml");
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.ADD, p4File2);
    }

    /**
     * Tests renaming an opened existing file
     */
    public void testRenameOpenedExistingFile() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("plugin.xml");
        assertTrue(fromFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());

        try {
            fromFile.move(project.getFile("plugin2.xml").getFullPath(), true,
                    null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.DELETE, p4File);

        IFile toFile = project.getFile("plugin2.xml");
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.ADD, p4File2);
    }

    /**
     * Test deleting a file that is opened for add
     */
    public void testDeleteAddedFile() {
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, true);
        try {
            IFile file = project.getFile("plugin2345.xml");
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

            assertTrue(p4File.openedForAdd());

            try {
                file.delete(true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }
            assertFalse(file.exists());
            assertFalse(p4File.isOpened());
        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_DELETE_PROJECT_FILES, false);
        }
    }

    /**
     * Test moving a project, not supported but should still succeed
     */
    public void testMoveProject() {
        IP4File p4ProjectFile = null;
        try {
            IFile projectFile = this.project.getFile(".project");
            assertTrue(projectFile.exists());
            assertTrue(projectFile.isReadOnly());
            EditAction edit = new EditAction();
            edit.setAsync(false);
            edit.selectionChanged(null, new StructuredSelection(projectFile));
            edit.run(null);
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    projectFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            p4ProjectFile = (IP4File) resource;
            assertTrue(p4ProjectFile.isOpened());
            IPath path = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                    .makeAbsolute();
            try {
                MoveProjectOperation operation = new MoveProjectOperation(
                        project, path.toFile().toURI(), "test");
                PlatformUI.getWorkbench().getOperationSupport()
                        .getOperationHistory()
                        .execute(operation, new NullProgressMonitor(), null);
                assertNotNull(P4ConnectionManager.getManager().getConnection(
                        project));
                assertEquals(path.append(project.getName()),
                        project.getLocation());
            } catch (ExecutionException e) {
                assertFalse("Execution exception thrown:" + e.getMessage(),
                        true);
            }
        } finally {
            if (p4ProjectFile != null) {
                p4ProjectFile.revert();
                p4ProjectFile.refresh();
                assertFalse(p4ProjectFile.isOpened());
            }
        }
    }

    /**
     * Test moving a file with the refactoring support preference off
     */
    public void testMoveFileOff() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                false);
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IFolder toFolder = project.getFolder("src/com/perforce");
        assertTrue(toFolder.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        try {
            fromFile.move(toFolder.getFile(fromFile.getName()).getFullPath(),
                    true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        assertFalse(p4File.isOpened());
        assertFalse(fromFile.exists());

        IFile toFile = toFolder.getFile(fromFile.getName());
        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        assertFalse(p4File2.isRemote());
        assertFalse(p4File2.isOpened());
    }

    /**
     * Test moving a folder with the refactoring support preference off
     */
    public void testMoveFolderOff() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                false);
        IFolder folderFrom = project.getFolder("images");
        assertTrue(folderFrom.exists());
        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                folderFrom);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4Folder);
        IP4Folder fromFolder = (IP4Folder) fromResource;

        IFolder folderTo = project.getFolder("META-INF");
        assertTrue(folderTo.exists());

        IFolder toFolder = folderTo.getFolder(folderFrom.getName());
        assertFalse(toFolder.exists());
        try {
            folderFrom.move(toFolder.getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }
        assertFalse(folderFrom.exists());
        assertTrue(toFolder.exists());

        IP4Resource[] fromFiles = fromFolder.members();
        assertNotNull(fromFiles);
        assertTrue(fromFiles.length > 0);
        for (IP4Resource resource : fromFiles) {
            if (resource instanceof IP4File) {
                assertFalse(((IP4File) resource).isOpened());
            }
        }

        IP4Resource newResource = P4Workspace.getWorkspace().getResource(
                folderTo.getFolder(folderFrom.getName()));
        assertNotNull(newResource);
        assertTrue(newResource instanceof IP4Folder);
        IP4Folder newFolder = (IP4Folder) newResource;
        newFolder.refresh();
        IP4Resource[] toFiles = newFolder.members();
        assertNotNull(toFiles);
        assertEquals(0, toFiles.length);
    }

    private void revertOtherProject(IProject project) {
        if (project != null) {
            RevertAction revertAction = new RevertAction();
            revertAction.setAsync(false);
            revertAction.selectionChanged(null,
                    new StructuredSelection(project));
            revertAction.runAction(false);

            try {
                project.delete(true, true, null);
            } catch (CoreException e) {
                assertFalse(true);
            }
            assertFalse(project.exists());
        }
    }

    private void check(FileAction action, IP4File file) {
        assertSame(action, file.getAction());
        assertTrue(file.isOpened());
        assertTrue(Arrays.asList(
                file.getConnection().getPendingChangelist(0).members())
                .contains(file));
        switch (action) {
        case DELETE:
            assertTrue(file.openedForDelete());
            break;
        case BRANCH:
        case ADD:
            assertTrue(file.openedForAdd());
            break;
        case INTEGRATE:
        case EDIT:
            assertTrue(file.openedForEdit());
        default:
            break;
        }
    }

    private int check(FileAction action, IP4File[] files) {
        int counted = 0;
        for (IP4File file : files) {
            check(action, file);
            counted++;
        }
        return counted;
    }

    private int check(FileAction action, IP4Folder folder) {
        int counted = 0;
        for (IP4Resource resource : folder.members()) {
            if (resource instanceof IP4Folder) {
                counted += check(action, (IP4Folder) resource);
            } else {
                assertTrue(resource instanceof IP4File);
                check(action, (IP4File) resource);
                counted++;
            }
        }
        return counted;
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.core";
    }

}
