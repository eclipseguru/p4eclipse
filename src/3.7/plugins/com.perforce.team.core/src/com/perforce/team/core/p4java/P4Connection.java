/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.core.IBranchSpecSummary;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.core.IDepot.DepotType;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.p4java.core.IFix;
import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.p4java.core.ILabel;
import com.perforce.p4java.core.ILabelSummary;
import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.Label;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.impl.mapbased.rpc.OneShotServerImpl;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.p4java.option.client.CopyFilesOptions;
import com.perforce.p4java.option.client.IntegrateFilesOptions;
import com.perforce.p4java.option.client.MergeFilesOptions;
import com.perforce.p4java.option.client.PopulateFilesOptions;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.p4java.option.server.GetClientsOptions;
import com.perforce.p4java.option.server.GetStreamsOptions;
import com.perforce.p4java.option.server.MatchingLinesOptions;
import com.perforce.p4java.option.server.ReloadOptions;
import com.perforce.p4java.option.server.StreamOptions;
import com.perforce.p4java.option.server.UnloadOptions;
import com.perforce.p4java.server.AuthTicketsHelper;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Connection extends P4Resource implements IP4Connection {

    /**
     * Should the path be converted? This is required if on windows and using a
     * path that contains the device separator
     * 
     * @param path
     * @return - true if the path should be converted
     */
    public static boolean shouldConvertPath(String path) {
        return path != null && P4CoreUtils.isWindows()
                && path.indexOf(IPath.DEVICE_SEPARATOR) != -1;
    }

    /**
     * Convert the path using the {@link File#getCanonicalPath()} method but
     * checking that the name doesn't change
     * 
     * @param localPath
     * @return - converted path
     */
    // TODO add the logic to convert upper case disk label to lower case.
    public static String convertPath(String localPath) {
        // This method designed to be used on Windows to fix case issues where
        // multiple files exist in the same depot folder and only differ in
        // case. Fix for job032688
        if (localPath == null) {
            return null;
        }

        String local=convertDiskLabel(localPath);
        
        File file = new File(local);
        try {
            String converted = file.getCanonicalPath();
            int nameIndex = converted.lastIndexOf(File.separatorChar);
            if (nameIndex > -1 && nameIndex + 1 < converted.length()) {
                String fileName = file.getName();
                String localName = converted.substring(nameIndex + 1);
                if (fileName.equals(localName)) {
                    local = converted;
                }
            }
        } catch (IOException e) {
            PerforceProviderPlugin.logError(e);
        }
        return local;
    }

    private ConnectionParameters params = null;

    /**
     * Current p4j client
     */
    protected IClient client = null;
    private String currentDirectory = null;

    /**
     * Current p4j server
     */
    protected IServer server = null;

    private ServerNotSupportedException serverException = null;
    private IJobSpec jobSpec = null;
    private int currentPending = -1;

    /**
     * Current p4j server info
     */
    protected IServerInfo serverInfo;

    private P4Depot[] cached = null;
    private P4Depot specDepot = null;

    /**
     * Is the server set to be in offline mode?
     */
    protected boolean offline = false;

    /**
     * Is the connection connected defined by the logic in the
     * {@link #connect()} method
     */
    protected boolean connected = false;

    private boolean disposed = false;
    private boolean showClientOnly = false;
    private boolean showDeleted = false;
    private long loginTime = -1L;
    private boolean loggedIn = false;
    private int serverVersion = 0;
    private PendingResourceManager openedManager = new PendingResourceManager();
    private Map<Integer, IP4PendingChangelist> changelists = Collections
            .synchronizedMap(new HashMap<Integer, IP4PendingChangelist>());
    private Map<String, IP4Resource> resources = Collections
            .synchronizedMap(new HashMap<String, IP4Resource>());

    private Map<String, Object> attributes = Collections.synchronizedMap(new HashMap<String, Object>());

    /**
     * Creates a new p4 connection from a connection parameters object
     * 
     * @param params
     */
    public P4Connection(ConnectionParameters params) {
        if (params == null) {
            params = new ConnectionParameters();
        }
        this.params = params;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getResource(java.lang.String)
     */
    public IP4Resource getResource(String localOrDepotPath) {
        if (shouldConvertPath(localOrDepotPath)) {
            localOrDepotPath = convertPath(localOrDepotPath);
        }
        IP4Resource res = this.resources.get(localOrDepotPath);
        // this is where a stale resource is cached: job069662
        if(res!=null && res.getRemotePath()==null && res.getLocalPath()==null){
        	this.resources.remove(localOrDepotPath);
        	return null;
        }
        return res;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFiles(com.perforce.p4java.core.file.IFileSpec[])
     */
	public IP4File[] getFiles(IFileSpec[] specs) {
        IP4File[] p4Files = new IP4File[0];
        if (specs != null) {
            p4Files = new IP4File[specs.length];
            List<IP4File> toNotify=new ArrayList<IP4File>();
            
            for(int i=0;i<specs.length;i++){
	            IP4File testFile = new P4File(specs[i], null);
	            IP4Resource resource = getResource(testFile.getActionPath());
	            if (resource instanceof IP4File) {
	                p4Files[i] = (IP4File) resource;
	            } else {
	                p4Files[i] = new P4File(specs[i], this);
	                if(updateResourceThenCheckNotify(p4Files[i])){
	                	toNotify.add(p4Files[i]);
	                }
	            }
            }
            if(toNotify.size()>0){
                P4Workspace.getWorkspace().notifyListeners(
                        new P4Event(EventType.ADDED, toNotify.toArray(new IP4File[0])));
            }
        }
        return p4Files;
	}
    

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFile(com.perforce.p4java.core.file.IFileSpec)
     */
    public IP4File getFile(IFileSpec spec) {
        IP4File p4File = null;
        if (spec != null) {
            IP4File testFile = new P4File(spec, null);
            IP4Resource resource = getResource(testFile.getActionPath());
            if (resource instanceof IP4File) {
                p4File = (IP4File) resource;
            } else {
                p4File = new P4File(spec, this);
                updateResource(p4File);
            }
        }
        return p4File;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFile(java.lang.String)
     */
    public IP4File getFile(String localOrDepotPath) {
        IP4File p4File = null;
        IP4Resource resource = getResource(localOrDepotPath);
        if (resource instanceof IP4File) {
            p4File = (IP4File) resource;
        } else {
            p4File = new P4File(new FileSpec(localOrDepotPath), this);
            p4File.refresh();
            updateResource(p4File);
        }
        return p4File;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFolder(java.lang.String)
     */
    public IP4Folder getFolder(String depotPath) {
        return getFolder(depotPath, true);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFolder(java.lang.String,
     *      boolean)
     */
    public IP4Folder getFolder(String depotPath, boolean refreshIfNotFound) {
        IP4Folder folder = null;
        IP4Resource resource = this.resources.get(depotPath);
        if (resource instanceof IP4Folder) {
            folder = (IP4Folder) resource;
        } else {
            folder = new P4Folder(this, null, depotPath);
            if (refreshIfNotFound) {
                folder.updateLocation();
                updateResource(folder);
                folder.refresh();
            } else {
                updateResource(folder);
            }
        }
        return folder;
    }

    // private boolean localPathMatches(String path, IP4Resource resource) {
    // boolean matches = false;
    // if (resource != null) {
    // String foundLocal = resource.getLocalPath();
    // if (shouldConvertPath(foundLocal)) {
    // foundLocal = convertPath(foundLocal);
    // }
    // matches = path.equals(foundLocal);
    // }
    // return matches;
    // }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getResource(org.eclipse.core.resources.IResource)
     */
    public IP4Resource getResource(IResource resource) {
        if (resource == null) {
            return null;
        }
        IPath resourcePath = resource.getLocation();
        if (resourcePath == null) {
            return null;
        }
        String path = resourcePath.makeAbsolute().toOSString();

        String convertedPath = path;
        if (shouldConvertPath(path)) {
            convertedPath = convertPath(path);
        }
        IP4Resource p4Resource = getResource(convertedPath);
        if (p4Resource == null) {
            if (resource instanceof IContainer) {
                p4Resource = new P4Folder(this, path, null);
                updateResource(p4Resource);
                p4Resource.refresh();
            } else if (resource instanceof IFile) {
                IContainer parent = resource.getParent();
                // If parent is null that means the resource is the workspace
                // root which shouldn't be loaded since only projects and
                // resources under projects should be loaded
                if (parent != null) {
                    IP4Resource parentResource = getResource(parent);
                    if (parentResource instanceof IP4Container) {
                        IP4Container folder = (IP4Container) parentResource;

                        // If parent is out of date then refresh it
                        if (folder.needsRefresh()) {
                            folder.refresh();
                        }

                        // Try to obtain the file again as it may have been
                        // loaded via the call to getResource with the parent
                        // specified
                        p4Resource = getResource(convertedPath);

                    }

                    // This is the case where fstat with /* on the parent
                    // directory didn't return a valid file spec for the path
                    
                    
                    // This is the case when adding a new file to the folder, but not marked as add to p4depot
                    // not update is needed.
                    
                    if (p4Resource == null) {
                        p4Resource = new P4File(this, path);
                        // updateResource(p4Resource);// avoid update new file 

                        // Fix for job036584, if we are dealing with a linked
                        // file that is not in a linked folder that is not
                        // already known then try to fstat the file directly
                        //
                        // Linked files not in linked folders will have resource
                        // parents that don't have the same base path so
                        // although the parent is loaded the file will still not
                        // be found in the fstat output and that is why a direct
                        // fstat of the file is performed.
                        if (resource.isLinked()) {
                            p4Resource.refresh();
                        }

                    }
                }
            }
        }
        return p4Resource;
    }
    
    /**
     * @see com.perforce.team.core.p4java.IP4Connection#updateResources(com.perforce.team.core.p4java.IP4Resource[])
     */
    public void updateResources(IP4Resource[] resources) {
    	List<IP4Resource> newAdded=new ArrayList<IP4Resource>();
    	for(IP4Resource resource:resources){
	        if (updateResourceThenCheckNotify(resource)) {
	        	newAdded.add(resource);
	        }
    	}
    	if(newAdded.size()>0){
    		P4Workspace.getWorkspace().notifyListeners(
    			new P4Event(EventType.ADDED, newAdded.toArray(new IP4Resource[0])));
    	}
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#updateResource(com.perforce.team.core.p4java.IP4Resource)
     */
    public void updateResource(IP4Resource resource) {
        if (updateResourceThenCheckNotify(resource)) {
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.ADDED, resource));
        }
    }

    public boolean updateResourceThenCheckNotify(IP4Resource resource) {
    	boolean notify = false;
        if (resource != null && !resource.isReadOnly()) {
            String local = resource.getLocalPath();
            String depot = resource.getRemotePath();
            if (local != null) {

                if (shouldConvertPath(local)) {
                    local = convertPath(local);
                }

                if (!resources.containsKey(local)) {
                    notify = true;
                }
                updateCache(local, resource);
            }
            if (depot != null) {
                if (!resources.containsKey(depot)) {
                    notify = true;
                }
                updateCache(depot, resource);
            }
        }
        return notify;
    }

    /**
     * Gets the user for this connection
     * 
     * @return - user
     */
    public String getUser() {
        if (this.server != null) {
            return this.server.getUserName();
        } else if (this.params != null) {
            return this.params.getUserNoNull();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#findFiles(java.lang.String)
     */
    public IP4File[] findFiles(String startsWithPath) {
        List<IP4File> found = new ArrayList<IP4File>();
        if (startsWithPath != null) {
            if (startsWithPath.endsWith(IP4Container.DIR_ELLIPSIS)) {
                // Add one to include the separator so that
                // //depot/folder/b/* does not match //depot/folder/buffer.txt
                startsWithPath = startsWithPath.substring(
                        0,
                        startsWithPath.length()
                                - IP4Container.DIR_ELLIPSIS.length() + 1);
            } else if (startsWithPath.endsWith(IP4Container.REMOTE_ELLIPSIS)) {
                // Add one to include the separator so that
                // //depot/folder/b/* does not match //depot/folder/buffer.txt
                startsWithPath = startsWithPath.substring(
                        0,
                        startsWithPath.length()
                                - IP4Container.REMOTE_ELLIPSIS.length() + 1);
            }
            if (shouldConvertPath(startsWithPath)) {
                startsWithPath = convertPath(startsWithPath);
            }
            String[] keys = null;
            synchronized (resources) {
                keys = resources.keySet().toArray(new String[0]);
            }
            for (String key : keys) {
                if (key.startsWith(startsWithPath)) {
                    IP4Resource resource = getResource(key);
                    if (resource instanceof IP4File) {
                        found.add((IP4File) resource);
                    }
                }
            }
        }
        return found.toArray(new IP4File[found.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#members()
     */
    public IP4Resource[] members() {
        if (isOffline()) {
            return new IP4Resource[0];
        } else if (this.needsRefresh && this.cached == null) {
            refresh();
        }
        return this.cached != null ? this.cached : new IP4Resource[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#size()
     */
    public int size() {
        return this.cached != null ? this.cached.length : 0;
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
        if (this.params != null) {
            StringBuilder name = new StringBuilder(this.params.getPortNoNull());
            name.append(',');
            name.append(' ');
            name.append(this.params.getClientNoNull());
            name.append(',');
            name.append(' ');
            name.append(this.params.getUserNoNull());

            String charset = this.params.getCharsetNoNone();
            if (charset != null) {
                name.append(',');
                name.append(' ');
                name.append(charset);
            }
            return name.toString();
        } else {
            return null;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getParent()
     */
    public IP4Container getParent() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getRemotePath()
     */
    public String getRemotePath() {
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String value = getName();
        if (value == null) {
            value = super.toString();
        }
        return "("+value+")";
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServer()
     */
    @Override
    public IServer getServer() {
        if (this.server == null) {
            refreshServer();
        }
        return this.server;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClient()
     */
    public IClient getClient() {
        if (this.client == null) {
            connect();
        }
        return this.client;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getConnection()
     */
    public IP4Connection getConnection() {
        return this;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath()
     */
    public String getActionPath() {
        return ""; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath(com.perforce.team.core.p4java.IP4Resource.Type)
     */
    public String getActionPath(Type type) {
        return getActionPath();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object otherConnection) {
        if (this == otherConnection) {
            return true;
        } else if (otherConnection instanceof IP4Connection) {
            if (this.params != null
                    && this.params.equals(((P4Connection) otherConnection)
                            .getParameters())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
    	if(this.params!=null)
    		return this.params.hashCode();
    	
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(int)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(int size) {
        return getSubmittedChangelists((String[]) null, size);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists()
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists() {
        return getSubmittedChangelists(0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String[])
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String[] paths) {
        return getSubmittedChangelists(paths, 0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String[],
     *      int)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String[] paths,
            int size) {
        return getSubmittedChangelists(paths, size, null, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String[],
     *      int, java.lang.String, java.lang.String)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(
            final String[] paths, final int size, final String user,
            final String clientWorkspace) {
        final List<IP4SubmittedChangelist> changes = new ArrayList<IP4SubmittedChangelist>();
        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                IServer server = client.getServer();
                List<IChangelistSummary> changelists = server.getChangelists(
                        size, P4FileSpecBuilder.makeFileSpecList(paths),
                        clientWorkspace, user, false, true, false, true);
                for (IChangelistSummary change : changelists) {
                    IP4SubmittedChangelist changelist = new P4SubmittedChangelist(
                            P4Connection.this, new Changelist(change, server,
                                    false));
                    changes.add(changelist);
                }
            }
        };
        runOperation(op);
        return changes.toArray(new IP4SubmittedChangelist[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String path) {
        return getSubmittedChangelists(path, 0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String,
     *      int)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String path,
            int size) {
        if (path == null) {
            return getSubmittedChangelists(size);
        } else {
            return getSubmittedChangelists(new String[] { path }, size);
        }
    }

    /**
     * Does the username from the client match the file spec?
     * 
     * @param spec
     * @return - true if spec username and connection username match
     */
    protected boolean userMatches(IFileSpec spec) {
        return isOwner(spec.getUserName());
    }

    /**
     * Does the clientname from the client match the file spec?
     * 
     * @param spec
     * @return - true if spec client name and params client name match
     */
    protected boolean clientMatches(IFileSpec spec) {
        String client = spec.getClientName();
        if (client != null) {
            if (isCaseSensitive()) {
                return client.equals(this.params.getClient());
            } else {
                return client.equalsIgnoreCase(this.params.getClient());
            }
        }
        return false;
    }

    /**
     * Does the username and clientname from the client match the file spec?
     * 
     * @param spec
     * @return - true if spec username/clientname and client username/clientname
     *         match
     */
    protected boolean userClientMatches(IFileSpec spec) {
        return clientMatches(spec) && userMatches(spec);
    }

    /**
     * Does the username from the client match the changelist?
     * 
     * @param list
     * @param client
     * @return - true if changelist username and client username match
     */
    protected boolean clientMatches(IChangelist list, IClient client) {
        String clientId = list.getClientId();
        if (clientId != null) {
            if (isCaseSensitive()) {
                return clientId.equals(client.getName());
            } else {
                return clientId.equalsIgnoreCase(client.getName());
            }
        }
        return false;
    }

    /**
     * Does the clientname from the client match the changelist?
     * 
     * @param list
     * @param client
     * @return - true if changelist clientname and client client match
     */
    protected boolean userMatches(IChangelist list, IClient client) {
        String user = list.getUsername();
        if (user != null) {
            if (isCaseSensitive()) {
                return user.equals(client.getOwnerName());
            } else {
                return user.equalsIgnoreCase(client.getOwnerName());
            }
        }
        return false;
    }

    /**
     * Does the username and clientname from the client match the changelist?
     * 
     * @param list
     * @param client
     * @return - true if changelist username/clientname and client
     *         username/clientname match
     */
    protected boolean userClientMatches(IChangelist list, IClient client) {
        return userMatches(list, client) && clientMatches(list, client);
    }

    /**
     * Refresh the files missing which are files that were previously in the
     * cache but have been removed outside the p4java bridge layer.
     * 
     * This occurs if a revert of a file is done outside of p4eclipse and then
     * the p4 pending changelist view is refreshed and the file contained in the
     * connection cache should be fstat'ed to get the latest status
     * 
     * @param previous
     * @param current
     */
    private void refreshMissingFiles(IP4PendingChangelist[] previous,
            Set<IP4File> current) {
        if (previous != null && current != null) {
            Set<IP4File> missing = new HashSet<IP4File>();
            for (IP4PendingChangelist list : previous) {
                IP4File[] files = list.getPendingFiles();
                for (IP4File file : files) {
                    if (!current.contains(file)) {
                        missing.add(file);
                    }
                }
            }
            if (!missing.isEmpty()) {
                // Refresh missing files
                P4Collection collection = new P4Collection(
                        missing.toArray(new IP4File[0]));
                collection.refresh();
            }
        }
    }

    /**
     * Refresh the new files which are files that were previously not in the
     * cache but have been added outside the p4java bridge layer.
     * 
     * This occurs if an edit of a file is done outside of p4eclipse and then
     * the p4 pending changelist view is refreshed and the file contained in the
     * connection cache should be fstat'ed to get the latest status
     * 
     * @param newFiles
     */
    private void refreshNewFiles(Set<IP4File> newFiles) {
        if (!newFiles.isEmpty()) {
            // Refresh missing files
            P4Collection collection = new P4Collection(
                    newFiles.toArray(new IP4File[0]));
            collection.refresh();
        }
    }

    private String hashChangelist(String client, String user, int id) {
        String hash = user + "@" + client + "@" + id; //$NON-NLS-1$ //$NON-NLS-2$
        if (!isCaseSensitive()) {
            hash = hash.toLowerCase();
        }
        return hash;
    }

    private void loadExistingChangelist(IClient p4jClient, String clientName,
            String userName, Map<String, IP4PendingChangelist> namesToLists,
            Map<Integer, IP4PendingChangelist> lists) throws P4JavaException {
        IServer server = p4jClient.getServer();
        // Add non-default changelists
        List<IChangelistSummary> changelists = server.getChangelists(0, null,
                clientName, userName, true, false, true, true);
        for (IChangelistSummary change : changelists) {
            IChangelist fullChange = new Changelist(change, server, false);
            String hash = hashChangelist(change.getClientId(),
                    change.getUsername(), change.getId());
            boolean onClient = clientMatches(fullChange, p4jClient)
                    && userMatches(fullChange, p4jClient);

            IP4PendingChangelist changelist = new P4PendingChangelist(this,
                    fullChange, onClient);
            namesToLists.put(hash, changelist);
            if (onClient) {
                lists.put(changelist.getId(), changelist);
            }
        }
    }

    private void loadOpenedFiles(IClient p4jClient, boolean all,
            Map<String, IP4PendingChangelist> namesToLists,
            Set<IP4File> current, Set<IP4File> newFiles) throws P4JavaException {
        List<IFileSpec> openedFiles = p4jClient.getServer().getOpenedFiles(
                new ArrayList<IFileSpec>(), all,
                all ? null : p4jClient.getName(), 0, IChangelist.UNKNOWN);
        openedFiles = P4FileSpecBuilder.getValidFileSpecs(openedFiles);
        if (!openedFiles.isEmpty()) {
            for (IFileSpec file : openedFiles) {
                boolean userMatches = userMatches(file);
                boolean clientMatches = clientMatches(file);
                IP4File p4File = null;
                if (userMatches && clientMatches) {
                    p4File = getFile(file);
                } else {
                    p4File = new P4File(file, this, true);
                }
                if (p4File != null) {
                    current.add(p4File);
                    if (userMatches && clientMatches && !p4File.isOpened()) {
                        newFiles.add(p4File);
                    }
                }

                String hash = hashChangelist(file.getClientName(),
                        file.getUserName(), file.getChangelistId());
                IP4PendingChangelist list = namesToLists.get(hash);

                // If list doesn't already exist and id is zero then
                // this must be a default changelist that needs to be
                // added to the cache
                if (list == null
                        && file.getChangelistId() == IChangelist.DEFAULT) {
                    if (userMatches && clientMatches) {
                        list = new P4DefaultChangelist(this, p4jClient, false,
                                true);
                    } else {
                        list = new P4DefaultChangelist(this, null, false, false);
                    }
                    ((P4DefaultChangelist) list).updateUserClient(file);
                    namesToLists.put(hash, list);
                }

                // Add file to changelist
                if (list != null && p4File != null) {
                    list.addFile(p4File);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelists(boolean)
     */
    public IP4PendingChangelist[] getPendingChangelists(boolean all) {
        IClient client = getClient();
        boolean retry = true;
        while (retry && client != null) {
            retry = false;
            IP4PendingChangelist[] previousLists = this.changelists.values()
                    .toArray(new IP4PendingChangelist[0]);
            Map<Integer, IP4PendingChangelist> newLists = new HashMap<Integer, IP4PendingChangelist>();
            String clientName = all ? null : client.getName();
            String userName = null;
            try {
                Map<String, IP4PendingChangelist> namesToLists = new HashMap<String, IP4PendingChangelist>();

                // Add non-default changelists
                loadExistingChangelist(client, clientName, userName,
                        namesToLists, newLists);

                // Add default changelist manually
                if (!newLists.containsKey(IChangelist.DEFAULT)) {
                    P4DefaultChangelist defaultChangelist = new P4DefaultChangelist(
                            this, client, false, true, true);
                    newLists.put(IChangelist.DEFAULT, defaultChangelist);
                    String hash = hashChangelist(getParameters().getClient(),
                            getParameters().getUser(), IChangelist.DEFAULT);
                    namesToLists.put(hash, defaultChangelist);
                }

                // Set cached map here after all changelists have been loaded
                // but before any files are processed so any infinite recursion
                // from files requesting changelist information is avoided
                this.changelists = newLists;

                // Load files and update ones that are new or missing
                Set<IP4File> current = new HashSet<IP4File>();
                Set<IP4File> newFiles = new HashSet<IP4File>();

                loadOpenedFiles(client, all, namesToLists, current, newFiles);

                P4DefaultChangelist defaultChangelist = (P4DefaultChangelist) newLists
                        .get(0);
                if (defaultChangelist != null) {
                    defaultChangelist.refresh();
                }

                // Refresh missing files that were changed outside of p4eclipse
                refreshMissingFiles(previousLists, current);

                // Refresh files that are currently reported as open but the
                // last fstat claims they are not opened, this should only be
                // done for files associated with the current connection's user
                // and client workspace
                refreshNewFiles(newFiles);

                return namesToLists.values()
                        .toArray(new P4PendingChangelist[0]);
            } catch (P4JavaException e) {
                retry = handleError(e);
                if (retry) {
                    client = getClient();
                }
                PerforceProviderPlugin.logError(e);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return new P4PendingChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isOffline()
     */
    public boolean isOffline() {
        return this.offline;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isOffline()
     */
    public boolean isSandbox() {
        if (serverInfo == null)
            refresh();
        return serverInfo != null && serverInfo.getSandboxVersion() != null;
    }
    
    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isConnected()
     */
    public boolean isConnected() {
        return this.connected && this.client != null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isContainer()
     */
    public boolean isContainer() {
        return true;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#refresh()
     */
    public void refresh() {
        if (!isOffline()) {
            IServer server = getServer();
            if (server != null) {
                try {
                	refreshClient();
                    this.serverInfo = server.getServerInfo();
                    this.serverVersion = parseServerVersion(getVersion());
                } catch (P4JavaException e) {
                    handleError(e);
                    PerforceProviderPlugin.logError(e);
                } catch (P4JavaError e) {
                    PerforceProviderPlugin.logError(e);
                }

                final List<IDepot> metadata = new ArrayList<IDepot>();

                IP4ServerOperation depotOp = new P4ServerOperation() {

                    public void run(IServer server) throws P4JavaException,
                            P4JavaError {
                        metadata.addAll(server.getDepots());
                    }
                };
                runOperation(depotOp);

                P4Depot[] depots = new P4Depot[metadata.size()];
                for (int i = 0; i < depots.length; i++) {
                    IDepot depot = metadata.get(i);
                    depots[i] = new P4Depot(depot, this);
                    if (DepotType.SPEC == depot.getDepotType()) {
                        this.specDepot = depots[i];
                    }
                }
                cached = depots;
            }
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.REFRESHED, this));
        }
        needsRefresh = false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh(int)
     */
    public void refresh(int depth) {
        refresh();
    }

    /**
     * Get the unsupported exception. The same exception object will be returned
     * for all calls
     * 
     * @return - single exception object
     */
    protected ServerNotSupportedException createUnsupportedException() {
        if (serverException == null) {
            serverException = new ServerNotSupportedException(
                    "Only server versions " + MINIMUM_SERVER_LABEL //$NON-NLS-1$
                            + "+ are supported by P4Eclipse"); //$NON-NLS-1$
        }
        return serverException;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#connect()
     */
    public void connect() {
        IServer server = getServer();
        if (server != null) {
            boolean retry = true;
            while (retry) {
                retry = false;
                try {
                    refreshClient();
                    this.serverInfo = server.getServerInfo();
                    this.serverVersion = parseServerVersion(getVersion());
                    if (getVersion() != null && !isSupported()) {
                        throw createUnsupportedException();
                    }
                    if (this.client != null) {
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
     * @see com.perforce.team.core.p4java.IP4Connection#getClientName()
     */
    public String getClientName() {
        if (this.client != null) {
            return this.client.getName();
        } else if (this.params != null) {
            return this.params.getClientNoNull();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getClientRoot()
     */
    public String getClientRoot() {
        if (this.client != null) {
            return this.client.getRoot();
        }
        return null;
    }

    /**
     * parse server version string.
     * 
     * @param ver
     *            the version string
     * @return the server version as an integer i.e 2004.2 = 20042
     */
    protected int parseServerVersion(String ver) {
        int version = -1;
        if (ver != null) {
            int idx = ver.indexOf('/') + 1;
            int start = ver.indexOf('/', idx) + 1;
            int end = ver.indexOf('/', start);
            String s = ver.substring(start, end);
            // Internal beta versions can have version strings like:
            // 2005.2.r05.2_nightly
            if (s.length() > 6) {
                s = s.substring(0, 6);
            }
            version = (int) (Float.parseFloat(s) * 10);
        }
        return version;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getVersion()
     */
    public String getVersion() {
        if (this.serverInfo != null) {
            return this.serverInfo.getServerVersion();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isSecure()
     */
    public boolean isSecure() {
        // TODO how does p4java know secure connections?
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#login(java.lang.String)
     */
    public boolean login(String password) {
        if (!this.isOffline() && !this.disposed) {
            IServer server = getServer();
            if (server != null) {
                try {
                    if (password == null && this.params != null
                            && this.params.savePassword()) {
                        password = this.params.getPassword();
                    }
                    if (password != null) {
                        server.login(password);
                        this.loggedIn = true;
                        this.loginTime = System.currentTimeMillis();
                        return true;
                    }
                } catch (P4JavaException e) {
                    PerforceProviderPlugin.logError(e);
                } catch (P4JavaError e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isLoggedIn()
     */
    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#markLoggedOut()
     */
    public void markLoggedOut() {
        this.loggedIn = false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#logout()
     */
    public void logout() {
        if (this.server != null) {
            try {
                this.server.logout();
            } catch (P4JavaException e) {
                PerforceProviderPlugin.logError(e);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#setOffline(boolean)
     */
    public void setOffline(boolean offline) {
        this.offline = offline;
        if (this.offline) {
            // Clear server/client when going offline so it will be re-built the
            // next time connect() is called.
            // This also ensures that any operations that are currently looping
            // will break out of the loop once the connection is set to offline
            this.server = null;
            this.client = null;
            this.connected = false;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getDate()
     */
    public String getDate() {
        if (this.serverInfo != null) {
            return this.serverInfo.getServerDate();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLicense()
     */
    public String getLicense() {
        if (this.serverInfo != null) {
            return this.serverInfo.getServerLicense();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getRoot()
     */
    public String getRoot() {
        if (this.serverInfo != null) {
            return this.serverInfo.getServerRoot();
        }
        return null;
    }

    public IServerInfo getServerInfo() {
        return this.serverInfo;
    }
    
    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfoClientRoot()
     */
    public String getServerInfoClientRoot() {
        if (this.serverInfo != null) {
            return this.serverInfo.getClientRoot();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfoClientAddress()
     */
    public String getServerInfoClientAddress() {
        if (this.serverInfo != null) {
            return this.serverInfo.getClientAddress();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfoClientHost()
     */
    public String getServerInfoClientHost() {
        if (this.serverInfo != null) {
            return this.serverInfo.getClientHost();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfoClientName()
     */
    public String getServerInfoClientName() {
        if (this.serverInfo != null) {
            return this.serverInfo.getClientName();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getAddress()
     */
    public String getAddress() {
        if (this.serverInfo != null) {
            return this.serverInfo.getServerAddress();
        } else if (this.params != null) {
            return this.params.getPortNoNull();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getUptime()
     */
    public String getUptime() {
        if (this.serverInfo != null) {
            return this.serverInfo.getServerUptime();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getParameters()
     */
    public ConnectionParameters getParameters() {
        return this.params;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#getAllLocalFiles()
     */
    public IP4File[] getAllLocalFiles() {
        return new IP4File[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#dispose()
     */
    public void dispose() {
        this.resources.clear();
        this.changelists.clear();
        this.openedManager.clear();
        this.disposed = true;
        this.server = null;
        this.client = null;
        this.cached = null;
        this.needsRefresh = true;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#createChangelist(java.lang.String,
     *      com.perforce.team.core.p4java.IP4File[])
     */
    public IP4PendingChangelist createChangelist(String description,
            IP4File[] files) {
        return createChangelist(description, files, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#createChangelist(java.lang.String,
     *      com.perforce.team.core.p4java.IP4File[],
     *      com.perforce.team.core.p4java.IP4Job[])
     */
    public IP4PendingChangelist createChangelist(String description,
            IP4File[] files, IP4Job[] jobs) {
        IP4PendingChangelist list = null;
        IClient client = getClient();
        if (client != null) {
            Changelist cl = new Changelist();
            cl.setId(IChangelist.UNKNOWN);
            cl.setClientId(client.getName());
            cl.setDescription(description);
            cl.setUsername(client.getServer().getUserName());
            try {
                IChangelist created = client.createChangelist(cl);
                if (created != null && created.getId() > 0) {
                    list = new P4PendingChangelist(this, created, true);
                    if (list.getId() > 0) {
                        changelists.put(list.getId(), list);
                    }
                    P4Workspace.getWorkspace().notifyListeners(
                            new P4Event(EventType.CREATE_CHANGELIST, list));
                    P4Collection collection = new P4Collection(files);
                    if (!collection.isEmpty()) {
                        collection.reopen(list);
                    }
                    // Fix for job032331
                    if (jobs != null && jobs.length > 0) {
                        new P4Collection(jobs).fix(list);
                    }
                }
            } catch (P4JavaException e) {
                handleError(e);
                PerforceProviderPlugin.logError(e);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return list;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getCachedPendingChangelists()
     */
    public IP4PendingChangelist[] getCachedPendingChangelists() {
        if (this.changelists.isEmpty()) {
            getPendingChangelists(false);
        }
        return this.changelists.values().toArray(new IP4PendingChangelist[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelist(int)
     */
    public IP4PendingChangelist getPendingChangelist(int changelistId) {
        return getPendingChangelist(changelistId, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelist(int,
     *      boolean)
     */
    public IP4PendingChangelist getPendingChangelist(int changelistId,
            boolean fetchIfNotFound) {
        return getPendingChangelist(changelistId, fetchIfNotFound, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelist(int,
     *      boolean, boolean)
     */
    public IP4PendingChangelist getPendingChangelist(int changelistId,
            boolean fetchIfNotFound, boolean ignoreErrors) {
        if (this.changelists.isEmpty()) {
            getPendingChangelists(false);
        }
        IP4PendingChangelist list = this.changelists.get(changelistId);
        if (changelistId > 0 && list != null && list.getChangelist() == null) {
            this.changelists.remove(changelistId);
            list = null;
        }
        if (fetchIfNotFound && list == null) {
            loadPendingChangelist(changelistId, ignoreErrors);
            list = this.changelists.get(changelistId);
        }
        return list;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs()
     */
    public IP4Job[] getJobs() {
        return getJobs(0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(int)
     */
    public IP4Job[] getJobs(int size) {
        return getJobs((String[]) null, size);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String[])
     */
    public IP4Job[] getJobs(String[] paths) {
        return getJobs(paths, 0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String[],
     *      int)
     */
    public IP4Job[] getJobs(String[] paths, int size) {
        return getJobs(paths, size, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String)
     */
    public IP4Job[] getJobs(String path) {
        return getJobs(path, 0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String,
     *      int)
     */
    public IP4Job[] getJobs(String path, int size) {
        if (path == null) {
            return getJobs(size);
        } else {
            return getJobs(new String[] { path }, size);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String[],
     *      int, java.lang.String)
     */
    public IP4Job[] getJobs(final String[] paths, final int size,
            final String jobViewPath) {
        final List<IP4Job> jobs = new ArrayList<IP4Job>();
        IP4ServerOperation op = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                List<IJob> p4jJobs = server.getJobs(
                        P4FileSpecBuilder.makeFileSpecList(paths), size, true,
                        true, false, jobViewPath);
                for (IJob job : p4jJobs) {
                    if (job != null) {
                        IP4Job p4Job = new P4Job(job, getConnection());
                        jobs.add(p4Job);
                    }
                }
            }
        };
        runOperation(op);
        return jobs.toArray(new IP4Job[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobFields()
     */
    public String[] getJobFields() {
        if (this.jobSpec == null) {
            refreshJobSpec();
        }
        List<String> jobFields = new ArrayList<String>();
        if (this.jobSpec != null) {
            List<IJobSpecField> fields = this.jobSpec.getFields();
            for (IJobSpecField field : fields) {
                String name = field.getName();
                if (name != null) {
                    jobFields.add(name);
                }
            }
        }
        return jobFields.toArray(new String[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobSpec()
     */
    public IJobSpec getJobSpec() {
        if (this.jobSpec == null) {
            refreshJobSpec();
        }
        return this.jobSpec;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#refreshJobSpec()
     */
    public void refreshJobSpec() {
        IP4ServerOperation op = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                P4Connection.this.jobSpec = server.getJobSpec();
            }
        };
        runOperation(op);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getOpenedBy(java.lang.String)
     */
    public IP4File[] getOpenedBy(String path) {
        IP4File[] files = new IP4File[0];
        IServer server = getServer();
        if (server != null) {
            if (path == null) {
                path = ""; //$NON-NLS-1$
            }
            List<IFileSpec> specs = P4FileSpecBuilder
                    .makeFileSpecList(new String[] { path });
            try {
                specs = server.getOpenedFiles(specs, true, null, 0, -1);
                specs = P4FileSpecBuilder.getValidFileSpecs(specs);
                files = new IP4File[specs.size()];
                for (int i = 0; i < specs.size(); i++) {
                    files[i] = new P4File(specs.get(i), this);
                }
            } catch (P4JavaException e) {
                PerforceProviderPlugin.logError(e);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return files;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#handleError(com.perforce.p4java.exception.P4JavaException)
     */
    @Override
    public boolean handleError(P4JavaException exception) {
        boolean retry = false;
        if (exception != null && this.errorHandler != null && !isOffline()) {
            retry = this.errorHandler.shouldRetry(this, exception);
        }
        return retry;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#handleErrors(com.perforce.p4java.core.file.IFileSpec[])
     */
    @Override
    public void handleErrors(IFileSpec[] specs) {
        IErrorHandler handler = this.errorHandler;
        if (handler != null) {
            for (IFileSpec spec : specs) {
                if (isErrorSpec(spec)) {
                    this.errorHandler.handleErrorSpecs(specs);
                    break;
                }
            }
        }
    }

    /**
     * Get the protocol to use when creating servers
     * 
     * @return - protocol
     */
    protected String getProtocol() {
        return OneShotServerImpl.PROTOCOL_NAME;
    }

    private String uriFromP4Port(String p4port) {
        Pattern p = Pattern.compile("((ssl):)?(.*)");
        Matcher m = p.matcher(p4port);
        m.matches();    // can't fail to match, so ignore result
        return StringUtils.join(getProtocol(), m.group(2), "://", m.group(3));
    }
    
    private IServer createServer(IServer current) throws P4JavaException,
            URISyntaxException {
        String serverUri = uriFromP4Port(this.params.getPortNoNull());
        IServer newServer = P4Workspace.createServer(serverUri);
        
        newServer.registerCallback(P4Workspace.getWorkspace().getCallback());
        newServer.registerSSOCallback(P4Workspace.getWorkspace()
                .getSSOCallback(), this.params.getPort());
        newServer.setUserName(ConnectionParameters.getTicketUser(this.params, current));

        String ticket = null;
        // Use existing auth ticket first then fall back to params
        // auth ticket and lastly attempt to look up from possible
        // tickets file
        if (current != null && current.getAuthTicket() != null) {
            ticket = current.getAuthTicket();
        } else {
            ticket = this.params.getAuthTicket();
        }
        newServer.setAuthTicket(ticket);

        // Set current client on server object if it exists
        if (this.client != null) {
            newServer.setCurrentClient(this.client);
        }

        // Set charset if it's known and not "none"
        String charset = this.params.getCharset();
        if (charset != null && !charset.equalsIgnoreCase("none") && PerforceCharsets.isSupported(charset)) {
            newServer.setCharsetName(this.params.getCharset());
        }
        return newServer;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#refreshServer()
     */
    public boolean refreshServer() {
        IServer current = this.server;
        this.server = null;
        if (!isOffline() && !this.disposed) {
            boolean retry = true;
            while (retry) {
                retry = false;
                try {
                    IServer newServer = createServer(current);
                    newServer.connect();
                    this.server = newServer;
                    this.serverInfo=newServer.getServerInfo();

                    String ticket = this.server.getAuthTicket();
                    try {
                    	if (ticket == null) {
                    		ticket = AuthTicketsHelper.getTicketValue(ConnectionParameters.getTicketUser(params, this.server),
                    				this.serverInfo.getServerAddress(),
                    				P4Connection.getP4TicketsOSLocation());
                    		if(ticket!=null)
                    			this.server.setAuthTicket(ticket);
                    	}
                    } catch (Throwable t) {
                    	PerforceProviderPlugin.logWarning(t);
                    }
                    
                } catch (P4JavaException e) {
                    String message = e.getMessage();
                    if (message != null
                            && message
                                    .indexOf("unsupported Perforce server version") != -1) {
                        e = createUnsupportedException();
                    }
                    retry = handleError(e);
                    PerforceProviderPlugin.logError(e);
                } catch (URISyntaxException e1) {
                    PerforceProviderPlugin.logError(e1);
                } catch (P4JavaError e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
            return this.server != null;
        }
        return false;
    }

    /**
     * This method only checks if an alt-root is used. It uses the defaults if
     * it finds that an alt-root is not needed or found.
     * 
     * @param server
     * @param client
     */
    private void updateWorkingDirectory(IServer server, IClient client) {
        String rootToUse = null;
        try {
            if (client != null) {
                String root = client.getRoot();
                if (root != null) {
                    // Attempt to match AltRoots: if the configured Root: is not
                    // absolute or not existent
                    File rootTest = new File(root);
                    if (!rootTest.isAbsolute() || !rootTest.exists()) {
                        List<String> altRoots = client.getAlternateRoots();
                        if (altRoots != null) {
                            for (String alt : altRoots) {
                                if (alt != null) {
                                    File altTest = new File(alt);
                                    if (altTest.isAbsolute()) {
                                        // Fall back to first absolute found
                                        if (rootToUse == null) {
                                            rootToUse = alt;
                                        }
                                        // Use first existent one found
                                        if (altTest.exists()) {
                                            rootToUse = alt;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            PerforceProviderPlugin.logError(e);
        }
        this.currentDirectory = rootToUse;
        server.setWorkingDirectory(this.currentDirectory);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#refreshClient()
     */
    public boolean refreshClient() {
        IServer server = getServer();
        boolean retry = true;
        while (retry && server != null) {
            retry = false;
            try {
                IClient newClient = server.getClient(this.params.getClient());
                this.client = newClient;
                if (newClient == null) {
                    throw new ConfigException(CLIENT_NON_EXISTENT_PREFIX
                            + this.params.getClient()
                            + CLIENT_NON_EXISTENT_SUFFIX);
                }
                if (newClient != null) {
                    server.setCurrentClient(newClient);
                }
                updateWorkingDirectory(server, newClient);
            } catch (P4JavaException e) {
                retry = handleError(e);
                if (retry) {
                    server = getServer();
                }
                PerforceProviderPlugin.logError(e);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            } catch (Exception e) {
                PerforceProviderPlugin.logError(e);
            }
            if (!retry) {
                return this.client != null;
            }
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#updateRevertedFiles(com.perforce.team.core.p4java.P4Collection)
     */
    public void updateRevertedFiles(P4Collection collection) {
        for (IP4Resource resource : collection.members()) {
            if (resource instanceof IP4File) {
                removeFileFromChangelists((IP4File) resource);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#removeFileFromChangelists(com.perforce.team.core.p4java.IP4File)
     */
    public void removeFileFromChangelists(IP4File file) {
        if (file != null) {
            for (IP4PendingChangelist list : this.changelists.values()) {
                list.removeFile(file);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#removeChangelist(com.perforce.team.core.p4java.IP4PendingChangelist)
     */
    public void removeChangelist(IP4PendingChangelist changelist) {
        if (changelist != null) {
            int id = changelist.getId();
            this.changelists.remove(id);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#removeChangelist(int)
     */
    public void removeChangelist(int changelist) {
        this.changelists.remove(changelist);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getDifferingFiles(java.lang.String[])
     */
    public IFileSpec[] getDifferingFiles(String[] paths) {
        IClient client = getClient();
        IFileSpec[] specs = null;
        if (client != null) {
            try {
                specs = client.getDiffFiles(
                        P4FileSpecBuilder.makeFileSpecList(paths), 0, false,
                        false, false, false, true, false, false).toArray(
                        new IFileSpec[0]);
            } catch (P4JavaException e) {
                PerforceProviderPlugin.logError(e);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        if (specs == null) {
            specs = new IFileSpec[0];
        }
        return specs;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getMissingFiles(java.lang.String[])
     */
    public IFileSpec[] getMissingFiles(String[] paths) {
        IClient client = getClient();
        IFileSpec[] specs = null;
        if (client != null) {
            try {
                specs = client.getDiffFiles(
                        P4FileSpecBuilder.makeFileSpecList(paths), 0, false,
                        false, false, true, false, false, false).toArray(
                        new IFileSpec[0]);
            } catch (P4JavaException e) {
                PerforceProviderPlugin.logError(e);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        if (specs == null) {
            specs = new IFileSpec[0];
        }
        return specs;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getMappedProjects()
     */
    public IProject[] getMappedProjects() {
        List<IProject> projects = new ArrayList<IProject>();
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
                .getProjects()) {
            PerforceTeamProvider provider = PerforceTeamProvider
                    .getPerforceProvider(project);
            try {
                if (provider != null
                        && params.equals(provider.getProjectProperties(true))) {
                    projects.add(project);
                }
            } catch (CoreException e) {
            }
        }
        return projects.toArray(new IProject[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#hasMappedProjects()
     */
    public boolean hasMappedProjects() {
        boolean mapped = false;
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
                .getProjects()) {
            PerforceTeamProvider provider = PerforceTeamProvider
                    .getPerforceProvider(project);
            try {
                if (provider != null
                        && params.equals(provider.getProjectProperties(true))) {
                    mapped = true;
                    break;
                }
            } catch (CoreException e) {
            }
        }
        return mapped;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#showClientOnly()
     */
    public boolean showClientOnly() {
        return this.showClientOnly;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#setShowClientOnly(boolean)
     */
    public void setShowClientOnly(boolean showClientOnly) {
        boolean refresh = this.showClientOnly != showClientOnly;
        this.showClientOnly = showClientOnly;
        if (refresh) {
            markForRefresh();
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#setShowFoldersWIthOnlyDeletedFiles(boolean)
     */
    public void setShowFoldersWIthOnlyDeletedFiles(boolean showDeleted) {
        boolean refresh = this.showDeleted != showDeleted;
        this.showDeleted = showDeleted;
        if (refresh) {
            markForRefresh();
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#showFoldersWithOnlyDeletedFiles()
     */
    public boolean showFoldersWithOnlyDeletedFiles() {
        return this.showDeleted;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isDisposed()
     */
    public boolean isDisposed() {
        return this.disposed;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isSupported()
     */
    public boolean isSupported() {
        return parseServerVersion(getVersion()) >= MINIMUM_SERVER_VERSION;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isMoveSupported()
     */
    public boolean isMoveSupported() {
        return parseServerVersion(getVersion()) >= MOVE_SERVER_VERSION;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isMoveServerOnlySupported()
     */
    public boolean isMoveServerOnlySupported() {
        return parseServerVersion(getVersion()) >= MOVE_SERVER_ONLY_SERVER_VERSION;
    }

    /**
     * Is the -u filter option available?
     * 
     * @return - true if supported
     */
    private boolean isUserFilterSupported() {
        return parseServerVersion(getVersion()) >= USER_FILTER_SERVER_VERSION;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isShelvingSupported()
     */
    public boolean isShelvingSupported() {
        return parseServerVersion(getVersion()) >= SHELVE_SERVER_VERSION;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isSearchSupported()
     */
    public boolean isSearchSupported() {
        return parseServerVersion(getVersion()) >= SEARCH_SERVER_VERSION;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getRootSpec()
     */
    public String getRootSpec() {
        return DEPOT_PREFIX + getParameters().getClientNoNull()
                + REMOTE_ELLIPSIS;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFixes(com.perforce.team.core.p4java.IP4Job)
     */
    public IP4Changelist[] getFixes(final IP4Job job) {
        return job != null ? getFixes(job.getId()) : new IP4Changelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFixes(java.lang.String)
     */
    public IP4Changelist[] getFixes(final String jobId) {
        final List<IP4Changelist> lists = new ArrayList<IP4Changelist>();
        if (jobId != null) {
            IP4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    List<IFix> fixes = client.getServer().getFixList(null,
                            IChangelist.UNKNOWN, jobId, false, 0);
                    for (IFix fix : fixes) {
                        int changelistId = fix.getChangelistId();
                        IP4Changelist list = getChangelistById(changelistId);
                        if (list != null) {
                            lists.add(list);
                        }
                    }
                }

                @Override
                public void exception(P4JavaException exception) {
                    lists.clear();
                }

            };
            runOperation(op);
        }
        return lists.toArray(new IP4Changelist[lists.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFixIds(java.lang.String)
     */
    public Integer[] getFixIds(final String jobId) {
        final List<Integer> lists = new ArrayList<Integer>();
        if (jobId != null) {
            IP4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    List<IFix> fixes = client.getServer().getFixList(null,
                            IChangelist.UNKNOWN, jobId, false, 0);
                    for (IFix fix : fixes) {
                        int changelistId = fix.getChangelistId();
                        if (changelistId > 0) {
                            lists.add(changelistId);
                        }
                    }
                }

                @Override
                public void exception(P4JavaException exception) {
                    lists.clear();
                }

            };
            runOperation(op);
        }
        return lists.toArray(new Integer[lists.size()]);
    }

    /**
     * 
     * @see com.perforce.team.core.p4java.IP4Connection#getFixIds(com.perforce.team.core.p4java.IP4Job)
     */
    public Integer[] getFixIds(final IP4Job job) {
        return job != null ? getFixIds(job.getId()) : new Integer[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getChangelistById(int)
     */
    public IP4Changelist getChangelistById(int id) {
        return getChangelistById(id, null, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getChangelistById(int,
     *      com.perforce.p4java.core.ChangelistStatus, boolean, boolean)
     */
    public IP4Changelist getChangelistById(final int id,
            final ChangelistStatus type, final boolean checkClient,
            final boolean ignoreErrors) {
        final IP4Changelist[] list = new IP4Changelist[1];

        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                try {
                    IChangelist p4jList = client.getServer().getChangelist(id);
                    if (p4jList != null
                            && (type == null || p4jList.getStatus() == type)) {
                        if (p4jList.getStatus() == ChangelistStatus.PENDING) {
                            boolean onClient = checkClient
                                    && clientMatches(p4jList, client)
                                    && userMatches(p4jList, client);
                            list[0] = new P4PendingChangelist(
                                    P4Connection.this, p4jList, onClient);
                        } else if (p4jList.getStatus() == ChangelistStatus.SUBMITTED) {
                            list[0] = new P4SubmittedChangelist(
                                    P4Connection.this, p4jList);
                        }
                    }
                } catch (P4JavaException e) {
                    if (!ignoreErrors) {
                        throw e;
                    }
                }
            }
        };
        runOperation(op);

        return list[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getChangelistById(int,
     *      com.perforce.p4java.core.ChangelistStatus, boolean)
     */
    public IP4Changelist getChangelistById(int id, ChangelistStatus type,
            boolean checkClient) {
        return getChangelistById(id, type, checkClient, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelistById(int)
     */
    public IP4SubmittedChangelist getSubmittedChangelistById(int id) {
        return getSubmittedChangelistById(id, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelistById(int,
     *      boolean)
     */
    public IP4SubmittedChangelist getSubmittedChangelistById(int id,
            boolean ignoreErrors) {
        return (IP4SubmittedChangelist) getChangelistById(id,
                ChangelistStatus.SUBMITTED, ignoreErrors);

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelistById(int)
     */
    public IP4PendingChangelist getPendingChangelistById(int id) {
        return (IP4PendingChangelist) getChangelistById(id,
                ChangelistStatus.PENDING, false);
    }

    private void loadPendingChangelist(int id, boolean ignoreErrors) {
        IP4Changelist list = getChangelistById(id, ChangelistStatus.PENDING,
                true, ignoreErrors);
        if (list instanceof IP4PendingChangelist) {
            IP4PendingChangelist pending = (IP4PendingChangelist) list;
            if (pending.isOnClient() && pending.getChangelist() != null) {
                this.changelists.put(id, pending);
                P4Workspace.getWorkspace().notifyListeners(
                        new P4Event(EventType.REFRESHED, pending));
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#loadPendingChangelist(int)
     */
    public void loadPendingChangelist(int id) {
        loadPendingChangelist(id, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#printToTempFile(java.lang.String)
     */
    public File printToTempFile(final String specPath) {
        final File[] tempFile = new File[1];
        if (specPath != null) {

            IP4ServerOperation op = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    InputStream stream = server.getFileContents(P4FileSpecBuilder
                            .makeFileSpecList(new String[] { specPath }),
                            false, true);
                    if(stream==null){ 
                    	// job033661: p4 merge behave differently for p4v and p4eclipse
                    	// We have to pass a non-null input to P4CoreUtils.createFile(). 
                    	stream = new ByteArrayInputStream(new byte[0]);
                    }
                    tempFile[0] = P4CoreUtils.createFile(stream);
                }
            };
            runOperation(op);
        }
        return tempFile[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getAllClients()
     */
    public IClientSummary[] getAllClients() {
        final List<IClientSummary> clients = new ArrayList<IClientSummary>();

        IP4ServerOperation op = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                clients.addAll(server.getClients(null, null, -1));
            }
        };
        runOperation(op);

        return clients.toArray(new IClientSummary[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getOwnedClients()
     */
    public IClientSummary[] getOwnedClients() {
        final List<IClientSummary> clients = new ArrayList<IClientSummary>();
        IP4ServerOperation op = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
            	/*
            	 * see job068513: prior 2006, the clients command does not support filter
            	 */
            	int[] version=P4CoreUtils.getVersion(getVersion());
            	if(version[0]<2006){
	                clients.addAll(((Server)server).getClients(new GetClientsOptions()));            		
            	}else{
	                clients.addAll(server.getClients(getParameters().getUser(),
	                        null, -1));
            	}
            }
        };
        runOperation(op);

        return clients.toArray(new IClientSummary[0]);
    }

    private boolean hostMatches(String infoHost, String clientHost) {
        return infoHost.equals(clientHost) || clientHost == null
                || "".equals(clientHost); //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getOwnedLocalClients()
     */
    public IClientSummary[] getOwnedLocalClients() {
        final List<IClientSummary> clients = new ArrayList<IClientSummary>();
        IP4ServerOperation op = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                String user = getParameters().getUser();
                String infoHost = getServerInfoClientHost();
                List<IClientSummary> owned = null;
                boolean matchHost = infoHost != null;
                boolean matchUser = false;
                if (isUserFilterSupported()) {
                    owned = server.getClients(user, null, -1);
                } else {
                    owned = server.getClients(null, null, -1);
                    matchUser = true;
                }
                if (matchUser && matchHost) {
                    for (IClientSummary spec : owned) {
                        if (user != null && user.equals(spec.getOwnerName())
                                && hostMatches(infoHost, spec.getHostName())) {
                            clients.add(spec);
                        }
                    }
                } else if (matchHost) {
                    for (IClientSummary spec : owned) {
                        if (hostMatches(infoHost, spec.getHostName())) {
                            clients.add(spec);
                        }
                    }
                } else if (matchUser) {
                    for (IClientSummary spec : owned) {
                        if (user != null && user.equals(spec.getOwnerName())) {
                            clients.add(spec);
                        }
                    }
                }
            }
        };
        runOperation(op);

        return clients.toArray(new IClientSummary[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#createJob(java.util.Map)
     */
    public IP4Job createJob(Map<String, Object> jobFields)
            throws P4JavaException {
        IP4Job newP4Job = null;
        if (jobFields != null) {
            IServer server = getServer();
            if (server != null) {
                try {
                    IJob newP4JJob = server.createJob(jobFields);
                    newP4Job = new P4Job(newP4JJob, this);
                } catch (P4JavaError error) {
                    PerforceProviderPlugin.logError(error);
                }
            }
        }
        return newP4Job;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#createBranch(com.perforce.p4java.core.IBranchSpec)
     */
    public IP4Branch createBranch(IBranchSpec spec) throws P4JavaException {
        IP4Branch newP4Branch = null;
        if (spec != null) {
            IServer server = getServer();
            if (server != null) {
                try {
                    server.createBranchSpec(spec);
                    newP4Branch = new P4Branch(this, spec, true);
                } catch (P4JavaError error) {
                    PerforceProviderPlugin.logError(error);
                }
            }
        }
        return newP4Branch;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getCurrentDirectory()
     */
    public String getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(int)
     */
    public IP4Label[] getLabels(int size) {
        return getLabels((String[]) null, size);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels()
     */
    public IP4Label[] getLabels() {
        return getLabels(0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String[])
     */
    public IP4Label[] getLabels(String[] paths) {
        return getLabels(paths, 0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String[],
     *      int)
     */
    public IP4Label[] getLabels(String[] paths, int size) {
        return getLabels(paths, size, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String[],
     *      int, java.lang.String)
     */
    public IP4Label[] getLabels(String[] paths, int size, String nameFilter) {
        return getLabels(null, paths, size, nameFilter);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String)
     */
    public IP4Label[] getLabels(String path) {
        return getLabels(path, 0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String,
     *      int)
     */
    public IP4Label[] getLabels(String path, int size) {
        if (path == null) {
            return getLabels(size);
        } else {
            return getLabels(new String[] { path }, size);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String,
     *      java.lang.String[], int, java.lang.String)
     */
    public IP4Label[] getLabels(final String user, final String[] paths,
            final int size, final String nameFilter) {
        final List<IP4Label> labels = new ArrayList<IP4Label>();
        IP4ServerOperation op = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                int serverVersion = parseServerVersion(getVersion());
                boolean filterUser = false;
                String userMatch = user;
                if (user != null && serverVersion < USER_FILTER_SERVER_VERSION) {
                    filterUser = true;
                    userMatch = null;
                }
                int max = size;
                if (serverVersion < MAX_FILTER_SERVER_VERSION) {
                    max = -1;
                }
                String nameMatch = nameFilter;
                if (serverVersion < NAME_FILTER_SERVER_VERSION) {
                    nameMatch = null;
                }

                List<ILabelSummary> p4jLabels = server
                        .getLabels(userMatch, max, nameMatch,
                                P4FileSpecBuilder.makeFileSpecList(paths));

                if (!filterUser) {
                    for (ILabelSummary label : p4jLabels) {
                        if (label != null) {
                            IP4Label p4Label = new P4Label(new Label(label),
                                    getConnection(), true);
                            labels.add(p4Label);
                        }
                    }
                } else {
                    for (ILabelSummary label : p4jLabels) {
                        if (label != null && user.equals(label.getOwnerName())) {
                            IP4Label p4Label = new P4Label(new Label(label),
                                    getConnection(), true);
                            labels.add(p4Label);
                        }
                    }
                }
            }
        };
        runOperation(op);
        return labels.toArray(new IP4Label[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#integrate(com.perforce.team.core.p4java.P4FileIntegration,
     *      int, boolean, boolean,
     *      com.perforce.team.core.p4java.P4IntegrationOptions)
     */
    public IP4Resource[] integrate(P4FileIntegration integration,
            int changelist, boolean preview, boolean all,
            P4IntegrationOptions options) {
        return integrate(integration, null, changelist, preview, all, options);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#integrate(com.perforce.team.core.p4java.P4FileIntegration,
     *      java.lang.String, int, boolean, boolean,
     *      com.perforce.team.core.p4java.P4IntegrationOptions)
     */
    public IP4Resource[] integrate(P4FileIntegration integration,
            String branch, int changelist, boolean preview, boolean all,
            P4IntegrationOptions options) {
        return integrate(integration, branch, changelist, null, preview, all,
                options);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#integrate(com.perforce.team.core.p4java.P4FileIntegration,
     *      java.lang.String, int, java.lang.String, boolean, boolean,
     *      com.perforce.team.core.p4java.P4IntegrationOptions)
     */
    public IP4Resource[] integrate(final P4FileIntegration integration,
            final String branch, int changelist, final String description,
            final boolean preview, final boolean all,
            final P4IntegrationOptions options) {
        P4Collection integrated = new P4Collection();
        if (integration != null || branch != null) {

            if (IP4PendingChangelist.NEW == changelist) {
                if (!preview) {
                    // Create a new pending changelist for this integration
                    String newDescription = description;
                    if (newDescription == null) {
                        newDescription = INTEGRATE_DEFAULT_DESCRIPTION;
                    }
                    IP4PendingChangelist newList = createChangelist(
                            newDescription, null);
                    if (newList != null) {
                        changelist = newList.getId();
                    }
                    // Only proceed if we have a usable changelist id
                    if (changelist < 0) {
                        return new IP4Resource[0];
                    }
                } else {
                    changelist = IChangelist.DEFAULT;
                }
            }

            final int pendingId = changelist;
            final List<IFileSpec> integSpecs = new ArrayList<IFileSpec>();
            P4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    String sourcePath = null;
                    String start = null;
                    String end = null;
                    String target = null;
                    if (integration != null) {
                        sourcePath = integration.getSource();
                        start = integration.getStart();
                        end = integration.getEnd();
                        target = integration.getTarget();
                    }
                    if (end != null) {
                        if (sourcePath == null) {
                            sourcePath = IP4Connection.ROOT;
                        }
                        if (start != null) {
                            sourcePath += start + "," + end; //$NON-NLS-1$
                        } else {
                            sourcePath += end;
                        }
                    }
                    IFileSpec fromSpec = null;
                    if (sourcePath != null) {
                        fromSpec = new FileSpec(sourcePath);
                    }
                    IFileSpec toSpec = null;
                    if (target != null) {
                        toSpec = new FileSpec(target);
                    }

					IntegrateFilesOptions integOpts = options
							.createIntegrateFilesOptions(pendingId, preview);
					
					// Fix for job042975, add -s flag when using branch-based
					// integration and source path is specified and target path
					// is not
					if (branch != null && fromSpec != null && toSpec == null) {
						integOpts.setBidirectionalInteg(true);
					}
					integSpecs.addAll(client.integrateFiles(fromSpec, toSpec,
							branch, integOpts));

                }
            };
            runOperation(op);

            if (!integSpecs.isEmpty()) {
                for (IFileSpec integ : integSpecs) {
                    if (all || FileSpecOpStatus.VALID == integ.getOpStatus()) {
                        IP4Resource resource = null;
                        if (preview) {
                            resource = new P4File(integ, P4Connection.this);
                            ((IP4File) resource)
                                    .setIntegrationSpecs(new IFileSpec[] { integ });
                        } else {
                            String path = integ.getToFile();
                            if (path == null) {
                                path = integ.getDepotPathString();
                            }
                            if (path != null) {
                                resource = getResource(path);
                                if (resource == null) {
                                    resource = new P4File(integ,
                                            P4Connection.this);
                                    updateResource(resource);
                                }
                            }
                        }
                        integrated.add(resource);
                    }
                }
                if (!preview) {
                    if (!integrated.isEmpty()) {
                        integrated.refresh();
                        integrated
                                .addToChangelistModel(getPendingChangelist(changelist));
                        P4Workspace.getWorkspace().notifyListeners(
                                new P4Event(EventType.OPENED, integrated));
                        integrated
                                .refreshLocalResources(IResource.DEPTH_INFINITE);

                        if (options != null && options.isTrySafeResolve()) {
                            P4Collection unresolved = new P4Collection();
                            for (IP4Resource resource : integrated.members()) {
                                if (resource instanceof IP4File
                                        && ((IP4File) resource).isUnresolved()) {
                                    unresolved.add(resource);
                                }
                            }
                            unresolved.setType(Type.LOCAL);
                            unresolved.resolve(new ResolveFilesAutoOptions()
                                    .setSafeMerge(true));
                        }
                    }
                }
                handleErrors(integSpecs.toArray(new IFileSpec[0]));
            }
        }
        return integrated.members();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#hasServer()
     */
    public boolean hasServer() {
        return server != null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJob(java.lang.String)
     */
    public IP4Job getJob(final String id) {
        final IP4Job[] job = new IP4Job[] { null };
        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                IJob p4jJob = server.getJob(id);
                if (p4jJob != null) {
                    job[0] = new P4Job(p4jJob, P4Connection.this);
                }
            }
        };
        runOperation(operation);
        return job[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabel(java.lang.String)
     */
    public IP4Label getLabel(final String labelName) {
        final IP4Label[] label = new IP4Label[] { null };
        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                ILabel p4jLabel = server.getLabel(labelName);
                if (p4jLabel != null) {
                    label[0] = new P4Label(p4jLabel, P4Connection.this, false);
                }
            }
        };
        runOperation(operation);
        return label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getIntVersion()
     */
    public int getIntVersion() {
        if (this.serverVersion == 0) {
            this.serverVersion = parseServerVersion(getVersion());
        }
        return this.serverVersion;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranch(java.lang.String)
     */
    public IP4Branch getBranch(final String branchName) {
        final IP4Branch[] branch = new IP4Branch[] { null };
        if (branchName != null) {
            IP4ServerOperation operation = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    IBranchSpec p4jBranch = server.getBranchSpec(branchName);
                    if (p4jBranch != null) {
                        branch[0] = new P4Branch(P4Connection.this, p4jBranch,
                                false);
                    }
                }
            };
            runOperation(operation);
        }
        return branch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranches(int)
     */
    public IP4Branch[] getBranches(int size) {
        return getBranches(null, size, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranches()
     */
    public IP4Branch[] getBranches() {
        return getBranches(null, -1, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranches(java.lang.String,
     *      int, java.lang.String)
     */
    public IP4Branch[] getBranches(final String user, final int size,
            final String nameFilter) {
        final List<IP4Branch> branches = new ArrayList<IP4Branch>();
        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                int version = getIntVersion();

                String name = nameFilter;
                if (version < NAME_FILTER_SERVER_VERSION) {
                    name = null;
                }

                int maxReturns = size;
                if (version < MAX_FILTER_SERVER_VERSION) {
                    maxReturns = -1;
                }

                boolean serverFilter = true;
                String userName = user;
                if (user != null && version < USER_FILTER_SERVER_VERSION) {
                    userName = null;
                    serverFilter = false;
                }

                List<IBranchSpecSummary> branchSpecs = server.getBranchSpecs(
                        userName, name, maxReturns);

                if (serverFilter) {
                    for (IBranchSpecSummary branchSpec : branchSpecs) {
                        if (branchSpec != null) {
                            branches.add(new P4Branch(getConnection(),
                                    branchSpec, true));
                        }
                    }
                } else {
                    for (IBranchSpecSummary branchSpec : branchSpecs) {
                        if (branchSpec != null
                                && user.equals(branchSpec.getOwnerName())) {
                            branches.add(new P4Branch(getConnection(),
                                    branchSpec, true));
                        }
                    }
                }
            }
        };
        runOperation(operation);
        return branches.toArray(new IP4Branch[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranches(java.lang.String,
     *      int)
     */
    public IP4Branch[] getBranches(String user, int size) {
        return getBranches(user, size, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getActivePendingChangelist()
     */
    public IP4PendingChangelist getActivePendingChangelist() {
        return getPendingChangelist(this.currentPending);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#setActivePendingChangelist(int)
     */
    public void setActivePendingChangelist(int id) {
        int currentId = this.currentPending;
        if (currentId != id) {
            this.currentPending = id;

            IP4PendingChangelist list = getPendingChangelist(id);
            IP4PendingChangelist current = getPendingChangelist(currentId);

            List<IP4Resource> changed = new ArrayList<IP4Resource>();
            if (list != null) {
                changed.add(list);
            }
            if (current != null) {
                changed.add(current);
            }
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.CHANGED, changed
                            .toArray(new IP4Resource[changed.size()])));
            if (list != null) {
                P4Workspace.getWorkspace().notifyListeners(
                        new P4Event(EventType.ACTIVE_CHANGELIST, list));
            } else {
                P4Workspace.getWorkspace().notifyListeners(
                        new P4Event(EventType.INACTIVE_CHANGELIST, this));
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists()
     */
    public IP4ShelvedChangelist[] getShelvedChangelists() {
        return getShelvedChangelists(0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(int)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(int size) {
        return getShelvedChangelists((String[]) null, size);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String[])
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String[] paths) {
        return getShelvedChangelists(paths, 0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String[],
     *      int)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String[] paths, int size) {
        return getShelvedChangelists(paths, size, null, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String[],
     *      int, java.lang.String, java.lang.String)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(final String[] paths,
            final int size, final String user, final String clientWorkspace) {
        final List<IP4ShelvedChangelist> changes = new ArrayList<IP4ShelvedChangelist>();
        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                if (isShelvingSupported()) {
                    IServer server = client.getServer();
                    List<IChangelistSummary> changelists = server
                            .getChangelists(size,
                                    P4FileSpecBuilder.makeFileSpecList(paths),
                                    clientWorkspace, user, false,
                                    IChangelist.Type.SHELVED, true);
                    for (IChangelistSummary change : changelists) {
                        IChangelist fullChange = new Changelist(change, server,
                                false);
                        IP4ShelvedChangelist changelist = new P4ShelvedChangelist(
                                P4Connection.this, fullChange,
                                !userClientMatches(fullChange, client));
                        changes.add(changelist);
                    }
                }
            }
        };
        runOperation(op);
        return changes.toArray(new IP4ShelvedChangelist[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String path) {
        return getShelvedChangelists(path, 0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String,
     *      int)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String path, int size) {
        if (path == null) {
            return getShelvedChangelists(size);
        } else {
            return getShelvedChangelists(new String[] { path }, size);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLoggedInTime()
     */
    public long getLoggedInTime() {
        return this.loginTime;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isPendingLoaded()
     */
    public boolean isPendingLoaded() {
        return !this.changelists.isEmpty();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSpecDepot()
     */
    public P4Depot getSpecDepot() {
        return this.specDepot;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getOpenedManager()
     */
    public PendingResourceManager getOpenedManager() {
        return this.openedManager;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isCaseSensitive()
     */
    @Override
    public boolean isCaseSensitive() {
        IServer server = getServer();
        return server != null ? server.isCaseSensitive() : true;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getInterchanges(java.lang.String,
     *      java.lang.String)
     */
    public IP4SubmittedChangelist[] getInterchanges(final String sourcePath,
            final String targetPath) {
        final IP4Connection connection = getConnection();
        final List<IP4SubmittedChangelist> lists = new ArrayList<IP4SubmittedChangelist>();
        if (sourcePath != null && targetPath != null && connection != null) {
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    IFileSpec sourceSpec = new FileSpec(sourcePath);
                    IFileSpec targetSpec = new FileSpec(targetPath);
                    List<IChangelist> interchanges = client.getServer()
                            .getInterchanges(sourceSpec, targetSpec, false,
                                    true, -1);
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
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String)
     */
    public IFileLineMatch[] searchDepot(String pattern) {
        return searchDepot(pattern, ROOT);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String,
     *      java.lang.String)
     */
    public IFileLineMatch[] searchDepot(String pattern, String path) {
        if (path == null) {
            return new IFileLineMatch[0];
        }
        return searchDepot(pattern, new String[] { path });
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String,
     *      java.lang.String[])
     */
    public IFileLineMatch[] searchDepot(String pattern, String[] paths) {
        if (pattern == null || paths == null || paths.length == 0) {
            return new IFileLineMatch[0];
        }
        return searchDepot(pattern, null, paths);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String,
     *      com.perforce.p4java.core.file.MatchingLinesOptions,
     *      java.lang.String)
     */
    public IFileLineMatch[] searchDepot(String pattern,
            MatchingLinesOptions options, String path) {
        if (pattern == null || path == null) {
            return new IFileLineMatch[0];
        }
        return searchDepot(pattern, options, new String[] { path });
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String,
     *      com.perforce.p4java.core.file.MatchingLinesOptions,
     *      java.lang.String[])
     */
    public IFileLineMatch[] searchDepot(final String pattern,
            final MatchingLinesOptions options, final String[] paths) {
        final List<IFileLineMatch> matches = new ArrayList<IFileLineMatch>();
        if (pattern != null && paths != null && paths.length > 0) {
            IP4ServerOperation operation = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    if (!(server instanceof IOptionsServer)) {
                        return;
                    }
                    MatchingLinesOptions grepOptions = options;
                    if (grepOptions == null) {
                        grepOptions = new MatchingLinesOptions();
                    }
                    grepOptions.setIncludeLineNumbers(true);
                    List<IFileSpec> specs = P4FileSpecBuilder
                            .makeFileSpecList(paths);
                    List<IFileLineMatch> found = ((IOptionsServer) server)
                            .getMatchingLines(specs, pattern, grepOptions);
                    matches.addAll(found);
                }
            };
            runOperation(operation);
        }
        return matches.toArray(new IFileLineMatch[matches.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getDiffs(java.lang.String,
     *      java.lang.String)
     */
    public IFileDiff[] getDiffs(final String path1, final String path2) {
        final List<IFileDiff> diffs = new ArrayList<IFileDiff>();
        if (path1 != null && path2 != null) {
            IP4ServerOperation operation = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    List<IFileSpec> specs = P4FileSpecBuilder
                            .makeFileSpecList(new String[] { path1, path2 });
                    List<IFileDiff> serverDiffs = server.getFileDiffs(
                            specs.get(0), specs.get(1), null, null, false,
                            false, false);
                    diffs.addAll(serverDiffs);
                }
            };
            runOperation(operation);
        }
        return diffs.toArray(new IFileDiff[diffs.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getHistory(java.lang.String[],
     *      int)
     */
    public IP4Revision[] getHistory(final String[] paths, final int max) {
        final List<IP4Revision> history = new ArrayList<IP4Revision>();
        if (paths != null && paths.length > 0) {
            IP4ServerOperation operation = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    List<IFileSpec> specs = P4FileSpecBuilder
                            .makeFileSpecList(paths);
                    Map<IFileSpec, List<IFileRevisionData>> historyData = server
                            .getRevisionHistory(specs, max, false, false, true,
                                    false);
                    if (historyData != null) {
                        for (List<IFileRevisionData> data : historyData
                                .values()) {
                            if (data != null) {
                                for (IFileRevisionData rev : data) {
                                    history.add(new P4Revision(
                                            P4Connection.this, rev));
                                }
                            }
                        }
                    }
                }
            };
            runOperation(operation);
        }
        return history.toArray(new IP4Revision[history.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isOwner(java.lang.String)
     */
    public boolean isOwner(String user) {
        if (user == null) {
            return false;
        }
        if (isCaseSensitive()) {
            return user.equals(params.getUser());
        } else {
            return user.equalsIgnoreCase(params.getUser());
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getDepot(java.lang.String)
     */
    public P4Depot getDepot(String name) {
        P4Depot depot = null;
        if (name != null) {
            for (IP4Resource resource : members()) {
                if (name.equals(resource.getName())) {
                    depot = (P4Depot) resource;
                    break;
                }
            }
        }
        return depot;
    }

	public List<IP4Stream> getFilteredStreams(final boolean unloaded, final List<String> paths,
			final String filter, final int size) {
        final List<IP4Stream> streams = new ArrayList<IP4Stream>();
        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                GetStreamsOptions opts = new GetStreamsOptions();
                opts.setUnloaded(unloaded);
                opts.setMaxResults(size);
                opts.setFilter(filter);
                
                if(server instanceof IOptionsServer){
                    List<IStreamSummary> summaries = ((IOptionsServer) server)
                            .getStreams(paths, opts);
                    for (IStreamSummary sum : summaries) {
                    	IP4Stream stream=new P4Stream(P4Connection.this,sum);
                        streams.add(stream);
                    }
                }
            }
        };
        runOperation(op);
        return streams;
	}

	public IP4Stream getStream(String path) {
	    if(StringUtils.isEmpty(path))
	        return null;
	    
        IServer server = getServer();
        if (server instanceof IOptionsServer) {
        	try {
				IStream stream = ((IOptionsServer) server).getStream(path);
				return new P4Stream(this,stream);
			} catch (P4JavaException e) {
				e.printStackTrace();
			}
        }
        return null;
	}
	
    public static boolean testEqual(IP4Connection c1, IP4Connection c2) {
        if (c1 == c2) {
            return true;
        } else if (c1!=null && c2!=null) {
        	return c1.equals(c2);
        }
        return false;
    }

    public IP4Resource[] copyStream(final P4FileIntegration integration,
            final String description, final CopyFilesOptions options) {
        P4Collection integrated = new P4Collection();
        if (integration != null) {
            if (IP4PendingChangelist.NEW == options.getChangelistId()) {
                if (!options.isNoUpdate()) {// not preview
                    // Create a new pending changelist for this integration
                    String newDescription = description;
                    if (newDescription == null) {
                        newDescription = "Copy stream change";
                    }
                    IP4PendingChangelist newList = createChangelist(
                            newDescription, null);
                    if (newList != null) {
                        options.setChangelistId(newList.getId());
                    }
                    // Only proceed if we have a usable changelist id
                    if (options.getChangelistId() < 0) {
                        return new IP4Resource[0];
                    }
                } else { 
                    options.setChangelistId(IChangelist.DEFAULT);
                }
            }

            final List<IFileSpec> integSpecs = new ArrayList<IFileSpec>();
            P4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    // for stream copy, fromFiles is assumed, we only consider
                    // the toFiles here
                    String start = null;
                    String end = null;
                    String target = null;
                    if (integration != null) {
                        start = integration.getStart();
                        end = integration.getEnd();
                        target = integration.getTarget();
                    }
                    if (end != null) {
                        if (target == null) {
                            target = IP4Connection.ROOT;
                        }
                        if (start != null) {
                            target += start + "," + end; //$NON-NLS-1$
                        } else {
                            target += end;
                        }
                    }
                    IFileSpec toSpec = null;
                    if (target != null) {
                        toSpec = new FileSpec(target);
                    }
                    List<IFileSpec> toSpecs=new ArrayList<IFileSpec>();
                    toSpecs.add(toSpec);

                    integSpecs.addAll(client.copyFiles(null, toSpecs, options)); // copy
                                                                                 // streams
                }
            };
            runOperation(op);

            if (!integSpecs.isEmpty()) {
                for (IFileSpec integ : integSpecs) {
                    if (FileSpecOpStatus.VALID == integ.getOpStatus()) {
                        IP4Resource resource = null;
                        if (options.isNoUpdate()) { // preview
                            resource = new P4File(integ, P4Connection.this);
                        ((IP4File) resource)
                                .setIntegrationSpecs(new IFileSpec[] { integ });
                        } else { // update the resources after copy
                            String path = integ.getToFile();
                            if (path == null) {
                                path = integ.getDepotPathString();
                            }
                            if (path != null) {
                                resource = getResource(path);
                                if (resource == null) {
                                resource = new P4File(integ, P4Connection.this);
                                    updateResource(resource);
                                }
                            }
                        }
                        integrated.add(resource);
                    }
                }
                if (!options.isNoUpdate()) { // not preview, do a refresh local
                                             // resources
                    if (!integrated.isEmpty()) {
                        integrated.refresh();
                        integrated
                                .addToChangelistModel(getPendingChangelist(options
                                        .getChangelistId()));
                        P4Workspace.getWorkspace().notifyListeners(
                                new P4Event(EventType.OPENED, integrated));
                        integrated
                                .refreshLocalResources(IResource.DEPTH_INFINITE);
                        
                    }
                }
                handleErrors(integSpecs.toArray(new IFileSpec[0]));
            }
        }
        return integrated.members();
    }

    public IP4Resource[] mergeStream(final P4FileIntegration integration,
            final String description, final MergeFilesOptions options) {
        P4Collection integrated = new P4Collection();
        if (integration != null) {
            if (IP4PendingChangelist.NEW == options.getChangelistId()) {
                if (!options.isShowActionsOnly()) {// not preview
                    // Create a new pending changelist for this integration
                    String newDescription = description;
                    if (newDescription == null) {
                        newDescription = "Merge stream change";
                    }
                    IP4PendingChangelist newList = createChangelist(
                            newDescription, null);
                    if (newList != null) {
                        options.setChangelistId(newList.getId());
                    }
                    // Only proceed if we have a usable changelist id
                    if (options.getChangelistId() < 0) {
                        return new IP4Resource[0];
                    }
                } else { 
                    options.setChangelistId(IChangelist.DEFAULT);
                }
            }

            final List<IFileSpec> integSpecs = new ArrayList<IFileSpec>();
            P4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    String start = null;
                    String end = null;
                    String target = null;
                    if (integration != null) {
                        start = integration.getStart();
                        end = integration.getEnd();
                        target = integration.getTarget();
                    }
                    
                    if (end != null) {
                        if (target == null) {
                            target = IP4Connection.ROOT;
                        }
                        if (start != null) {
                            target += start + "," + end; //$NON-NLS-1$
                        } else {
                            target += end;
                        }
                    }                    
                    IFileSpec toSpec = null;
                    if (target != null) {
                        toSpec = new FileSpec(target);
                    }
                    List<IFileSpec> toSpecs=new ArrayList<IFileSpec>();
                    toSpecs.add(toSpec);

                    integSpecs
                            .addAll(client.mergeFiles(null, toSpecs, options)); // merge
                                                                                // streams
                }
            };
            runOperation(op);

            if (!integSpecs.isEmpty()) {
                for (IFileSpec integ : integSpecs) {
                    if (FileSpecOpStatus.VALID == integ.getOpStatus()) {
                        IP4Resource resource = null;
                        if (options.isShowActionsOnly()) { // preview
                            resource = new P4File(integ, P4Connection.this);
                            ((IP4File) resource)
                                    .setIntegrationSpecs(new IFileSpec[] { integ });
                        } else { // update the resources after copy
                            String path = integ.getToFile();
                            if (path == null) {
                                path = integ.getDepotPathString();
                            }
                            if (path != null) {
                                resource = getResource(path);
                                if (resource == null) {
                                    resource = new P4File(integ, P4Connection.this);
                                    updateResource(resource);
                                }
                            }
                        }
                        integrated.add(resource);
                    }
                }
                if (!options.isShowActionsOnly()) { // not preview, do a refresh
                                                    // local resources
                    if (!integrated.isEmpty()) {
                        integrated.refresh();
                        integrated
                                .addToChangelistModel(getPendingChangelist(options
                                        .getChangelistId()));
                        P4Workspace.getWorkspace().notifyListeners(
                                new P4Event(EventType.OPENED, integrated));
                        integrated
                                .refreshLocalResources(IResource.DEPTH_INFINITE);
                    }
                }
                handleErrors(integSpecs.toArray(new IFileSpec[0]));
            }
        }
        return integrated.members();
    }

    public static String convertDiskLabel(String path){
    	Tracing.printTrace("CONVERT DISK LABEL", path);// $NON-NLS-1$
    	
    	Pattern p = Pattern.compile("([a-zA-Z]"+IPath.DEVICE_SEPARATOR+")(\\\\|\\/)");
//    	Pattern p = Pattern.compile("([a-zA-Z]:)(\\\\|\\/)");
    	String newPath=path;
    	
    	if(shouldConvertPath(path)){
    		Matcher matcher = p.matcher(path);
    		if(matcher.find()){
    			String group0=matcher.group(0);
    			String group1=matcher.group(1);
    			String group2=matcher.group(2);
    			newPath=path.replace(group0, group1.toLowerCase()+group2);
    		}
    	}
    	return newPath;
    }

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}
	
	public static String getP4TicketsOSLocation() {
		String location = null;
		String os = System.getProperty("os.name");
		String home = System.getProperty("user.home");

		// If home and os aren't known then just return null
		if (home != null && os != null) {
			StringBuilder builtLocation = new StringBuilder(home);
			builtLocation.append(File.separatorChar);
			if (os.toLowerCase(Locale.ENGLISH).contains("windows")) {
				builtLocation.append(Server.P4TICKETS_DEFAULT_WINDOWS);
			} else {
				builtLocation.append(Server.P4TICKETS_DEFAULT_OTHER);
			}
			location = builtLocation.toString();
		}
		return location;
	}
	
    public IStreamSummary getStreamSummary(final String streamPath) {
    	final IStreamSummary[] result=new IStreamSummary[1];
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if(server instanceof IOptionsServer)
        			result[0]=((IOptionsServer) server).getStream(streamPath);
        	}
        };
        runOperation(operation);
        return result[0];
    }

	@Override
	public void createStream(final IStream stream) {
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if(server instanceof IOptionsServer)
        			((IOptionsServer) server).createStream(stream);
        	}
        };
        runOperation(operation);
	}

	@Override
	public void deleteStream(final String streamPath, final StreamOptions opts) {
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if(server instanceof IOptionsServer)
        			((IOptionsServer) server).deleteStream(streamPath, opts);
        	}
        };
        runOperation(operation);
	}

	@Override
	public void reloadStream(final String streamPath, final ReloadOptions opts){
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if(server instanceof IOptionsServer)
        			((IOptionsServer) server).reload(opts.setStream(streamPath));
        	}
        };
        runOperation(operation);
	}

	@Override
	public void unloadStream(final String streamPath, final UnloadOptions opts) {
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if(server instanceof IOptionsServer)
        			((IOptionsServer) server).unload(opts.setStream(streamPath).setLocked(true));
        	}
        };
        runOperation(operation);
	}

	public List<IFileSpec> populateStream(final IStream stream) {
		final List<IFileSpec> specs = new ArrayList<IFileSpec>();
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		IClient client = getClient();
    			List<IFileSpec> list= client
    					.populateFiles(
    							null,
    							null,
    							new PopulateFilesOptions()
    									.setStream(stream.getStream())
    									.setReverseMapping(true)
    									.setShowPopulatedFiles(true)
    									.setDescription(MessageFormat.format(Messages.Stream_Populate, stream.getStream(), stream.getParent())));
    			specs.addAll(list);
        	}
        };
        runOperation(operation);
        return specs;
	}

	@Override
	public IP4Resource[] populate(final String sourcePath, final String targetPath,
			boolean preview, String description) {
		
		P4Collection populated= new P4Collection();
		
		final PopulateFilesOptions options=new PopulateFilesOptions();
		options.setNoUpdate(preview);
		// NOTE: you can not preview with description. Otherwise, it will populate.
		if(!preview)
			options.setDescription(description);
		options.setShowPopulatedFiles(true);
		
        final List<IFileSpec> populateSpecs = new ArrayList<IFileSpec>();
        P4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException,
                    P4JavaError {
                IFileSpec fromSpec = null;
                if (sourcePath != null) {
                    fromSpec = new FileSpec(sourcePath);
                }
                IFileSpec toSpec = null;
                if (targetPath != null) {
                    toSpec = new FileSpec(targetPath);
                }
                List<IFileSpec> files = client.populateFiles(fromSpec, Arrays.asList(toSpec), options);
                populateSpecs.addAll(files);
            }
        };
        runOperation(op);

        if (!populateSpecs.isEmpty()) {
            for (IFileSpec integ : populateSpecs) {
                if (integ.getDepotPath()!=null && FileSpecOpStatus.VALID == integ.getOpStatus()) {
                    IP4Resource resource = null;
                    if (preview) {
                        resource = new P4File(integ, P4Connection.this);
                        ((IP4File) resource)
                                .setIntegrationSpecs(new IFileSpec[] { integ });
                    } else {
                        String path = integ.getToFile();
                        if (path == null) {
                            path = integ.getDepotPathString();
                        }
                        if (path != null) {
                            resource = getResource(path);
                            if (resource == null) {
                                resource = new P4File(integ,
                                        P4Connection.this);
                                updateResource(resource);
                            }
                        }
                    }
                    populated.add(resource);
                }
            }

			if (!preview) { // not preview, do a refresh local resources
				if (!populated.isEmpty()) {
					populated.refresh();
					P4Workspace.getWorkspace().notifyListeners(
							new P4Event(EventType.AVAILABLE, populated));
					populated.refreshLocalResources(IResource.DEPTH_INFINITE);
				}
			}
			handleErrors(populateSpecs.toArray(new IFileSpec[0]));
        }
        return populated.members();
	}

	@Override
	public List<IDepot> getDepots() {
        final List<IDepot> depots = new ArrayList<IDepot>();
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if (!(server instanceof IOptionsServer)) {
        			return;
        		}
        		List<IDepot> founded = ((IOptionsServer) server)
        				.getDepots();
        		if(founded!=null)
        			depots.addAll(founded);
        	}
        };
        runOperation(operation);
        return depots;
	}

    public List<IClientSummary> getClients(final GetClientsOptions opts) {
        final List<IClientSummary> clients = new ArrayList<IClientSummary>();
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if (!(server instanceof IOptionsServer)) {
        			return;
        		}
        		List<IClientSummary> founded = ((IOptionsServer) server)
        				.getClients(opts);
        		if(founded!=null)
        			clients.addAll(founded);
        	}
        };
        runOperation(operation);
        return clients;
    }

	@Override
	public void createClient(final IClient client) {
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if (!(server instanceof IOptionsServer)) {
        			return;
        		}
        		server.createClient(client);
        	}
        };
        runOperation(operation);
	}

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#updateClient(com.perforce.p4java.client.IClient)
     */
    public void updateClient(final IClient newClient) {
        if (newClient != null) {
            IP4ServerOperation operation = new P4ServerOperation() {
            	
            	public void run(IServer server) throws P4JavaException,
            	P4JavaError {
            		if (!(server instanceof IOptionsServer)) {
            			return;
            		}
                    server.updateClient(newClient);

                    // If updating ourselves then reconnect
                    if (client != null
                            && client.getName().equals(newClient.getName())) {
                        connect();
                    }
            	}
            };
            runOperation(operation);
        }
    }

    public List<String> getStreamViewMapping(final String streamPath){
    	final List<String> list=new ArrayList<String>();
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if (!(server instanceof OneShotServerImpl)) {
        			return;
        		}
                Pattern pattern=Pattern.compile("^View(\\d)+");
                
                List<Map<String, Object>> result = ((OneShotServerImpl) server).execQuietMapCmdList("stream", new String[]{"-o", "-v",streamPath}, null);
                for(Map<String, Object> map:result){
                    String[] keySet = map.keySet().toArray(new String[0]);
                    Arrays.sort(keySet);
                    for(int i=0;i<keySet.length;i++){
                        String key=keySet[i];
                        if(pattern.matcher(key).matches()){
                            list.add((String) map.get(key));
                        }
                    }
                }
        	}
        };
        runOperation(operation);
        return list;

    }

	@Override
	public List<IStreamSummary> getStreams(final List<String> paths,
			final GetStreamsOptions opts) {
        final List<IStreamSummary> streams = new ArrayList<IStreamSummary>();
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if (!(server instanceof IOptionsServer)) {
        			return;
        		}
        		List<IStreamSummary> founded = ((IOptionsServer)server).getStreams(paths, opts);;
        		if(founded!=null)
        			streams.addAll(founded);
        	}
        };
        runOperation(operation);
		return streams;
	}

	@Override
	public void updateStream(final IStream stream) {
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if (!(server instanceof IOptionsServer)) {
        			return;
        		}
        		((IOptionsServer)server).updateStream(stream, null);
        	}
        };
        runOperation(operation);
	}

	@Override
	public List<Map<String, Object>>  execMapCmdList(final String name, final String[] strings,
			final HashMap<String, Object> hashMap) {
		final List<Map<String, Object>> retMaps=new ArrayList<Map<String,Object>>();
		
        IP4ServerOperation operation = new P4ServerOperation() {
        	
        	public void run(IServer server) throws P4JavaException,
        	P4JavaError {
        		if (!(server instanceof IOptionsServer)) {
        			return;
        		}
        		List<Map<String, Object>> result = ((IOptionsServer)server).execMapCmdList(name, strings, hashMap); //$NON-NLS-1$
        		if(result!=null)
        			retMaps.addAll(result);
        	}
        };
        runOperation(operation);	
        return retMaps;
	}

	@Override
	public void clearCache() {
		resources.clear();
	}
	
	private void updateCache(String key, IP4Resource value){
		// possible tracing?
		// System.out.println(String.format(">>>UPDATE CACHE: [%s=%s]",key, value));
		resources.put(key, value);
	}
}
