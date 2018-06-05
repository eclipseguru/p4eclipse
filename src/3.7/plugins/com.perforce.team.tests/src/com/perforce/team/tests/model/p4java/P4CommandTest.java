/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import java.io.File;
import java.util.Map;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.mapbased.rpc.RpcPropertyDefs;
import com.perforce.team.core.IP4CommandCallback;
import com.perforce.team.core.IP4ServerConstants;
import com.perforce.team.core.P4CommandCallback;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Command;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4CommandTest extends ConnectionBasedTestCase {

    /**
     * @throws Exception
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        IClient client = createConnection().getClient();
        openFile(client, client.getServer().getChangelist(0), client.getRoot()
                + File.separator + "openedFile.txt");
        addDepotFile(client,
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml");
    }

    /**
     * Test invalid command
     */
    public void testInvalidCommand() {
        P4Command p4Command = new P4Command(null, null);
        try {
            p4Command.run(true, null);
        } catch (Exception e) {
            assertFalse("Exception thrown with null values", true);
        }
    }

    /**
     * Test p4 set
     */
    public void testUnsupportedCommand() {
        P4Workspace.getWorkspace().getServerProperties()
        .put(RpcPropertyDefs.RPC_RELAX_CMD_NAME_CHECKS_NICK, false);

        IP4Connection connection = createConnection();
        String command = "set";
        P4Command p4Command = new P4Command(connection, command);
        @SuppressWarnings("unchecked")
        final Map<String, Object>[] call = new Map[] { null };
        p4Command.run(false, new P4CommandCallback() {

            @Override
            public void callbackError(java.util.List<Map<String, Object>> data) {
                call[0] = (Map<String, Object>) data.get(0);
            }

        });
        int max = 60;
        int counter = 0;
        while (call[0] == null && counter < max) {
            Utils.sleep(.1);
            counter++;
        }
        assertNotNull(call[0]);
        assertTrue(call[0].containsKey("Error"));
        assertTrue(call[0].keySet().size() > 0);
        assertTrue(call[0].values().size() > 0);
    }

    /**
     * Tests opened -m 1
     */
    public void testP4Command1() {
        IP4Connection connection = createConnection();
        String command = "opened -a -m 1";
        P4Command p4Command = new P4Command(connection, command);
        @SuppressWarnings("unchecked")
        final Map<String, Object>[][] call = new Map[][] { null };
        p4Command.run(false, new P4CommandCallback() {

            @Override
            public void callback(java.util.List<Map<String, Object>> data) {
                call[0] = (Map<String, Object>[]) data.toArray();
            }

        });
    	int serverVer = connection.getServer().getServerVersionNumber();
    	if(serverVer>=IP4ServerConstants.PROGRESS_SERVERID_VERSION){
	        assertNull(call[0]);
    	}else{
	        int max = 60;
	        int counter = 0;
	        while (call[0] == null && counter < max) {
	            Utils.sleep(.1);
	            counter++;
	        }
	        assertNotNull(call[0]);
	        assertEquals(1, call[0].length);
	        assertTrue(call[0][0].keySet().size() > 0);
	        assertTrue(call[0][0].values().size() > 0);
    	}
    }

    /**
     * Tests p4 info
     */
    public void testP4Command2() {
        IP4Connection connection = createConnection();
        String command = "p4 info";
        P4Command p4Command = new P4Command(connection, command);
        @SuppressWarnings("unchecked")
        final Map<String, Object>[][] call = new Map[][] { null };
        p4Command.run(false, new P4CommandCallback() {

            @Override
            public void callback(java.util.List<Map<String, Object>> data) {
                call[0] = (Map<String, Object>[]) data.toArray();
            }

        });
    	int serverVer = connection.getServer().getServerVersionNumber();
    	if(serverVer>=IP4ServerConstants.PROGRESS_SERVERID_VERSION){
	        assertNull(call[0]);
    	}else{
	        int max = 60;
	        int counter = 0;
	        while (call[0] == null && counter < max) {
	            Utils.sleep(.1);
	            counter++;
	        }
	        assertNotNull(call[0]);
	        assertEquals(1, call[0].length);
	        assertTrue(call[0][0].keySet().size() > 0);
	        assertTrue(call[0][0].values().size() > 0);
    	}
    }

    /**
     * Tests fstat with refresh
     */
    public void testP4Command3() {
        IP4Connection connection = createConnection();
        String filePath = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml";
        IP4File file = connection.getFile(filePath);
        assertNotNull(file);
        IFileSpec spec = file.getP4JFile();
        assertNotNull(spec);
        String command = "p4 fstat " + filePath;
        P4Command p4Command = new P4Command(connection, command);
        @SuppressWarnings("unchecked")
        final Map<String, Object>[][] call = new Map[][] { null };
        p4Command.run(true, new P4CommandCallback() {

            @Override
            public void callback(java.util.List<Map<String, Object>> data) {
                call[0] = (Map<String, Object>[]) data.toArray();
            }

        });
    	int serverVer = connection.getServer().getServerVersionNumber();
    	if(serverVer>=IP4ServerConstants.PROGRESS_SERVERID_VERSION){
	        assertNull(call[0]);
    	}else{
	        int max = 60;
	        int counter = 0;
	        while (call[0] == null && counter < max) {
	            Utils.sleep(.1);
	            counter++;
	        }
	        assertNotNull(call[0]);
	        assertEquals(1, call[0].length);
	        assertTrue(call[0][0].keySet().size() > 0);
	        assertTrue(call[0][0].values().size() > 0);
	        IFileSpec endSpec = file.getP4JFile();
	        assertNotSame(spec, endSpec);
    	}
    }

    /**
     * Test base callback class
     */
    public void testBaseCallback() {
        try {
            IP4CommandCallback callback = new P4CommandCallback();
            callback.callback(null);
            callback.callbackError(null);
        } catch (Throwable e) {
            assertFalse("Throwable thrown: " + e.getMessage(), true);
        }
    }
}
