/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4CollectionTest extends ConnectionBasedTestCase {

    /**
     * Test the IP4Resource.Type enum
     */
    public void testTypes() {
        Type[] types = IP4Resource.Type.values();
        assertNotNull(types);
        assertTrue(types.length > 0);
        for (Type type : types) {
            assertNotNull(type);
            assertNotNull(type.toString());
            assertEquals(type, Type.valueOf(type.toString()));
        }
    }

    /**
     * Tests an empty p4 collection
     */
    public void testEmptyP4Collection() {
        P4Collection collection = new P4Collection();
        assertNull(collection.getActionPath());
        assertNull(collection.getActionPath(Type.REMOTE));
        assertNull(collection.getActionPath(Type.LOCAL));
        assertNull(collection.getClient());
        assertNull(collection.getClientPath());
        assertNull(collection.getConnection());
        assertNull(collection.getErrorHandler());
        assertNull(collection.getLocalPath());
        assertNull(collection.getName());
        assertNull(collection.getParent());
        assertNull(collection.getRemotePath());
        assertNotNull(collection.getAllLocalFiles());
        assertEquals(0, collection.getAllLocalFiles().length);
        assertFalse(collection.needsRefresh());
        collection.markForRefresh();
        assertFalse(collection.needsRefresh());
        assertNotNull(collection.members());
        assertEquals(0, collection.members().length);
        assertTrue(collection.isContainer());
    }

    /**
     * Tests a p4 collection with a depot, file, and folder
     */
    public void testP4Collection() {
        IP4Connection connection = createConnection();
        P4Collection collection = new P4Collection();
        IP4File file = new P4File(connection, null);
        IP4Folder folder = new P4Folder(connection, null, null);
        P4Depot depot = new P4Depot(null, connection);
        collection.add(file);
        collection.add(folder);
        collection.add(depot);
        assertTrue(collection.contains(file));
        assertTrue(collection.contains(folder));
        assertTrue(collection.contains(depot));
        assertNotNull(collection.members());
        assertEquals(3, collection.members().length);
        assertNotNull(collection.getAllLocalFiles());
        assertEquals(1, collection.getAllLocalFiles().length);
        assertFalse(collection.contains(new P4File(connection, null)));
        assertFalse(collection.contains(null));
    }

    /**
     * Test the helper methods for creating a p4 collection
     */
    public void testCreators() {
        P4Collection collection = P4Collection.createCollection(null, null);
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
        collection = P4Collection.createCollection(createConnection(), null);
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                parameters);
        String path = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml";
        FileSpec spec = new FileSpec(path);
        spec.setDepotPath(path);
        List<IFileSpec> specs = new ArrayList<IFileSpec>();
        specs.add(spec);
        collection = P4Collection.createCollection(connection, specs);
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
        IP4Resource[] resources = collection.members();
        assertNotNull(resources);
        assertEquals(1, resources.length);
        assertTrue(resources[0] instanceof IP4File);
    }
}
