/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IJob;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.PerforceProviderPlugin;

import java.util.Map;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Job extends P4Resource implements IP4Job {

    private IP4Connection connection = null;
    private IJob p4jJob = null;
    private IP4Changelist changelist;
    private String id;

    /**
     * Creates a new p4 job
     * 
     * @param job
     * @param connection
     * @param changelist
     */
    public P4Job(IJob job, IP4Connection connection, IP4Changelist changelist) {
        this.p4jJob = job;
        this.connection = connection;
        this.changelist = changelist;
        formatId();
    }

    private void formatId() {
        if (this.p4jJob != null) {
            String rawId = this.p4jJob.getId();

            // Convert job id from "job with space" to job_with_space
            if(rawId!=null){
	            int length = rawId.length();
	            if (length > 2) {
	                // Check if id is quoted, and if so remove quotes and replace
	                // spaces with _
	                if (rawId.charAt(0) == '\"' && rawId.charAt(length - 1) == '\"') {
	                    rawId = rawId.substring(1, length - 1).replace(' ', '_');
	                }
	            }
            }

            this.id = rawId;
        } else {
            this.id = null;
        }
    }

    /**
     * Creates a new p4 job
     * 
     * @param job
     * @param connection
     */
    public P4Job(IJob job, IP4Connection connection) {
        this(job, connection, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Job#getDescription()
     */
    public String getDescription() {
        String description = null;
        if (this.p4jJob != null) {
            description = this.p4jJob.getDescription();
        }
        return description;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Job#getField(java.lang.String)
     */
    public Object getField(String name) {
        Object fieldValue = null;
        if (name != null && this.p4jJob != null) {
            Map<String, Object> fields = this.p4jJob.getRawFields();
            if (fields != null) {
                fieldValue = fields.get(name);
            }
        }
        return fieldValue;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Job#getFieldNames()
     */
    public String[] getFieldNames() {
        String[] fields = new String[0];
        if (this.p4jJob != null) {
            Map<String, Object> rawFields = this.p4jJob.getRawFields();
            if (rawFields != null) {
                fields = rawFields.keySet().toArray(new String[0]);
            }
        }
        return fields;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Job#getFieldValues()
     */
    public Object[] getFieldValues() {
        Object[] values = new Object[0];
        if (this.p4jJob != null) {
            Map<String, Object> rawFields = this.p4jJob.getRawFields();
            if (rawFields != null) {
                values = rawFields.values().toArray(new String[0]);
            }
        }
        return values;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Job#getId()
     */
    public String getId() {
        return this.id;
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
    public String getActionPath(Type type) {
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
        return getId();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getParent()
     */
    public IP4Container getParent() {
        return this.changelist != null ? this.changelist : this.connection;
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
        IP4ServerOperation op = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                IJob latestJob = server.getJob(getId());
                P4Job.this.p4jJob = latestJob;
                formatId();
            }
        };
        runOperation(op);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Job#getShortDescription()
     */
    public String getShortDescription() {
        String full = getDescription();
        if (full != null) {
            // Convert newlines to spaces
            StringBuilder buffer = new StringBuilder(full);
            for (int i = 0; i < buffer.length(); i++) {
                if (buffer.charAt(i) == '\n') {
                    buffer.setCharAt(i, ' ');
                }
            }
            // Only return first 256 chars of description
            if (buffer.length() > 256) {
                buffer.setLength(256);
            }
            return buffer.toString();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof IP4Job) {
            IP4Job other = (IP4Job) obj;
            if (!connectionEquals(other)) {
                return false;
            }
            String id = getId();
            String oId = other.getId();
            if (id != null && id.equals(oId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        String id = getId();
        if (id != null) {
            return id.hashCode();
        } else {
            return super.hashCode();
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Job#getJob()
     */
    public IJob getJob() {
        return this.p4jJob;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Job#update(com.perforce.p4java.core.IJob)
     */
    public void update(IJob job) throws P4JavaException {
        if (job != null) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                IServer server = connection.getServer();
                if (server != null) {
                    try {
                        server.updateJob(job);
                    } catch (P4JavaError error) {
                        PerforceProviderPlugin.logError(error);
                    }
                }
            }
        }
    }
}
