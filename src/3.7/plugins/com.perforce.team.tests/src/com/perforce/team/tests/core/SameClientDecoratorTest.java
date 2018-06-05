/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.decorator.OverlayIcon;
import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.p4java.actions.RefreshAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SameClientDecoratorTest extends ProjectBasedTestCase {

    private ConnectionParameters parameters2 = null;

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project_same_client_decorators";
    }

    private IP4File editOtherFile(IFile addFile) {
        assertFalse(addFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            addFile.create(fileUrl.openStream(), true, null);
            assertTrue(addFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        IP4Connection other = createConnection2();
        IP4File otherFile = new P4File(other, addFile.getLocation()
                .makeAbsolute().toOSString());
        otherFile.refresh();
        otherFile.add(0);
        otherFile.refresh();
        assertTrue(otherFile.openedForAdd());

        IP4Changelist list = otherFile.getChangelist();
        assertNotNull(list);
        assertTrue(list instanceof IP4PendingChangelist);
        int id = ((IP4PendingChangelist) list).submit(
                "test OtherDecoratorTest.addOtherFile",
                new IP4File[] { otherFile }, new NullProgressMonitor());
        assertTrue(id > 0);
        otherFile.refresh();
        assertFalse(otherFile.openedForAdd());
        assertEquals(1, otherFile.getHeadRevision());

        otherFile.edit();
        otherFile.refresh();
        assertTrue(otherFile.openedForEdit());

        return otherFile;
    }

    private IP4File addOtherFile(IFile addFile) {
        assertFalse(addFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            addFile.create(fileUrl.openStream(), true, null);
            assertTrue(addFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        IP4Connection other = createConnection2();
        IP4File otherFile = new P4File(other, addFile.getLocation()
                .makeAbsolute().toOSString());
        otherFile.refresh();
        otherFile.add(0);
        otherFile.refresh();
        assertTrue(otherFile.openedForAdd());
        return otherFile;
    }

    private IP4File deleteOtherFile(IFile addFile) {
        assertFalse(addFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            addFile.create(fileUrl.openStream(), true, null);
            assertTrue(addFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        IP4Connection other = createConnection2();
        IP4File otherFile = new P4File(other, addFile.getLocation()
                .makeAbsolute().toOSString());
        otherFile.refresh();
        otherFile.add(0);
        otherFile.refresh();
        assertTrue(otherFile.openedForAdd());

        IP4Changelist list = otherFile.getChangelist();
        assertNotNull(list);
        assertTrue(list instanceof IP4PendingChangelist);
        int id = ((IP4PendingChangelist) list).submit(
                "test OtherDecoratorTest.addOtherFile",
                new IP4File[] { otherFile }, new NullProgressMonitor());
        assertTrue(id > 0);
        otherFile.refresh();
        assertFalse(otherFile.openedForAdd());
        assertEquals(1, otherFile.getHeadRevision());

        otherFile.delete();
        otherFile.refresh();
        assertTrue(otherFile.openedForDelete());

        return otherFile;
    }

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#initParameters()
     */
    @Override
    protected void initParameters() {
        super.initParameters();

        // params 2
        parameters2 = new ConnectionParameters();
        parameters2.setClient(parameters.getClient());
        parameters2.setUser(parameters.getUser() + "a");
        parameters2.setPort(parameters.getPort());
        parameters2.setPassword(parameters.getPassword());
    }

    private IP4Connection createConnection2() {
        IP4Connection connection = new P4Connection(parameters2);
        connection.login(parameters2.getPassword());
        connection.connect();
        assertTrue(connection.isConnected());
        assertNotNull(connection.getClient());
        return connection;
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        Utils.clearDecoratorPrefs();
        DecoratorManager manager = WorkbenchPlugin.getDefault()
                .getDecoratorManager();
        manager.clearCaches();
        for (DecoratorDefinition definitions : manager
                .getAllDecoratorDefinitions()) {
            if (definitions.getId().equals(
                    "com.perforce.team.ui.decorator.PerforceDecorator")) {
                definitions.setEnabled(true);
            }
        }
        manager.clearCaches();
        manager.updateForEnablementChange();
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        DecoratorManager manager = WorkbenchPlugin.getDefault()
                .getDecoratorManager();
        manager.clearCaches();
        for (DecoratorDefinition definitions : manager
                .getAllDecoratorDefinitions()) {
            if (definitions.getId().equals(
                    "com.perforce.team.ui.decorator.PerforceDecorator")) {
                definitions.setEnabled(false);
            }
        }
        manager.clearCaches();
        manager.updateForEnablementChange();
    }

    private void revertOtherFile(IP4File file) {
        file.revert();
        file.refresh();
        assertFalse(file.isOpened());
    }

    /**
     * Test the icon decorator when a file is opened for add on another client
     */
    public void testOtherAddImage() {
        IFile file = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
        IP4File addedFile = null;
        try {
            addedFile = addOtherFile(file);

            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;

            assertTrue(p4File.openedForAdd());
            assertFalse(p4File.openedByOwner());

            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                            IPerforceUIConstants.ICON_TOP_RIGHT);
            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_OTHER_ICON,
                            IPerforceUIConstants.ICON_BOTTOM_RIGHT);
            ImageDescriptor desc = PlatformUI.getWorkbench()
                    .getEditorRegistry().getImageDescriptor(file.getName());
            Image base = desc.createImage();
            OverlayIcon icon = new OverlayIcon(
                    base,
                    new ImageDescriptor[] { Utils
                            .getUIDescriptor(IPerforceUIConstants.IMG_DEC_ADD_OTHER), },
                    new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT, });
            Image expected = icon.createImage();
            PerforceDecorator decorator = new PerforceDecorator(true);
            Image decorated = decorator.decorateImage(base, file);
            for (int x = 0; x < expected.getBounds().width; x++) {
                for (int y = 0; y < expected.getBounds().height; y++) {
                    assertEquals(expected.getImageData().getPixel(x, y),
                            decorated.getImageData().getPixel(x, y));
                }
            }
        } finally {
            if (addedFile != null) {
                revertOtherFile(addedFile);
            }
        }
    }

    /**
     * Test the icon decorator when a file is opened for edit on another client
     */
    public void testOtherEditImage() {
        IFile file = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
        IP4File editedFile = null;
        try {
            editedFile = editOtherFile(file);

            RefreshAction refresh = new RefreshAction();
            refresh.setAsync(false);
            refresh.selectionChanged(null, new StructuredSelection(project));
            refresh.run(null);

            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);

            IP4File p4File = (IP4File) resource;
            assertTrue(p4File.openedForEdit());
            assertFalse(p4File.openedByOwner());

            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_SYNC2_ICON,
                            IPerforceUIConstants.ICON_TOP_LEFT);
            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                            IPerforceUIConstants.ICON_TOP_RIGHT);
            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_OTHER_ICON,
                            IPerforceUIConstants.ICON_BOTTOM_RIGHT);
            ImageDescriptor desc = PlatformUI.getWorkbench()
                    .getEditorRegistry().getImageDescriptor(file.getName());
            Image base = desc.createImage();
            OverlayIcon icon = new OverlayIcon(
                    base,
                    new ImageDescriptor[] {
                            Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_EDIT_OTHER),
                            Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_SYNC), },
                    new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT,
                            IPerforceUIConstants.ICON_TOP_LEFT, });
            Image expected = icon.createImage();
            PerforceDecorator decorator = new PerforceDecorator(true);
            Image decorated = decorator.decorateImage(base, file);
            for (int x = 0; x < expected.getBounds().width; x++) {
                for (int y = 0; y < expected.getBounds().height; y++) {
                    assertEquals(expected.getImageData().getPixel(x, y),
                            decorated.getImageData().getPixel(x, y));
                }
            }
        } finally {
            if (editedFile != null) {
                revertOtherFile(editedFile);
            }
        }
    }

    /**
     * Test the icon decorator when a file is opened for delete on another
     * client
     */
    public void testOtherDeleteImage() {
        IFile file = project.getFile("newFileToAdd"
                + System.currentTimeMillis() + ".txt");
        IP4File deletedFile = null;
        try {
            deletedFile = deleteOtherFile(file);
            assertTrue(file.exists());

            RefreshAction refresh = new RefreshAction();
            refresh.setAsync(false);
            refresh.selectionChanged(null, new StructuredSelection(project));
            refresh.run(null);

            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);

            IP4File p4File = (IP4File) resource;
            assertTrue(p4File.openedForDelete());
            assertFalse(p4File.openedByOwner());

            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_SYNC2_ICON,
                            IPerforceUIConstants.ICON_TOP_LEFT);
            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_OTHER_ICON,
                            IPerforceUIConstants.ICON_BOTTOM_RIGHT);
            ImageDescriptor desc = PlatformUI.getWorkbench()
                    .getEditorRegistry().getImageDescriptor(file.getName());
            Image base = desc.createImage();
            OverlayIcon icon = new OverlayIcon(
                    base,
                    new ImageDescriptor[] {
                            Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_DELETE_OTHER),
                            Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_SYNC), },
                    new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT,
                            IPerforceUIConstants.ICON_TOP_LEFT, });
            Image expected = icon.createImage();
            PerforceDecorator decorator = new PerforceDecorator(true);
            Image decorated = decorator.decorateImage(base, file);
            for (int x = 0; x < expected.getBounds().width; x++) {
                for (int y = 0; y < expected.getBounds().height; y++) {
                    assertEquals(expected.getImageData().getPixel(x, y),
                            decorated.getImageData().getPixel(x, y));
                }
            }
        } finally {
            if (deletedFile != null) {
                revertOtherFile(deletedFile);
            }
        }
    }

}
