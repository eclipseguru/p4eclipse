/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ILabel;
import com.perforce.p4java.core.ILabelMapping;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

import java.util.Date;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Label extends P4Resource implements IP4Label {

    private IP4Connection connection = null;
    private ILabel p4jLabel;

    /**
     * Create a new p4 label from a p4j label
     * 
     * @param label
     * @param connection
     * @param needsRefresh
     */
    public P4Label(ILabel label, IP4Connection connection, boolean needsRefresh) {
        this.p4jLabel = label;
        this.connection = connection;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Label#getLabel()
     */
    public ILabel getLabel() {
        return this.p4jLabel;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath()
     */
    public String getActionPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath(com.perforce.team.core.p4java.IP4Resource.Type)
     */
    public String getActionPath(Type preferredType) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClient()
     */
    public IClient getClient() {
        IClient client = null;
        if (connection != null) {
            client = connection.getClient();
        }
        return client;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getLocalPath()
     */
    public String getLocalPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getName()
     */
    public String getName() {
        String name = null;
        if (this.p4jLabel != null) {
            name = this.p4jLabel.getName();
        }
        return name;
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
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isContainer()
     */
    public boolean isContainer() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh(int)
     */
    public void refresh(int depth) {
        refresh();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {
        final String name = getName();
        if (name != null) {
            IP4ServerOperation op = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    P4Label.this.p4jLabel = server.getLabel(name);
                    needsRefresh = false;
                }
            };
            runOperation(op);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Label#getAccessTime()
     */
    public Date getAccessTime() {
        Date access = null;
        if (this.p4jLabel != null) {
            access = this.p4jLabel.getLastAccess();
        }
        return access;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Label#getDescription()
     */
    public String getDescription() {
        String description = null;
        if (this.p4jLabel != null) {
            description = this.p4jLabel.getDescription();
        }
        return description;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Label#getOwner()
     */
    public String getOwner() {
        String owner = null;
        if (this.p4jLabel != null) {
            owner = this.p4jLabel.getOwnerName();
        }
        return owner;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Label#getUpdateTime()
     */
    public Date getUpdateTime() {
        Date update = null;
        if (this.p4jLabel != null) {
            update = this.p4jLabel.getLastUpdate();
        }
        return update;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Label#getRevision()
     */
    public String getRevision() {
        String revision = null;
        if (this.p4jLabel != null) {
            revision = this.p4jLabel.getRevisionSpec();
        }
        return revision;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Label#getView()
     */
    public ViewMap<ILabelMapping> getView() {
        ViewMap<ILabelMapping> mapping = null;
        if (this.p4jLabel != null) {
            mapping = this.p4jLabel.getViewMapping();
        }
        return mapping;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Label#isLocked()
     */
    public boolean isLocked() {
        return this.p4jLabel != null ? this.p4jLabel.isLocked() : false;
    }

}
