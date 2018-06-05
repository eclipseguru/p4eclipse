/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.FileStatOutputOptions;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ShelvedChangelist extends P4Changelist implements
        IP4ShelvedChangelist {

    /**
     * Create a p4 shelved changelist
     * 
     * @param connection
     * @param changelist
     * @param readOnly
     */
    public P4ShelvedChangelist(IP4Connection connection,
            IChangelist changelist, boolean readOnly) {
        super(connection, changelist);
        this.readOnly = readOnly;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getFiles()
     */
    public IP4Resource[] getFiles() {
        List<IP4Resource> files = new ArrayList<IP4Resource>();
        for (IP4Resource resource : members()) {
            if (resource instanceof IP4ShelveFile) {
                files.add(resource);
            }
        }
        return files.toArray(new IP4Resource[files.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof IP4ShelvedChangelist && super.equals(obj);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#refresh()
     */
    @Override
    public void refresh() {
        final IP4Connection connection = getConnection();
        final int id = getId();
        if (id > 0 && connection != null) {
            // This is run as a client operation even though getChangelist is
            // off P4JServer because a client is required to get a valid default
            // changelist on the currently configure connection's client name
            IP4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    IServer server = client.getServer();
                    if (server != null) {
                        Set<IP4Resource> resources = new HashSet<IP4Resource>();

                        FileStatOutputOptions options = new FileStatOutputOptions();
                        options.setShelvedFiles(true);
                        List<IExtendedFileSpec> files = server
                                .getExtendedFiles(
                                        P4FileSpecBuilder
                                                .makeFileSpecList(new String[] { DEPOT_PREFIX
                                                        + ELLIPSIS, }), 0, -1,
                                        id, options, null);

                        for (IExtendedFileSpec file : files) {
                            if (isValidFileSpec(file)) {
                                IP4File p4File = new P4File(file,
                                        P4ShelvedChangelist.this, true);
                                IP4ShelveFile shelveFile = new P4ShelveFile(
                                        getChangelist(), p4File, readOnly);
                                resources.add(shelveFile);
                            }
                        }
                        cachedFiles = resources;
                    }
                }
            };
            runOperation(op);
        }
        this.needsRefresh = false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelvedChangelist#unshelve()
     */
    public IFileSpec[] unshelve() {
        return unshelve(-1);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelvedChangelist#unshelve(int)
     */
    public IFileSpec[] unshelve(int toChangelist) {
        return unshelve(null, toChangelist);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelvedChangelist#unshelve(com.perforce.team.core.p4java.IP4Resource[])
     */
    public IFileSpec[] unshelve(IP4Resource[] files) {
        return unshelve(files, -1);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelvedChangelist#unshelve(com.perforce.team.core.p4java.IP4Resource[],
     *      int)
     */
    public IFileSpec[] unshelve(IP4Resource[] files, final int toChangelist) {
        return unshelve(files, toChangelist, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelvedChangelist#unshelve(com.perforce.team.core.p4java.IP4Resource[],
     *      int, boolean)
     */
    public IFileSpec[] unshelve(IP4Resource[] files, final int toChangelist,
            final boolean overwrite) {
        final int id = getId();
        final List<IFileSpec> unshelveSpecs = new ArrayList<IFileSpec>();
        if (id > 0) {
            final List<IFileSpec> specs = new ArrayList<IFileSpec>();
            if (files != null) {
                List<String> actionPaths = new ArrayList<String>();
                for (IP4Resource file : files) {
                    String path = file.getActionPath(Type.REMOTE);
                    if (path != null) {
                        actionPaths.add(path);
                    }
                }
                specs.addAll(P4FileSpecBuilder.makeFileSpecList(actionPaths
                        .toArray(new String[actionPaths.size()])));
            }
            IP4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    List<IFileSpec> output = client.unshelveChangelist(id,
                            specs, toChangelist, overwrite, false);
                    unshelveSpecs.addAll(output);
                    handleErrors(output.toArray(new IFileSpec[output.size()]));
                }
            };
            runOperation(op);
        }
        return unshelveSpecs.toArray(new IFileSpec[unshelveSpecs.size()]);
    }
    
    @Override
    public String toString() {
    	return ("P4ShelvedChangelist:["+getDescription()+"]").replaceAll("[\n|\r]", ""); //$NON-NLS-1$
    }

}
