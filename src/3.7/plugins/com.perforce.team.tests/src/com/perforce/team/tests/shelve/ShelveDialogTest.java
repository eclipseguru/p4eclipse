/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.shelve;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.shelve.ConfirmShelveDialog;
import com.perforce.team.ui.shelve.ShelveFileDialog;
import com.perforce.team.ui.shelve.UpdateShelveDialog;
import com.perforce.team.ui.shelve.UpdateShelveDialog.Option;
import com.perforce.team.ui.shelve.UpdateShelveDialog.ShelveChange;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelveDialogTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Test showing the shelve file dialog with files in the default changelist
     */
    public void testDefault() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());

        ConfirmShelveDialog defaultDialog = new ConfirmShelveDialog(
                Utils.getShell(), connection.getPendingChangelist(0),
                new IP4File[] { p4File });
        try {
            defaultDialog.setBlockOnOpen(false);
            defaultDialog.open();
            assertNotNull(defaultDialog.getShell().getText());
            assertNull(defaultDialog.getErrorMessage());
            assertNotNull(defaultDialog.getChangelist());
            defaultDialog.updateSelection();
            assertNotNull(defaultDialog.getSelectedFiles());
            assertEquals(1, defaultDialog.getSelectedFiles().length);
            assertEquals(p4File, defaultDialog.getSelectedFiles()[0]);
        } finally {
            defaultDialog.close();
        }
    }

    /**
     * Test showing the shelve file dialog with files in a numbered changelist
     */
    public void testNumbered() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());

        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveDialogTest.testNumbered", new IP4File[] { p4File });

        ShelveFileDialog numberedDialog = new ShelveFileDialog(
                Utils.getShell(), connection.getPendingChangelist(0),
                new IP4Resource[] { p4File }, new IP4Resource[] { p4File },
                Option.UPDATE);
        try {
            numberedDialog.setBlockOnOpen(false);
            numberedDialog.open();
            assertNotNull(numberedDialog.getShell().getText());
            assertNull(numberedDialog.getErrorMessage());
            numberedDialog.updateSelection();
            assertNotNull(numberedDialog.getSelectedFiles());
            assertEquals(1, numberedDialog.getSelectedFiles().length);
            assertEquals(p4File, numberedDialog.getSelectedFiles()[0]);
        } finally {
            numberedDialog.close();
            if (changelist != null) {
                changelist.revert();
                changelist.delete();
            }
        }
    }

    /**
     * Test showing the update shelve file dialog with shelved files
     */
    public void testUpdate() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());

        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveDialogTest.testNumbered", new IP4File[] { p4File });

        UpdateShelveDialog numberedDialog = new UpdateShelveDialog(
                Utils.getShell(), connection.getPendingChangelist(0),
                new IP4Resource[] { p4File }, new IP4Resource[0],
                new IP4Resource[] { p4File }, Option.UPDATE);
        try {
            numberedDialog.setBlockOnOpen(false);
            numberedDialog.open();
            assertNotNull(numberedDialog.getShell().getText());
            assertNull(numberedDialog.getErrorMessage());
            numberedDialog.updateChanges();
            ShelveChange[] changes = numberedDialog.getChanges();
            assertNotNull(changes);
            assertEquals(1, changes.length);
            assertEquals(UpdateShelveDialog.Option.ADD, changes[0].option);
            assertEquals(p4File, changes[0].file);
        } finally {
            numberedDialog.close();
            if (changelist != null) {
                changelist.revert();
                changelist.delete();
            }
        }
    }

    /**
     * Test showing the delete shelve file dialog with shelved files
     */
    public void testDelete() {
        IFile file = project.getFile("plugin.xml");
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(file);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());

        IP4PendingChangelist changelist = connection.createChangelist(
                "ShelveDialogTest.testNumbered", new IP4File[] { p4File });
        ShelveFileDialog numberedDialog = null;
        try {
            assertFalse(changelist.isShelved());
            changelist.shelve(new IP4File[] { p4File });
            assertTrue(changelist.isShelved());
            numberedDialog = new ShelveFileDialog(Utils.getShell(),
                    connection.getPendingChangelist(0),
                    new IP4Resource[] { p4File }, new IP4Resource[] { p4File },
                    Option.DELETE);
            numberedDialog.setBlockOnOpen(false);
            numberedDialog.open();
            assertNotNull(numberedDialog.getShell().getText());
            assertNull(numberedDialog.getErrorMessage());
            numberedDialog.updateSelection();
            assertNotNull(numberedDialog.getSelectedFiles());
            assertEquals(1, numberedDialog.getSelectedFiles().length);
            assertEquals(p4File, numberedDialog.getSelectedFiles()[0]);
        } finally {
            if (numberedDialog != null) {
                numberedDialog.close();
            }
            if (changelist != null) {
                changelist.revert();
                changelist.deleteShelved();
                changelist.delete();
            }
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

}
