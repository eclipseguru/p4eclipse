/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.core.IDepot.DepotType;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.team.core.P4CoreUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Depot extends P4Container {

    private IDepot depotData = null;
    private IP4Connection connection = null;
    private String localPath = null;
    private List<IStreamSummary> streams = null;
    
    /**
     * Creates a new p4 depot object from a P4J depot metadata object and a p4
     * connection object.
     * 
     * @param depotData
     * @param connection
     */
    public P4Depot(IDepot depotData, IP4Connection connection) {
        this.depotData = depotData;
        this.connection = connection;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getLocalPath()
     */
    public String getLocalPath() {
        return this.localPath;
    }

    /**
     * Is any part of this depot local?
     * 
     * @return - true if any file in this depot is in the client view
     */
    public boolean isLocal() {
        boolean local = false;
        if (connection != null) {
            IClient client = connection.getClient();
            if (client != null) {
                ClientView view = client.getClientView();
                if (view != null) {
                    String depotPath = getRemotePath();
                    for (IClientViewMapping mapping : view) {
                        if (mapping.getDepotSpec().startsWith(depotPath)) {
                            local = true;
                            break;
                        }
                    }
                }
            }
        }
        return local;
    }

    /**
     * Sets the local path
     * 
     * @param localPath
     */
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getName()
     */
    public String getName() {
        if (depotData != null) {
            return depotData.getName();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getParent()
     */
    public IP4Container getParent() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getRemotePath()
     */
    public String getRemotePath() {
        String name = getName();
        if (name != null) {
            name = "//" + name; //$NON-NLS-1$
        }
        return name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClient()
     */
    public IClient getClient() {
        if (this.connection != null) {
            return this.connection.getClient();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object otherDepot) {
        if (this == otherDepot) {
            return true;
        } else if (otherDepot instanceof P4Depot) {
            return super.equals(otherDepot);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode()+P4CoreUtils.hashCode(connection)*31;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        return null;
    }

    /**
     * Get the file suffix for spec depots
     * 
     * @return - file suffix if this is a spec depot
     */
    public String getSuffix() {
        if (this.depotData != null) {
            return this.depotData.getSuffix();
        }
        return null;
    }

    /**
     * Get depot type value
     * 
     * @return - depot type or null if underlying Depot object is null
     */
    public DepotType getType() {
        if (this.depotData != null) {
            return this.depotData.getDepotType();
        }
        return null;
    }
    
    public List<IStreamSummary> getStreams() {
        members();  // trigger refresh if needed
        return streams;
    }
    
    @Override
    public void refresh(final int depth) {
        refreshStreams();
        super.refresh(depth);
    }
    
    private void refreshStreams() {
        // shortcut if not a stream depot (also avoid exception in p4java)
        if (this.getType() != IDepot.DepotType.STREAM)
            return;
        
        final List<IStreamSummary> streams = new ArrayList<IStreamSummary>();
        IP4ClientOperation streamsOp = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                    streams.addAll(getStreams(client));
            }
        };
        runOperation(streamsOp);
        this.streams = streams;
    }
    
    private List<IStreamSummary> getStreams(IClient client)
            throws P4JavaException {
        List<IStreamSummary> streams = new ArrayList<IStreamSummary>();
        IP4Connection connection = getConnection();
        String path = getRemotePath();
        if (connection == null || path == null)
            return streams;
        if (!(client.getServer() instanceof IOptionsServer))
            return streams;
        IOptionsServer os = (IOptionsServer)client.getServer();
        List<String> paths = new ArrayList<String>();
        paths.add(getRemotePath() + "/*");
        streams = os.getStreams(paths, null);
        return streams;
    }

}
