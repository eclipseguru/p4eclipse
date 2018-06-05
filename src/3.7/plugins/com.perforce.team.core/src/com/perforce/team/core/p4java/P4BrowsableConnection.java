/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4BrowsableConnection extends P4Connection {

    private boolean clientExists = false;

    /**
     * @param params
     */
    public P4BrowsableConnection(ConnectionParameters params) {
        super(params);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Connection#refreshClient()
     */
    @Override
    public boolean refreshClient() {
        IServer server = getServer();
        boolean retry = true;
        while (retry && server != null) {
            retry = false;
            try {
                String clientName = getParameters().getClient();
                IClient latest = null;
                if (clientName != null) {
                    latest = server.getClientTemplate(clientName, true);
                    if (latest != null) {
                        this.clientExists = latest.getUpdated() != null
                                && latest.getAccessed() != null;
                    } else {
                        clientExists = false;
                    }
                    server.setCurrentClient(latest);
                } else {
                    clientExists = false;
                }
                this.client = latest;
            } catch (P4JavaException e) {
                handleError(e);
                PerforceProviderPlugin.logError(e);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
            return this.client != null;
        }
        return false;
    }

    /**
     * Connect for a browsable connection means was the client and info
     * retrieved without any p4j exceptions being thrown and is the server info
     * existent
     * 
     * @see com.perforce.team.core.p4java.IP4Connection#connect()
     */
    @Override
    public void connect() {
        refresh();
        IServer server = getServer();
        if (server != null) {
            boolean retry = true;
            while (retry) {
                retry = false;
                try {
                    refreshClient();
                    this.serverInfo = server.getServerInfo();
                    if (getVersion() != null && !isSupported()) {
                        throw createUnsupportedException();
                    }
                    if (this.serverInfo != null) {
                        this.connected = true;
                        this.offline = false;
                    } else {
                        this.connected = false;
                    }
                } catch (P4JavaException e) {
                    retry = handleError(e);
                    if (retry) {
                        server = getServer();
                    } else {
                        this.connected = false;
                    }
                    PerforceProviderPlugin.logError(e);
                } catch (P4JavaError e) {
                    this.connected = false;
                    PerforceProviderPlugin.logError(e);
                }
            }
        } else {
            this.connected = false;
        }
    }

    /**
     * Did the last client retrieved actually exist?
     * 
     * @return - true if formally exists
     */
    public boolean clientExists() {
        return this.clientExists;
    }

    @Override
    public boolean equals(Object otherConnection) {
    	// override to prevent coverity from complaining.
    	return otherConnection instanceof P4BrowsableConnection && getParameters().equals(((P4BrowsableConnection) otherConnection).getParameters());
    }
    
    @Override
    public int hashCode() {
    	// override to prevent coverity from complaining.
    	return getParameters().hashCode();
    }
}
