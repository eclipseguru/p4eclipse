/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.folder;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.folder.P4DiffFile;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.folder.diff.model.FileEntry;
import com.perforce.team.ui.folder.diff.model.IGroupProvider;

import java.io.ByteArrayInputStream;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileEntryTest extends ProjectBasedTestCase {

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
     * Test entry
     */
    public void testEntry() {
        IGroupProvider provider = new IGroupProvider() {

            public Object getUniquePair(FileEntry entry) {
                return null;
            }

            public Type getType() {
                return Type.FLAT;
            }

            public Object getParent(FileEntry entry) {
                return this;
            }
        };
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(project
                .getFile("diff.txt"));
        assertNotNull(resource);
        String path1 = resource.getRemotePath() + "#1";
        String path2 = resource.getRemotePath() + "#2";
        IFileDiff[] diffs = connection.getDiffs(path1, path2);
        assertNotNull(diffs);
        assertEquals(1, diffs.length);
        IP4DiffFile diff = new P4DiffFile(connection, diffs[0], true);
        FileEntry entry = new FileEntry(diff, provider);
        assertEquals(provider, entry.getProvider());
        assertEquals(diff, entry.getFile());
        assertEquals(provider, entry.getParent(provider));
        assertNotNull(entry.getLabel(entry));
        assertNotNull(entry.getImageDescriptor(entry));
        assertEquals(connection, entry.getAdapter(IP4Connection.class));
        assertEquals(diff, entry.getAdapter(IP4Resource.class));
        assertEquals(diff, entry.getAdapter(IP4DiffFile.class));
        assertEquals(diff.getFile(), entry.getAdapter(IP4File.class));
    }

    /**
     * Test empty file entry
     */
    public void testEmpty() {
        FileEntry entry = new FileEntry(null, null);
        assertNull(entry.getVirtualPairPath());
        entry.setVirtualPairPath("test");
        assertEquals("test", entry.getVirtualPairPath());
        assertNull(entry.getPair());
        assertNull(entry.getProvider());
        assertNull(entry.getFile());
        assertNotNull(entry.getChildren(entry));
        assertNull(entry.getParent(entry));
        assertEquals(entry, entry);
        assertFalse(entry.equals(null));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/file/entry";
    }

}
