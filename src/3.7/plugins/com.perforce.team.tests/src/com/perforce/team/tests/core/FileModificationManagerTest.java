/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.FileModificationValidatorManager;
import com.perforce.team.ui.IPerforceUIConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileModificationManagerTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IFile file = project.getFile("about.ini");
        IClient client = createConnection().getClient();
        addFile(client, file);
    }

    /**
     * Tests validate edit
     */
    public void testValidateEditOn() {
        FileModificationValidatorManager manager = new FileModificationValidatorManager();
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        assertTrue(file.isReadOnly());
        IStatus status = manager.validateEdit(new IFile[] { file }, null);
        assertNotNull(status);
        assertEquals(IStatus.OK, status.getSeverity());
        assertTrue(p4File.isOpened());
        assertFalse(file.isReadOnly());
    }

    /**
     * Tests auto edit turned off
     */
    public void testValidateEditOff() {
        FileModificationValidatorManager manager = new FileModificationValidatorManager();
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                false);
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        assertTrue(file.isReadOnly());
        IStatus status = manager.validateEdit(new IFile[] { file }, null);
        assertEquals(Status.CANCEL_STATUS, status);
        assertFalse(p4File.isOpened());
        assertTrue(file.isReadOnly());
    }

    /**
     * Tests validate save on
     */
    public void testValidateSaveOn() {
        FileModificationValidatorManager manager = new FileModificationValidatorManager();
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_REFACTOR_SAVE_SUPPORT, true);
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        assertTrue(file.isReadOnly());
        IStatus status = manager.validateSave(file);
        assertNotNull(status);
        assertEquals(IStatus.OK, status.getSeverity());
        assertTrue(p4File.isOpened());
        assertFalse(file.isReadOnly());
    }

    /**
     * Test validate save off
     */
    public void testValidateSaveOff() {
        FileModificationValidatorManager manager = new FileModificationValidatorManager();
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_REFACTOR_SAVE_SUPPORT, false);
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        assertTrue(file.isReadOnly());
        IStatus status = manager.validateSave(file);
        assertNotNull(status);
        assertEquals(IStatus.OK, status.getSeverity());
        assertFalse(p4File.isOpened());
        assertTrue(file.isReadOnly());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/com.perforce.team.plugin";
    }

}
