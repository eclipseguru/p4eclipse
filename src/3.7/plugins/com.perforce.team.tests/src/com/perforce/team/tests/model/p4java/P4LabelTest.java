/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Label;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;

import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4LabelTest extends ProjectBasedTestCase {

    /**
     * Basic empty label test
     */
    public void testEmpty() {
        IP4Connection connection = createConnection();
        IP4Label label = new P4Label(null, connection, true);
        assertTrue(label.needsRefresh());
        assertNull(label.getDescription());
        assertNull(label.getView());
        assertNull(label.getRevision());
        assertNull(label.getName());
        assertNull(label.getOwner());
        assertNull(label.getUpdateTime());
        assertNull(label.getAccessTime());
        assertNull(label.getLabel());
        assertNull(label.getActionPath());
        assertNull(label.getActionPath(Type.REMOTE));
        assertNull(label.getLocalPath());
        assertNull(label.getClientPath());
        assertNull(label.getRemotePath());
        assertFalse(label.isContainer());
        assertFalse(label.isLocked());
        assertEquals(connection, label.getParent());
        assertNotNull(label.getClient());
        try {
            label.refresh();
            label.refresh(-1);
            label.refresh(1);
        } catch (Throwable e) {
            handle(e);
        }
    }

    /**
     * Test tagging files
     */
    public void testLabelFiles() {
        IP4Connection connection = createConnection();
        IFile file1 = null;
        IFile file2 = null;
        try {
            file1 = Utils.addSubmitRandom(getClass().getName(), project,
                    connection);
            file2 = Utils.addSubmitRandom(getClass().getName(), project,
                    connection);
        } catch (Throwable e) {
            assertFalse("Error creating files: " + e.getMessage(), true);
        }
        IP4Resource resource1 = connection.getResource(file1);
        assertNotNull(resource1);
        IP4Resource resource2 = connection.getResource(file2);
        assertNotNull(resource2);
        String labelName = getClass().getName() + System.currentTimeMillis();
        P4Collection collection = new P4Collection(new IP4Resource[] {
                resource1, resource2 });
        collection.tag(labelName);
        IP4Label[] labels = connection.getLabels(null, 1, "..." + labelName
                + "...");
        assertNotNull(labels);
        assertEquals(1, labels.length);
        assertNotNull(labels[0]);
        assertNotNull(connection.getServer());
        String spec = "//...@" + labelName;
        try {
            List<IExtendedFileSpec> specs = connection
                    .getServer()
                    .getExtendedFiles(
                            P4FileSpecBuilder
                                    .makeFileSpecList(new String[] { spec }),
                            2, -1, -1, null, null);
            assertNotNull(specs);
            assertEquals(2, specs.size());
            assertEquals(resource1.getRemotePath(), specs.get(0)
                    .getDepotPathString());
            assertEquals(resource2.getRemotePath(), specs.get(1)
                    .getDepotPathString());
        } catch (P4JavaException e) {
            assertFalse("P4jException thrown: " + e.getMessage(), true);
        }
    }

    /**
     * Test untagging files
     */
    public void testUnlabelFiles() {
        IP4Connection connection = createConnection();
        IFile file1 = null;
        IFile file2 = null;
        try {
            file1 = Utils.addSubmitRandom(getClass().getName(), project,
                    connection);
            file2 = Utils.addSubmitRandom(getClass().getName(), project,
                    connection);
        } catch (Throwable e) {
            assertFalse("Error creating files: " + e.getMessage(), true);
        }
        IP4Resource resource1 = connection.getResource(file1);
        assertNotNull(resource1);
        IP4Resource resource2 = connection.getResource(file2);
        assertNotNull(resource2);
        String labelName = getClass().getName() + System.currentTimeMillis();
        P4Collection collection = new P4Collection(new IP4Resource[] {
                resource1, resource2 });
        collection.tag(labelName);
        IP4Label[] labels = connection.getLabels(null, 1, "..." + labelName
                + "...");
        assertNotNull(labels);
        assertEquals(1, labels.length);
        assertNotNull(labels[0]);
        assertNotNull(connection.getServer());
        String spec = "//...@" + labelName;
        try {
            List<IExtendedFileSpec> specs = connection
                    .getServer()
                    .getExtendedFiles(
                            P4FileSpecBuilder
                                    .makeFileSpecList(new String[] { spec }),
                            2, -1, -1, null, null);
            assertNotNull(specs);
            assertEquals(2, specs.size());
            assertEquals(resource1.getRemotePath(), specs.get(0)
                    .getDepotPathString());
            assertEquals(resource2.getRemotePath(), specs.get(1)
                    .getDepotPathString());
        } catch (P4JavaException e) {
            assertFalse("P4jException thrown: " + e.getMessage(), true);
        }
        collection.tag(labelName, null, true, false);
        try {
            List<IExtendedFileSpec> specs = connection
                    .getServer()
                    .getExtendedFiles(
                            P4FileSpecBuilder
                                    .makeFileSpecList(new String[] { spec }),
                            2, -1, -1, null, null);
            assertNotNull(specs);
            assertEquals(1, specs.size());
            assertEquals(FileSpecOpStatus.ERROR, specs.get(0).getOpStatus());
        } catch (P4JavaException e) {
            assertFalse("P4jException thrown: " + e.getMessage(), true);
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project_label";
    }

}
