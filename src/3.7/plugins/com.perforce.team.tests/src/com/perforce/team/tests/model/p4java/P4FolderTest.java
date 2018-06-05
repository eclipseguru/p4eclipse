/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Container;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4FolderTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        addDepotFile(createConnection().getClient(),
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/test.txt");
        addDepotFile(createConnection().getClient(),
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/test.txt");
    }

    /**
     * Test the p4 folder edit method options
     */
    public void testEdit() {
        IP4Folder folder = new P4Folder(createConnection(), null,
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin");
        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.revert();
        collection.forceSync(new NullProgressMonitor());
        folder.edit();
        folder.refresh(); // this only refresh depth folders, not files.
        IP4Resource[] members = folder.members();
        assertNotNull(members);
        assertTrue(members.length > 0);

        boolean fileProcessed = false;
        for (IP4Resource resource : members) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                file.refresh(); // this is necessary in order to get the right state.
                assertTrue(file.isOpened());
                assertTrue(file.openedForEdit());
                fileProcessed = true;
            }
        }
        assertTrue(fileProcessed);

        folder.revert();
        folder.refresh(); // this only refresh depth 1.
        fileProcessed = false;
        members = folder.members();
        for (IP4Resource resource : members) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                file.refresh(); // this is necessary in order to get the right state.
                assertFalse(file.isOpened());
                assertFalse(file.openedForEdit());
                fileProcessed = true;
            }
        }
        assertTrue(fileProcessed);
    }

    /**
     * Test the p4 folder delete method options
     */
    public void testDelete() {
        IP4Folder folder = new P4Folder(createConnection(), null,
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin");
        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.revert();
        collection.forceSync(new NullProgressMonitor());
        folder.delete();
        folder.refresh(); // this only refresh depth folders, not files.
        IP4Resource[] members = folder.members();
        assertNotNull(members);
        assertTrue(members.length > 0);

        boolean fileProcessed = false;
        for (IP4Resource resource : members) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                file.refresh(); // this is necessary in order to get the right state.
                assertTrue(file.isOpened());
                assertTrue(file.openedForDelete());
                fileProcessed = true;
            }
        }
        assertTrue(fileProcessed);

        folder.revert();
        folder.refresh(); // this only refresh depth folders, not files.
        fileProcessed = false;
        members = folder.members();
        for (IP4Resource resource : members) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                file.refresh(); // this is necessary in order to get the right state.
                assertFalse(file.isOpened());
                assertFalse(file.openedForDelete());
                fileProcessed = true;
            }
        }
        assertTrue(fileProcessed);
    }

    /**
     * Test {@link P4Container#sync()}
     */
    public void testSync() {
        IP4Folder folder = new P4Folder(createConnection(), null,
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin");
        IP4Resource[] members = folder.members();
        assertNotNull(members);
        assertTrue(members.length > 0);

        IP4File unsynced = null;
        for (IP4Resource resource : members) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                if (file.getHaveRevision() > 1) {
                    P4Collection collection = new P4Collection();
                    collection.add(file);
                    collection.sync("#1",new NullProgressMonitor());
                    file.refresh(); // this is necessary in order to get the right state.
                    assertEquals(1, file.getHaveRevision());
                    assertTrue(file.getHaveRevision() < file.getHeadRevision());
                    unsynced = file;
                }
            }
        }
        assertNotNull(unsynced);
        assertFalse(unsynced.isSynced());
        folder.sync(new NullProgressMonitor());
        unsynced.refresh();
        assertTrue(unsynced.isSynced());
        assertEquals(unsynced.getHaveRevision(), unsynced.getHeadRevision());
    }

    /**
     * Tests the p4 folder object
     */
    public void testP4Folder() {
        P4Connection connection = new P4Connection(parameters);
        IP4Resource[] members = connection.members();
        assertNotNull(members);
        assertTrue(members.length > 0);
        boolean depotProcessed = false;
        boolean folderProcessed = false;
        for (IP4Resource resource : members) {
            if (resource instanceof P4Depot) {
                P4Depot depot = (P4Depot) resource;
                depotProcessed = true;
                IP4Resource[] folders = depot.members();
                assertNotNull(folders);
                for (IP4Resource res : folders) {
                    if (res instanceof IP4Folder) {
                        IP4Folder folder = (IP4Folder) res;
                        folderProcessed = true;
                        assertNotNull(folder.getName());
                        assertNotNull(folder.getActionPath());
                        assertNotNull(folder.getRemotePath());
                        assertNotNull(folder.getParent());
                        assertNotNull(folder.getClient());
                        assertNotNull(folder.toString());
                        IP4Folder folderSame = new P4Folder(folder.getParent(),
                                folder.getName());
                        folderSame.updateLocation();
                        assertEquals(folder, folderSame);
                        assertEquals(folder, folder);
                        assertFalse(folder.equals(depot));
                        assertSame(depot, folder.getParent());
                        IP4Resource[] folderChildren = folder.members();
                        assertNotNull(folderChildren);
                        assertSame(folderChildren, folder.members());
                    }
                }
                break;
            }
        }
        assertTrue(depotProcessed);
        assertTrue(folderProcessed);
    }

    /**
     * Test all local file container method
     */
    public void testAllLocalFiles() {
        IP4Folder folder = new P4Folder(createConnection(), null,
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin");
        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.revert();
        collection.forceSync(new NullProgressMonitor());
        IP4File[] allChidren = folder.getAllLocalFiles();
        assertNotNull(allChidren);
        assertTrue(allChidren.length > 0);
    }

    /**
     * Tests a folder created with all null values
     */
    public void testEmptyFolder() {
        IP4Folder folder = new P4Folder((IP4Connection) null, null, null);
        assertNull(folder.getConnection());
        assertNull(folder.getParent());
        assertNull(folder.getLocalPath());
        assertNull(folder.getRemotePath());
        assertNull(folder.getClientPath());
        assertNull(folder.getClient());
        assertNull(folder.getActionPath());
        assertNull(folder.getErrorHandler());
        assertNull(folder.getName());
        assertNotNull(folder.toString());
        assertNotNull(folder.getLocalContainers());
        assertEquals(0, folder.getLocalContainers().length);
        assertTrue(folder.needsRefresh());
        folder.refresh(); // this only refresh depth folders, not files.
        assertFalse(folder.needsRefresh());
        folder.markForRefresh();
        assertTrue(folder.needsRefresh());
    }

}
