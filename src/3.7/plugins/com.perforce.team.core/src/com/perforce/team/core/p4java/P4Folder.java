/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Folder extends P4Container implements IP4Folder {

    private IP4Container parent;
    private String name;
    private IP4Connection connection;
    private String localPath = null;
    private String depotPath = null;

    /**
     * Creates a new p4 folder.
     * 
     * @param connection
     * @param localPath
     * @param depotPath
     * 
     */
    public P4Folder(IP4Connection connection, String localPath, String depotPath) {
        if (connection != null) {
            this.connection = connection;
            this.localPath = localPath;
            this.depotPath = depotPath;
            if (this.depotPath != null) {
                int index = this.depotPath.lastIndexOf('/');
                if (index >= 0 && index + 1 < this.depotPath.length()) {
                    this.name = this.depotPath.substring(index + 1);
                }
            }
            this.parent = connection;
        }
    }

    /**
     * Creates a new p4 folder from a client, parent, and folder name.
     * 
     * @param parent
     * @param name
     */
    public P4Folder(IP4Container parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#edit(int)
     */
    @Override
    public void edit(final int changelist) {
        IClient client = getClient();
        if (client != null) {
            final String action = getActionPath();
            if (action != null) {
                IP4ClientOperation op = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {
                        client.editFiles(P4FileSpecBuilder
                                .makeFileSpecList(new String[] { action }),
                                false, false, changelist, null);
                    }
                };
                runOperation(op);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#delete(int)
     */
    @Override
    public void delete(final int changelist) {
        IClient client = getClient();
        if (client != null) {
            final String action = getActionPath();
            if (action != null) {
                IP4ClientOperation op = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {
                        client.deleteFiles(P4FileSpecBuilder
                                .makeFileSpecList(new String[] { action }),
                                changelist, false);
                    }
                };
                runOperation(op);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#revert()
     */
    @Override
    public void revert() {
        IClient client = getClient();
        if (client != null) {
            final String action = getActionPath();
            if (action != null) {
                IP4ClientOperation op = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {
                        client.revertFiles(P4FileSpecBuilder
                                .makeFileSpecList(new String[] { action }),
                                false, -1, false, false);
                    }
                };
                runOperation(op);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getLocalPath()
     */
    public String getLocalPath() {
        return this.localPath;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getName()
     */
    public String getName() {
    	if(name!=null)
    		return name;

   		String rpath=getRemotePath();
   		if(rpath!=null)
   			return rpath;
   		
    	String lpath = getLocalPath();
   		if(lpath!=null)
   			return lpath;
   		
        return this.name;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getRemotePath()
     */
    public String getRemotePath() {
        if (this.depotPath != null) {
            return this.depotPath;
        } else if (this.parent != null && this.parent.getRemotePath() != null) {
            return this.parent.getRemotePath() + "/" + getName(); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getParent()
     */
    public IP4Container getParent() {
        return this.parent;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String name = getName();
        if (name == null) {
            name = super.toString();
        }
        return name;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClient()
     */
    public IClient getClient() {
        if (this.parent != null) {
            return this.parent.getClient();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getConnection()
     */
    public IP4Connection getConnection() {
        if (this.connection != null) {
            return this.connection;
        } else if (this.parent != null) {
            return this.parent.getConnection();
        }
        return null;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object otherFolder) {
        if (this == otherFolder) {
            return true;
        }
        if (otherFolder instanceof IP4Folder) {
            IP4Folder folder = (IP4Folder) otherFolder;
            if (!connectionEquals(folder)) {
                return false;
            }
            if (this.parent != null && this.parent.equals(folder.getParent())) {
                // Do remote comparison if both have remote path
                if (this.getRemotePath() != null
                        && this.getRemotePath().equals(folder.getRemotePath())) {
                    return true;
                }

                // Do local comparison only if neither have remote paths
                if (this.getRemotePath() == null
                        && folder.getRemotePath() == null) {
                    if (this.getLocalPath() != null
                            && this.getLocalPath()
                                    .equals(folder.getLocalPath())) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        String path = this.getRemotePath();
        if (path == null) {
            path = this.getLocalPath();
        }
        int hash;
        if (path != null) {
            hash = path.hashCode();
        } else {
            hash = super.hashCode();
        }
        return hash;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Folder#getLocalContainers()
     */
    public IContainer[] getLocalContainers() {
        return PerforceProviderPlugin.getLocalContainers(getLocalPath());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Folder#getFirstWhereRemotePath()
     */
    public String getFirstWhereRemotePath() {
        final String[] firstRemotePathMapping = new String[1];
        IClient client = getClient();
        if (client != null) {
            final String path = getLocalPath();
            if (path != null) {
                IP4ClientOperation op = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {
                        List<IFileSpec> specs = P4FileSpecBuilder
                                .makeFileSpecList(new String[] { path });
                        specs = convertToEllipsisSpecs(specs);
                        specs = client.where(specs);
                        specs = P4FileSpecBuilder.getValidFileSpecs(specs);
                        specs = filterUnmapped(specs);
                        if (specs.size() >= 1) {
                            firstRemotePathMapping[0] = convertFromEllipsis(specs
                                    .get(0).getDepotPathString());
                        }
                    }
                };
                runOperation(op);
            }
        }
        return firstRemotePathMapping[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Folder#updateLocation()
     */
    public void updateLocation() {
        final String path = getRemotePath();
        if (path != null) {
            IP4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    List<IFileSpec> specs = P4FileSpecBuilder
                            .makeFileSpecList(new String[] { path });
                    specs = convertToEllipsisSpecs(specs);
                    specs = client.where(specs);
                    
                    // If client's serverID doesn't match server, need to get that
                    // error message to the user rather than silently failing.
                    // If it turns out that there are other sev 3 errors that
                    // shouldn't get thrown here, then this test will need to be
                    // made more specific.
                    if (specs.get(0).getSeverityCode() >= 3)
                    	throw new P4JavaException(specs.get(0).getStatusMessage());
                    
                    specs = P4FileSpecBuilder.getValidFileSpecs(specs);
                    specs = filterUnmapped(specs);

                	/* 
                	 * job056249, job041351
                	 * 
                	 * For regular non-overlay mapping, there is only one spec returned.
                	 * 
                	 * For following overlay mapping:
                	 *   //depot/dev/main/... //ali_play_20131/dev.main/...
                	 *   //depot/dev/main/docs/... //ali_play_20131/docs.in.main/...
                	 * 
                	 * p4 where //depot/dev/main/... will return
                	 *   //depot/dev/main/...        //ali_play_20131/dev.main/...
                	 *   //depot/dev/main/docs/...   //ali_play_20131/dev.main/docs/... (unmapped)
                	 *   //depot/dev/main/docs/...   //ali_play_20131/docs.in.main/...
                	 */
                	for(IFileSpec spec: specs){
                        String newDepotPath = convertFromEllipsis(spec
                                .getDepotPathString());
                        if (path.equals(newDepotPath)) {
                            P4Folder.this.depotPath = newDepotPath;
                            P4Folder.this.localPath = convertFromEllipsis(spec
                                    .getLocalPathString());
                            break;
                        }
                	}
                    
                    // Special case for stream root
                    if(P4Folder.this.depotPath==null){
                        String stream = P4Folder.this.getConnection().getClient().getStream();
                        if(stream!=null){
                        	IP4Stream p4stream = P4Folder.this.getConnection().getStream(stream);
                        	if(p4stream!=null){
                        		IStreamSummary summary = p4stream.getStreamSummary();
                        		if(summary!=null){
		                        	if(StringUtils.equals(StringUtils.stripEnd(summary.getStream(),"/"),StringUtils.stripEnd(path,"/"))){
		                        		PerforceProviderPlugin.logInfo("Import stream as project...");
		                                P4Folder.this.depotPath = path;
		                                P4Folder.this.localPath = P4Folder.this.getConnection().getClientRoot();
		                        	}
                        		}
                        	}
                        }
                    }
                }
            };
            runOperation(op);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Folder#getCompleteHistory(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IP4ChangelistRevision[] getCompleteHistory(IProgressMonitor monitor) {
        final Map<Integer, P4ChangelistRevision> revisions = new HashMap<Integer, P4ChangelistRevision>();
        monitor.setTaskName(MessageFormat
                .format(Messages.P4Folder_1, getName()));
        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                String path = getActionPath();
                if (path != null) {
                    List<IFileSpec> specs = P4FileSpecBuilder
                            .makeFileSpecList(path);
                    Map<IFileSpec, List<IFileRevisionData>> history = server
                            .getRevisionHistory(specs, 0, false, false, true,
                                    false);
                    if (history != null) {
                        for (List<IFileRevisionData> data : history.values()) {
                            if (data != null) {
                                for (IFileRevisionData revision : data) {
                                    P4ChangelistRevision changelistRev = revisions
                                            .get(revision.getChangelistId());
                                    if (changelistRev == null) {
                                        changelistRev = new P4ChangelistRevision(
                                                getConnection());
                                        revisions.put(
                                                revision.getChangelistId(),
                                                changelistRev);
                                    }
                                    changelistRev.add(revision);
                                }
                            }
                        }
                    }
                }
            }
        };
        runOperation(operation);

        return revisions.values().toArray(
                new IP4ChangelistRevision[revisions.size()]);
    }

}
