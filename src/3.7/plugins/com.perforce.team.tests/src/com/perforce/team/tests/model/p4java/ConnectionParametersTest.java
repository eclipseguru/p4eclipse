/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionParametersTest extends P4TestCase {

    /**
     * Test string constructor
     */
    public void testFromString() {
        ConnectionParameters params = new ConnectionParameters(
                "P4PORT perforce:1666 P4CLIENT test_client P4USER"
                        + " test_user P4CHARSET UTF-8 P4PASSWD password OFFLINE true SAVEPASS false");
        assertEquals("test_client", params.getClient());
        assertEquals("perforce:1666", params.getPort());
        assertEquals("test_user", params.getUser());
        assertEquals("UTF-8", params.getCharset());
        assertEquals("password", params.getPassword());
        assertNull(params.getAuthTicket());
        assertFalse(params.savePassword());
        assertTrue(params.isOffline());

        // // TODO: figure out why secure storage does not work on nightly test.
        // String key = params.getUserNoNull() + IConstants.AT +
        // params.getPort();
        // try {
        //
        // log("puting "+key+":password to secure storage start ...");
        // printTimestamp();
        // P4SecureStore.INSTANCE.put(key, "password", true);
        // log("puting "+key+":password to secure storage end!");
        // } catch (StorageException e) {
        // e.printStackTrace();
        // }
        //
        // params = new ConnectionParameters(
        // "P4PORT perforce:1666 P4CLIENT test_client P4USER"
        // + " test_user P4CHARSET UTF-8 OFFLINE true SAVEPASS true");
        //
        // assertEquals("test_client", params.getClient());
        // assertEquals("perforce:1666", params.getPort());
        // assertEquals("perforce", params.getHostComponent());
        // assertEquals("1666", params.getPortComponent());
        // assertEquals("test_user", params.getUser());
        // assertEquals("UTF-8", params.getCharset());
        // assertEquals("password", params.getPassword());
        // assertNull(params.getAuthTicket());
        // assertTrue(params.savePassword());
        // assertTrue(params.isOffline());
        //
        // try{
        // log("removeing "+key+" from secure storage start ...");
        // printTimestamp();
        // P4SecureStore.INSTANCE.remove(key);
        // log("removeing "+key+" from secure storage end!");
        // }catch(Exception e){
        // e.printStackTrace();
        // }

    }

    /**
     * Test default constructor
     */
    public void testDefault() {
        ConnectionParameters params = new ConnectionParameters();
        params.setClient("test_client");
        assertEquals("test_client", params.getClient());
        params.setPort("perforce:1666");
        assertEquals("perforce:1666", params.getPort());
        params.setUser("test_user");
        assertEquals("test_user", params.getUser());
        params.setCharset("UTF-8");
        assertEquals("UTF-8", params.getCharset());
        params.setPassword("password");
        assertEquals("password", params.getPassword());
        params.setSavePassword(true);
        String ticket = "ABCD1234EFG";
        params.setAuthTicket(ticket);
        assertEquals(ticket, params.getAuthTicket());
        assertTrue(params.savePassword());
        params.setOffline(true);
        assertTrue(params.isOffline());
        params.setSavePassword(false);
        assertFalse(params.savePassword());
        params.setOffline(false);
        assertFalse(params.isOffline());
        assertFalse(params.equals(new Object()));
    }

    /**
     * Test equals method of connection parameters
     */
    public void testEquals() {
        ConnectionParameters params = new ConnectionParameters();
        params.setClient("test_client");
        params.setPort("perforce:1666");
        params.setUser("test_user");
        params.setCharset("UTF-8");

        assertFalse(params.equals(new Object()));

        ConnectionParameters params2 = new ConnectionParameters();
        params2.setClient("test_client");
        assertFalse(params.equals(params2));
        params2.setPort("perforce:1666");
        assertFalse(params.equals(params2));
        params2.setUser("test_user");
        assertFalse(params.equals(params2));
        params2.setCharset("UTF-8");
        assertTrue(params.equals(params2));
    }

    /**
     * Test no null methods
     */
    public void testNoNull() {
        ConnectionParameters params = new ConnectionParameters();
        assertNotNull(params.getCharsetNoNull());
        assertNotNull(params.getClientNoNull());
        assertNotNull(params.getPasswordNoNull());
        assertNotNull(params.getPortNoNull());
        assertNotNull(params.getUserNoNull());
        assertFalse(params.savePassword());
        assertFalse(params.isOffline());
    }

    /**
     * Test copy method
     */
    public void testCopy() {
        ConnectionParameters params = new ConnectionParameters(
                "P4PORT perforce:1666 P4CLIENT test_client P4USER"
                        + " test_user P4CHARSET UTF-8 P4PASSWD password");
        ConnectionParameters params2 = new ConnectionParameters();
        params.setOffline(true);
        params.setSavePassword(true);
        params.setAuthTicket("TEST111TSET");
        params.copy(params2);
        assertEquals(params.getPort(), params2.getPort());
        assertEquals(params.getUser(), params2.getUser());
        assertEquals(params.getCharset(), params2.getCharset());
        assertEquals(params.getClient(), params2.getClient());
        assertEquals(params.getPassword(), params2.getPassword());
        assertEquals(params.savePassword(), params2.savePassword());
        assertEquals(params.isOffline(), params2.isOffline());
        assertEquals(params.getAuthTicket(), params2.getAuthTicket());
        assertEquals(params.hashCode(), params2.hashCode());
        assertEquals(params.toString(), params2.toString());
    }

}
