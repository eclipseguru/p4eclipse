/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests;

import junit.framework.TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ServerBasedTestCase extends TestCase {

    /**
     * Server object
     */
    // protected PerforceServer server = null;
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // assertNotNull(System.getProperty("p4.client"));
        // assertNotNull(System.getProperty("p4.user"));
        // assertNotNull(System.getProperty("p4.password"));
        // assertNotNull(System.getProperty("p4.port"));
        // ConnectionParameters params = new ConnectionParameters();
        // params.setClient(System.getProp/erty("p4.client"));
        // params.setUser(System.getProperty("p4.user"));
        // params.setPort(System.getProperty("p4.port"));
        // params.setPassword(System.getProperty("p4.password"));
        // DepotView view = DepotView.showView();
        // ConnectionParameters cached = ConnectionCache.defaultCache
        // .getParameters(params);
        // view.addConnection(cached);
        // server = new PerforceServer(view, cached);
        // assertSame(server, server.getPerforceServer());
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // DepotView view = DepotView.showView();
        // view.removeConnection(server.getConnectionParams());
        // ConnectionCache.defaultCache.removeConnection(server
        // .getConnectionParams());
    }

}
