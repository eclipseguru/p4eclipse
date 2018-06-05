/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.team.core.ConnectionParameters;

/**
 * Adapter for {@link IP4ConnectionListener} interface.
 * 
 * Should be subclassed instead of implementing {@link IP4ConnectionListener}
 * directly.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4ConnectionListener implements IP4ConnectionListener {

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionListener#connectionChanged(com.perforce.team.core.p4java.IP4Connection,
     *      com.perforce.team.core.ConnectionParameters)
     */
    public void connectionChanged(IP4Connection connection,
            ConnectionParameters previousParams) {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionListener#connectionAdded(com.perforce.team.core.p4java.IP4Connection)
     */
    public void connectionAdded(IP4Connection connection) {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionListener#connectionRemoved(com.perforce.team.core.p4java.IP4Connection)
     */
    public void connectionRemoved(IP4Connection connection) {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionListener#connectionRemovalRequested(ConnectionParameters)
     */
    public void connectionRemovalRequested(ConnectionParameters params) {
        // Does nothing by default, subclasses should override
    }

}
