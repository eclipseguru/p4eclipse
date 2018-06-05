/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.folder;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.p4java.core.file.FileDiff;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.folder.P4DiffFile;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.tests.ProjectBasedTestCase;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffFileTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        addFile(client, project.getFile("diff.txt"), new ByteArrayInputStream(
                "content1".getBytes()));
        addFile(client, project.getFile("diff.txt"), new ByteArrayInputStream(
                "content2".getBytes()));
    }

    /**
     * Test empty diff file
     */
    public void testEmpty() {
        IP4Connection connection = createConnection();
        IFileDiff diff = new FileDiff(new HashMap<String, Object>());
        IP4DiffFile file = new P4DiffFile(connection, diff, true);
        assertTrue(file.isFile());
        assertTrue(file.isFile1());
        assertNull(file.getPair());
        assertNotNull(file.getDiff());
    }

    /**
     * Test invalid construction
     */
    public void testInvalid() {
        try {
            new P4DiffFile(null, new FileDiff(new HashMap<String, Object>()),
                    true);
            assertFalse(true);
        } catch (AssertionFailedException afe) {
            assertNotNull(afe);
        }
        try {
            new P4DiffFile(createConnection(), null, true);
            assertFalse(true);
        } catch (AssertionFailedException afe) {
            assertNotNull(afe);
        }
    }

    /**
     * Test diffing a file against a previous revision
     */
    public void testDiffRevision() {
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(project
                .getFile("diff.txt"));
        assertNotNull(resource);
        String path1 = resource.getRemotePath() + "#1";
        String path2 = resource.getRemotePath() + "#2";
        IFileDiff[] diffs = connection.getDiffs(path1, path2);
        assertNotNull(diffs);
        assertEquals(1, diffs.length);
        IFileDiff diff = diffs[0];
        assertNotNull(diff);

        IP4DiffFile left = new P4DiffFile(connection, diff, true);
        assertEquals(1, left.getRevision());
        assertEquals(Status.CONTENT, left.getStatus());
        assertEquals(connection, left.getConnection());
        assertEquals(resource.getActionPath(), left.getActionPath());
        assertEquals(resource.getActionPath(Type.REMOTE),
                left.getActionPath(Type.REMOTE));
        assertEquals(connection.getClient(), left.getClient());
        assertEquals(resource.getRemotePath(), left.getRemotePath());
        assertNull(left.getClientPath());
        assertNull(left.getLocalPath());
        assertFalse(left.isContainer());
        assertTrue(left.isFile());
        assertTrue(left.isFile1());
        assertNotNull(left.getFile());
        assertEquals(left, left);
        assertFalse(left.equals(resource));
        assertNotNull(left.toString());
        assertNotNull(left.getFile1Contents());
        assertNotNull(left.getFile2Contents());
        assertEquals(resource.getName(), left.getName());

        IP4DiffFile right = new P4DiffFile(connection, diff, false);
        assertEquals(2, right.getRevision());
        assertEquals(Status.CONTENT, right.getStatus());
        assertEquals(connection, right.getConnection());
        assertEquals(resource.getActionPath(), right.getActionPath());
        assertEquals(resource.getActionPath(Type.REMOTE),
                right.getActionPath(Type.REMOTE));
        assertEquals(connection.getClient(), right.getClient());
        assertEquals(resource.getRemotePath(), right.getRemotePath());
        assertNull(right.getClientPath());
        assertNull(right.getLocalPath());
        assertFalse(right.isContainer());
        assertTrue(right.isFile());
        assertFalse(right.isFile1());
        assertNotNull(right.getFile());
        assertEquals(right, right);
        assertFalse(right.equals(resource));
        assertNotNull(right.toString());
        assertNotNull(right.getFile1Contents());
        assertNotNull(right.getFile2Contents());
        assertEquals(resource.getName(), right.getName());

        assertFalse(left.equals(right));
        assertEquals(left.hashCode(), right.hashCode());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/diff";
    }

}
