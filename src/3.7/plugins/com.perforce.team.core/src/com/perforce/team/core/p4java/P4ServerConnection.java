/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.ConnectionParameters;

/**
 * 
 * P4 Connection that will doesn't contain a p4jclient object
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ServerConnection extends P4Connection {

    /**
     * @param params
     */
    public P4ServerConnection(ConnectionParameters params) {
        super(params);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Connection#getClient()
     */
    @Override
    public IClient getClient() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Connection#refreshClient()
     */
    @Override
    public boolean refreshClient() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isConnected()
     */
    @Override
    public boolean isConnected() {
        return this.connected && this.server != null;
    }

}
