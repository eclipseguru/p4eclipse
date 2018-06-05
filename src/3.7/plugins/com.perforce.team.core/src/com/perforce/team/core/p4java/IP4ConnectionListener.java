/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.team.core.ConnectionParameters;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4ConnectionListener {

    /**
     * Connection edited
     * 
     * @param connection
     * @param previousParams
     */
    void connectionChanged(IP4Connection connection,
            ConnectionParameters previousParams);

    /**
     * Connection added to workspace
     * 
     * @param connection
     */
    void connectionAdded(IP4Connection connection);

    /**
     * Connection about to be removed from workspace
     * 
     * @param params
     *            - connection parameters
     */
    void connectionRemovalRequested(ConnectionParameters params);

    /**
     * Connection removed from workspace
     * 
     * @param connection
     */
    void connectionRemoved(IP4Connection connection);

}
