/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.core.IBranchSpecSummary;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.BranchSpec;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.PerforceProviderPlugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4Branch extends P4Resource implements IP4Branch {

    private IP4Connection connection;
    private IBranchSpecSummary branch;

    /**
     * Create a new p4 branch
     * 
     * @param connection
     * @param branch
     * @param needsRefresh
     */
    public P4Branch(IP4Connection connection, IBranchSpecSummary branch,
            boolean needsRefresh) {
        this.connection = connection;
        this.branch = branch;
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
        return this.branch != null ? this.branch.getName() : null;
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
            IP4ServerOperation operation = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    P4Branch.this.branch = server.getBranchSpec(name);
                    needsRefresh = false;
                }
            };
            runOperation(operation);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getAccessTime()
     */
    public Date getAccessTime() {
        return this.branch != null ? this.branch.getAccessed() : null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getDescription()
     */
    public String getDescription() {
        return this.branch != null ? this.branch.getDescription() : null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getOwner()
     */
    public String getOwner() {
        return this.branch != null ? this.branch.getOwnerName() : null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getUpdateTime()
     */
    public Date getUpdateTime() {
        return this.branch != null ? this.branch.getUpdated() : null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getView()
     */
    public ViewMap<IBranchMapping> getView() {
        ViewMap<IBranchMapping> view = null;
        if (this.branch instanceof BranchSpec) {
            view = ((BranchSpec) this.branch).getBranchView();
        }
        return view;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#isLocked()
     */
    public boolean isLocked() {
        return this.branch != null ? this.branch.isLocked() : false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#update(com.perforce.p4java.core.IBranchSpec)
     */
    public void update(IBranchSpec branch) throws P4JavaException {
        if (branch != null) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                IServer server = connection.getServer();
                if (server != null) {
                    try {
                        server.updateBranchSpec(branch);
                    } catch (P4JavaError error) {
                        PerforceProviderPlugin.logError(error);
                    }
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getInterchanges()
     */
    public IP4SubmittedChangelist[] getInterchanges() {
        return getInterchanges(false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getInterchanges(boolean)
     */
    public IP4SubmittedChangelist[] getInterchanges(final boolean reverse) {
        final String name = getName();
        final IP4Connection connection = getConnection();
        final List<IP4SubmittedChangelist> lists = new ArrayList<IP4SubmittedChangelist>();
        if (name != null && connection != null) {
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    List<IChangelist> interchanges = client.getServer()
                            .getInterchanges(name, null, null, false, true, -1,
                                    reverse, false);
                    for (IChangelist list : interchanges) {
                        lists.add(new P4SubmittedChangelist(connection, list));
                    }

                }
            };
            runOperation(operation);
        }
        return lists.toArray(new IP4SubmittedChangelist[lists.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getDiffs()
     */
    public IFileDiff[] getDiffs() {
        return getDiffs(null, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Branch#getDiffs(java.lang.String,
     *      java.lang.String)
     */
    public IFileDiff[] getDiffs(final String sourceFilter,
            final String targetFilter) {
        final List<IFileDiff> diffs = new ArrayList<IFileDiff>();
        final String name = getName();
        if (name != null) {
            IP4ServerOperation operation = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    IFileSpec leftSpec = null;
                    IFileSpec rightSpec = null;
                    if (sourceFilter != null) {
                        leftSpec = new FileSpec(sourceFilter);
                    }
                    if (targetFilter != null) {
                        rightSpec = new FileSpec(targetFilter);
                    }
                    List<IFileDiff> serverDiffs = server.getFileDiffs(leftSpec,
                            rightSpec, name, null, false, false, false);
                    diffs.addAll(serverDiffs);
                }
            };
            runOperation(operation);
        }
        return diffs.toArray(new IFileDiff[diffs.size()]);
    }
}
