/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.FileStatOutputOptions;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.P4Event.EventType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4DefaultChangelist extends P4PendingChangelist {

    private String description = null;
    private String username = null;
    private String clientname = null;
    private IClient wrappedClient = null;

    /**
     * @param connection
     * @param client
     * @param needsRefresh
     */
    public P4DefaultChangelist(IP4Connection connection, IClient client,
            boolean needsRefresh) {
        this(connection, client, needsRefresh, false);
    }

    /**
     * @param connection
     * @param client
     * @param needsRefresh
     * @param onClient
     */
    public P4DefaultChangelist(IP4Connection connection, IClient client,
            boolean needsRefresh, boolean onClient) {
        super(connection, null, onClient);
        this.wrappedClient = client;
        this.needsRefresh = needsRefresh;
        if (onClient && this.wrappedClient != null) {
            this.username = this.wrappedClient.getOwnerName();
            this.clientname = this.wrappedClient.getName();
        }
    }

    /**
     * @param connection
     * @param client
     * @param needsRefresh
     * @param onClient
     * @param useConnection
     */
    public P4DefaultChangelist(IP4Connection connection, IClient client,
            boolean needsRefresh, boolean onClient, boolean useConnection) {
        this(connection, client, needsRefresh, onClient);
        if (useConnection && connection != null) {
            this.username = connection.getParameters().getUser();
            this.clientname = connection.getParameters().getClient();
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4PendingChangelist#getAllMembers()
     */
    @Override
    public IP4Resource[] getAllMembers() {
        return members();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getId()
     */
    @Override
    public int getId() {
        return IChangelist.DEFAULT;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#getStatus()
     */
    @Override
    public ChangelistStatus getStatus() {
        return ChangelistStatus.PENDING;
    }

    /**
     * Update the associated user/client for this changelist from the
     * information obtained from the specified file spec
     * 
     * @param file
     */
    public void updateUserClient(IFileSpec file) {
        if (file != null) {
            if (file.getUserName() != null) {
                username = file.getUserName();
            }
            if (file.getClientName() != null) {
                clientname = file.getClientName();
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getName()
     */
    @Override
    public String getName() {
        String name = "Default"; //$NON-NLS-1$
        if (this.clientname != null && this.username != null) {
            name += " - " + this.username + "@" + this.clientname; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return name;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#getClient()
     */
    @Override
    public IClient getClient() {
        if (!isReadOnly() && this.wrappedClient != null) {
            String name = this.wrappedClient.getName();
            if (name != null && name.equals(this.clientname)) {
                return this.wrappedClient;
            }
        }
        return super.getClient();
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#getClientName()
     */
    @Override
    public String getClientName() {
        return this.clientname;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#getUserName()
     */
    @Override
    public String getUserName() {
        return this.username;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        if (isOnClient()) {
            this.description = description;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#isDefault()
     */
    @Override
    public boolean isDefault() {
        return true;
    }

    private boolean userMatchesAction(String user, IExtendedFileSpec spec) {
        boolean matches = false;
        if (user == null) {
            matches = true;
        } else {
            if (isCaseSensitive()) {
                matches = user.equals(spec.getActionOwner());
            } else {
                matches = user.equalsIgnoreCase(spec.getActionOwner());
            }
        }
        return matches;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#refresh()
     */
    @Override
    public void refresh() {
        final IP4Connection connection = getConnection();
        if (isOnClient() && connection != null) {
            // This is run as a client operation even though getChangelist is
            // off P4JServer because a client is required to get a valid default
            // changelist on the currently configure connection's client name
            IP4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    IServer server = client.getServer();
                    if (server != null) {
                        IChangelist defaultP4JList = server
                                .getChangelist(IChangelist.DEFAULT);
                        if (defaultP4JList != null) {
                            setDescription(defaultP4JList.getDescription());
                        }

                        FileStatOutputOptions options = new FileStatOutputOptions();
                        options.setMappedFiles(true);
                        options.setOpenedFiles(true);
                        List<IExtendedFileSpec> files = server
                                .getExtendedFiles(
                                        P4FileSpecBuilder
                                                .makeFileSpecList(new String[] { connection
                                                        .getRootSpec(), }), 0,
                                        -1, 0, options, null);

                        // Specifically check user name against action owner on
                        // resulting file spec in case a different use has this
                        // file checked out on the same workspace
                        String user = getUserName();
                        List<IP4Resource> resources = new ArrayList<IP4Resource>();
                        for (IExtendedFileSpec file : files) {
                            if (isValidFileSpec(file)
                                    && userMatchesAction(user, file)) {
                                IP4File p4File = null;
                                if (!readOnly) {
                                    p4File = getConnection().getFile(file);
                                }
                                if (p4File == null) {
                                    p4File = new P4File(file,
                                            P4DefaultChangelist.this, readOnly);
                                }
                                p4File.setFileSpec(file);
                                resources.add(p4File);
                            }
                        }
                        connection.getOpenedManager().replaceResources(getId(),
                                resources);
                        P4Workspace.getWorkspace().notifyListeners(
                                new P4Event(EventType.REFRESHED,
                                        P4DefaultChangelist.this));
                    }
                }
            };
            runOperation(op);
        }
        this.needsRefresh = false;
    }

    /**
     * @see com.perforce.team.core.p4java.P4PendingChangelist#isShelved()
     */
    @Override
    public boolean isShelved() {
        return false;
    }

    @Override
    public String toString() {
    	return ("P4DefaultChangelist:["+getDescription()+"]").replaceAll("[\n|\r]", ""); //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
    	return obj instanceof P4DefaultChangelist && super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return super.hashCode();
    }

}
