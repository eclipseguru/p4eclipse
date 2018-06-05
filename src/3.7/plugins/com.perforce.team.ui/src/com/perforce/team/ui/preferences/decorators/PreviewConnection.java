/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences.decorators;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.StreamSummary;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.option.client.CopyFilesOptions;
import com.perforce.p4java.option.client.MergeFilesOptions;
import com.perforce.p4java.option.server.GetClientsOptions;
import com.perforce.p4java.option.server.GetStreamsOptions;
import com.perforce.p4java.option.server.MatchingLinesOptions;
import com.perforce.p4java.option.server.ReloadOptions;
import com.perforce.p4java.option.server.StreamOptions;
import com.perforce.p4java.option.server.UnloadOptions;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4ProgressListener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4Stream;
import com.perforce.team.core.p4java.PendingResourceManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PreviewConnection implements IP4Connection {

	private IP4Stream p4stream;
    private ConnectionParameters params;
    private boolean offline = false;
    private boolean sandbox = false;
	private String streamRoot=Messages.PreviewConnection_StreamRoot;
    
    /**
     * @param params
     * @param offline
     */
    public PreviewConnection(ConnectionParameters params, boolean offline, boolean sandbox, String streamRoot, String streamName) {
        this.params = params;
        this.offline = offline;
        this.sandbox = sandbox;
        this.streamRoot=streamRoot;
        this.p4stream=new P4Stream(this, new StreamSummary(true, streamRoot, null,
    			null, streamName, null, null,null,null,null));
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#connect()
     */
    public void connect() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#createChangelist(java.lang.String,
     *      com.perforce.team.core.p4java.IP4File[])
     */
    public IP4PendingChangelist createChangelist(String description,
            IP4File[] files) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#createChangelist(java.lang.String,
     *      com.perforce.team.core.p4java.IP4File[],
     *      com.perforce.team.core.p4java.IP4Job[])
     */
    public IP4PendingChangelist createChangelist(String description,
            IP4File[] files, IP4Job[] jobs) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#dispose()
     */
    public void dispose() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#findFiles(java.lang.String)
     */
    public IP4File[] findFiles(String startsWithPath) {
        return new IP4File[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getAddress()
     */
    public String getAddress() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getCachedPendingChangelists()
     */
    public IP4PendingChangelist[] getCachedPendingChangelists() {
        return new IP4PendingChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getClientName()
     */
    public String getClientName() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getClientRoot()
     */
    public String getClientRoot() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getDate()
     */
    public String getDate() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getDifferingFiles(java.lang.String[])
     */
    public IFileSpec[] getDifferingFiles(String[] paths) {
        return new IFileSpec[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFile(java.lang.String)
     */
    public IP4File getFile(String localOrDepotPath) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFile(com.perforce.p4java.core.file.IFileSpec)
     */
    public IP4File getFile(IFileSpec spec) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFolder(java.lang.String)
     */
    public IP4Folder getFolder(String depotPath) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobFields()
     */
    public String[] getJobFields() {
        return new String[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(int)
     */
    public IP4Job[] getJobs(int size) {
        return new IP4Job[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs()
     */
    public IP4Job[] getJobs() {
        return new IP4Job[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String[])
     */
    public IP4Job[] getJobs(String[] paths) {
        return new IP4Job[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String[],
     *      int)
     */
    public IP4Job[] getJobs(String[] paths, int size) {
        return new IP4Job[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String[],
     *      int, java.lang.String)
     */
    public IP4Job[] getJobs(String[] paths, int size, String jobViewPath) {
        return new IP4Job[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String)
     */
    public IP4Job[] getJobs(String path) {
        return new IP4Job[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobs(java.lang.String,
     *      int)
     */
    public IP4Job[] getJobs(String path, int size) {
        return new IP4Job[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLicense()
     */
    public String getLicense() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getMappedProjects()
     */
    public IProject[] getMappedProjects() {
        return new IProject[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getMissingFiles(java.lang.String[])
     */
    public IFileSpec[] getMissingFiles(String[] paths) {
        return new IFileSpec[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getOpenedBy(java.lang.String)
     */
    public IP4File[] getOpenedBy(String path) {
        return new IP4File[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getParameters()
     */
    public ConnectionParameters getParameters() {
        return this.params;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelist(int)
     */
    public IP4PendingChangelist getPendingChangelist(int changelistId) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelists(boolean)
     */
    public IP4PendingChangelist[] getPendingChangelists(boolean all) {
        return new IP4PendingChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getResource(java.lang.String)
     */
    public IP4Resource getResource(String localOrDepotPath) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getResource(org.eclipse.core.resources.IResource)
     */
    public IP4Resource getResource(IResource resource) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getRoot()
     */
    public String getRoot() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServer()
     */
    public IServer getServer() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(int)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(int size) {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists()
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists() {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String[])
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String[] paths) {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String[],
     *      int)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String[] paths,
            int size) {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String[],
     *      int, java.lang.String, java.lang.String)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String[] paths,
            int size, String user, String clientWorkspace) {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String path) {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelists(java.lang.String,
     *      int)
     */
    public IP4SubmittedChangelist[] getSubmittedChangelists(String path,
            int size) {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getUptime()
     */
    public String getUptime() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getUser()
     */
    public String getUser() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getVersion()
     */
    public String getVersion() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#hasMappedProjects()
     */
    public boolean hasMappedProjects() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isConnected()
     */
    public boolean isConnected() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isDisposed()
     */
    public boolean isDisposed() {
        return false;
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
        return this.sandbox;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isSecure()
     */
    public boolean isSecure() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isSupported()
     */
    public boolean isSupported() {
        return true;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#login(java.lang.String)
     */
    public boolean login(String password) {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#logout()
     */
    public void logout() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#refresh()
     */
    public void refresh() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#refreshJobSpec()
     */
    public void refreshJobSpec() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#refreshServer()
     */
    public boolean refreshServer() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#removeChangelist(com.perforce.team.core.p4java.IP4PendingChangelist)
     */
    public void removeChangelist(IP4PendingChangelist changelist) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#removeChangelist(int)
     */
    public void removeChangelist(int changelist) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#removeFileFromChangelists(com.perforce.team.core.p4java.IP4File)
     */
    public void removeFileFromChangelists(IP4File file) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#setOffline(boolean)
     */
    public void setOffline(boolean offline) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#setShowClientOnly(boolean)
     */
    public void setShowClientOnly(boolean showClientOnly) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#setShowFoldersWIthOnlyDeletedFiles(boolean)
     */
    public void setShowFoldersWIthOnlyDeletedFiles(boolean showDeleted) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#showClientOnly()
     */
    public boolean showClientOnly() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#showFoldersWithOnlyDeletedFiles()
     */
    public boolean showFoldersWithOnlyDeletedFiles() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#updateResource(com.perforce.team.core.p4java.IP4Resource)
     */
    public void updateResource(IP4Resource resource) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#updateRevertedFiles(com.perforce.team.core.p4java.P4Collection)
     */
    public void updateRevertedFiles(P4Collection collection) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#getAllLocalFiles()
     */
    public IP4File[] getAllLocalFiles() {
        return new IP4File[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#markForRefresh()
     */
    public void markForRefresh() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#members()
     */
    public IP4Resource[] members() {
        return new IP4Resource[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#needsRefresh()
     */
    public boolean needsRefresh() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#add()
     */
    public void add() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#add(int)
     */
    public void add(int changelist) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#delete()
     */
    public void delete() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#delete(int)
     */
    public void delete(int changelist) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#edit()
     */
    public void edit() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#edit(int)
     */
    public void edit(int changelist) {

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
    	Client client = new Client();
    	client.setStream(streamRoot);
    	
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
        return this;
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
     * @see com.perforce.team.core.p4java.IP4Resource#ignore()
     */
    public void ignore() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isContainer()
     */
    public boolean isContainer() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isReadOnly()
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh(int)
     */
    public void refresh(int depth) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#revert()
     */
    public void revert() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#sync()
     */
    public void sync(IProgressMonitor monitor) {

    }
    
	public void sync(IProgressMonitor monitor, IP4ProgressListener callback) {
		
	}

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#getErrorHandler()
     */
    public IErrorHandler getErrorHandler() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#handleError(com.perforce.p4java.exception.P4JavaException)
     */
    public boolean handleError(P4JavaException exception) {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#handleErrors(com.perforce.p4java.core.file.IFileSpec[])
     */
    public void handleErrors(IFileSpec[] specs) {

    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#setErrorHandler(com.perforce.team.core.p4java.IErrorHandler)
     */
    public void setErrorHandler(IErrorHandler handler) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJobSpec()
     */
    public IJobSpec getJobSpec() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFixes(com.perforce.team.core.p4java.IP4Job)
     */
    public IP4SubmittedChangelist[] getFixes(IP4Job job) {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelistById(int)
     */
    public IP4SubmittedChangelist getSubmittedChangelistById(int id) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelistById(int)
     */
    public IP4PendingChangelist getPendingChangelistById(int id) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#printToTempFile(java.lang.String)
     */
    public File printToTempFile(String specPath) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getAllClients()
     */
    public IClient[] getAllClients() {
        return new IClient[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getOwnedClients()
     */
    public IClient[] getOwnedClients() {
        return new IClient[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFolder(java.lang.String,
     *      boolean)
     */
    public IP4Folder getFolder(String depotPath, boolean refreshIfNotFound) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getRootSpec()
     */
    public String getRootSpec() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfo()
     */
    public IServerInfo getServerInfo() {
        return null;
    }
    
    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfoClientAddress()
     */
    public String getServerInfoClientAddress() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfoClientHost()
     */
    public String getServerInfoClientHost() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfoClientName()
     */
    public String getServerInfoClientName() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getServerInfoClientRoot()
     */
    public String getServerInfoClientRoot() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#createJob(java.util.Map)
     */
    public IP4Job createJob(Map<String, Object> jobFields)
            throws P4JavaException {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isMoveSupported()
     */
    public boolean isMoveSupported() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isLoggedIn()
     */
    public boolean isLoggedIn() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#markLoggedOut()
     */
    public void markLoggedOut() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#refreshClient()
     */
    public boolean refreshClient() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getCurrentDirectory()
     */
    public String getCurrentDirectory() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getOwnedLocalClients()
     */
    public IClient[] getOwnedLocalClients() {
        return new IClient[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(int)
     */
    public IP4Label[] getLabels(int size) {
        return new IP4Label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels()
     */
    public IP4Label[] getLabels() {
        return new IP4Label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String[])
     */
    public IP4Label[] getLabels(String[] paths) {
        return new IP4Label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String[],
     *      int)
     */
    public IP4Label[] getLabels(String[] paths, int size) {
        return new IP4Label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String[],
     *      int, java.lang.String)
     */
    public IP4Label[] getLabels(String[] paths, int size, String nameFilter) {
        return new IP4Label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String,
     *      java.lang.String[], int, java.lang.String)
     */
    public IP4Label[] getLabels(String user, String[] paths, int size,
            String nameFilter) {
        return new IP4Label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String)
     */
    public IP4Label[] getLabels(String path) {
        return new IP4Label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabels(java.lang.String,
     *      int)
     */
    public IP4Label[] getLabels(String path, int size) {
        return new IP4Label[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#integrate(com.perforce.team.core.p4java.P4FileIntegration,
     *      int, boolean, boolean,
     *      com.perforce.team.core.p4java.P4IntegrationOptions)
     */
    public IP4Resource[] integrate(P4FileIntegration integration,
            int changelist, boolean preview, boolean all,
            P4IntegrationOptions options) {
        return new IP4Resource[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#integrate(com.perforce.team.core.p4java.P4FileIntegration,
     *      java.lang.String, int, boolean, boolean,
     *      com.perforce.team.core.p4java.P4IntegrationOptions)
     */
    public IP4Resource[] integrate(P4FileIntegration integration,
            String branch, int changelist, boolean preview, boolean all,
            P4IntegrationOptions options) {
        return new IP4Resource[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#hasServer()
     */
    public boolean hasServer() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getJob(java.lang.String)
     */
    public IP4Job getJob(String id) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLabel(java.lang.String)
     */
    public IP4Label getLabel(String labelName) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#loadPendingChangelist(int)
     */
    public void loadPendingChangelist(int id) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getIntVersion()
     */
    public int getIntVersion() {
        return 0;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranch(java.lang.String)
     */
    public IP4Branch getBranch(String branchName) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranches(int)
     */
    public IP4Branch[] getBranches(int size) {
        return new IP4Branch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranches()
     */
    public IP4Branch[] getBranches() {
        return new IP4Branch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranches(java.lang.String,
     *      int, java.lang.String)
     */
    public IP4Branch[] getBranches(String user, int size, String nameFilter) {
        return new IP4Branch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getBranches(java.lang.String,
     *      int)
     */
    public IP4Branch[] getBranches(String user, int size) {
        return new IP4Branch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getActivePendingChangelist()
     */
    public IP4PendingChangelist getActivePendingChangelist() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#setActivePendingChangelist(int)
     */
    public void setActivePendingChangelist(int id) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelist(int,
     *      boolean)
     */
    public IP4PendingChangelist getPendingChangelist(int changelistId,
            boolean fetchIfNotFound) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#createBranch(com.perforce.p4java.core.IBranchSpec)
     */
    public IP4Branch createBranch(IBranchSpec spec) throws P4JavaException {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isMoveServerOnlySupported()
     */
    public boolean isMoveServerOnlySupported() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists()
     */
    public IP4ShelvedChangelist[] getShelvedChangelists() {
        return new IP4ShelvedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(int)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(int size) {
        return new IP4ShelvedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String[])
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String[] paths) {
        return new IP4ShelvedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String[],
     *      int)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String[] paths, int size) {
        return new IP4ShelvedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String[],
     *      int, java.lang.String, java.lang.String)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String[] paths,
            int size, String user, String clientWorkspace) {
        return new IP4ShelvedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String path) {
        return new IP4ShelvedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getShelvedChangelists(java.lang.String,
     *      int)
     */
    public IP4ShelvedChangelist[] getShelvedChangelists(String path, int size) {
        return new IP4ShelvedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isShelvingSupported()
     */
    public boolean isShelvingSupported() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getLoggedInTime()
     */
    public long getLoggedInTime() {
        return 0;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getPendingChangelist(int,
     *      boolean, boolean)
     */
    public IP4PendingChangelist getPendingChangelist(int changelistId,
            boolean fetchIfNotFound, boolean ignoreErrors) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#updateClient(com.perforce.p4java.client.IClient)
     */
    public void updateClient(IClient newClient) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getChangelistById(int)
     */
    public IP4Changelist getChangelistById(int id) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getChangelistById(int,
     *      com.perforce.p4java.core.ChangelistStatus, boolean, boolean)
     */
    public IP4Changelist getChangelistById(int id, ChangelistStatus type,
            boolean checkClient, boolean ignoreErrors) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getChangelistById(int,
     *      com.perforce.p4java.core.ChangelistStatus, boolean)
     */
    public IP4Changelist getChangelistById(int id, ChangelistStatus type,
            boolean checkClient) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isFile()
     */
    public boolean isFile() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFixes(java.lang.String)
     */
    public IP4Changelist[] getFixes(String jobId) {
        return new IP4Changelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isPendingLoaded()
     */
    public boolean isPendingLoaded() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSpecDepot()
     */
    public P4Depot getSpecDepot() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFixIds(java.lang.String)
     */
    public Integer[] getFixIds(String jobId) {
        return new Integer[0]; // to avoid Coverity "defeference null return value"
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getFixIds(com.perforce.team.core.p4java.IP4Job)
     */
    public Integer[] getFixIds(IP4Job job) {
        return new Integer[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getSubmittedChangelistById(int,
     *      boolean)
     */
    public IP4SubmittedChangelist getSubmittedChangelistById(int id,
            boolean ignoreErrors) {
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getOpenedManager()
     */
    public PendingResourceManager getOpenedManager() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#integrate(com.perforce.team.core.p4java.P4FileIntegration,
     *      java.lang.String, int, java.lang.String, boolean, boolean,
     *      com.perforce.team.core.p4java.P4IntegrationOptions)
     */
    public IP4Resource[] integrate(P4FileIntegration integration,
            String branch, int changelist, String description, boolean preview,
            boolean all, P4IntegrationOptions options) {
        return new IP4Resource[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isCaseSensitive()
     */
    public boolean isCaseSensitive() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getInterchanges(java.lang.String,
     *      java.lang.String)
     */
    public IP4SubmittedChangelist[] getInterchanges(String sourcePath,
            String targetPath) {
        return new IP4SubmittedChangelist[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isSearchSupported()
     */
    public boolean isSearchSupported() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getDiffs(java.lang.String,
     *      java.lang.String)
     */
    public IFileDiff[] getDiffs(String path1, String path2) {
        return new IFileDiff[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getHistory(java.lang.String[],
     *      int)
     */
    public IP4Revision[] getHistory(String[] paths, int max) {
        return new IP4Revision[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String)
     */
    public IFileLineMatch[] searchDepot(String pattern) {
        return new IFileLineMatch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String,
     *      java.lang.String)
     */
    public IFileLineMatch[] searchDepot(String pattern, String path) {
        return new IFileLineMatch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String,
     *      java.lang.String[])
     */
    public IFileLineMatch[] searchDepot(String pattern, String[] paths) {
        return new IFileLineMatch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String,
     *      com.perforce.p4java.core.file.MatchingLinesOptions,
     *      java.lang.String)
     */
    public IFileLineMatch[] searchDepot(String pattern,
            MatchingLinesOptions options, String path) {
        return new IFileLineMatch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#searchDepot(java.lang.String,
     *      com.perforce.p4java.core.file.MatchingLinesOptions,
     *      java.lang.String[])
     */
    public IFileLineMatch[] searchDepot(String pattern,
            MatchingLinesOptions options, String[] paths) {
        return new IFileLineMatch[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#isOwner(java.lang.String)
     */
    public boolean isOwner(String user) {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#size()
     */
    public int size() {
        return 0;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Connection#getDepot(java.lang.String)
     */
    public P4Depot getDepot(String name) {
        return null;
    }

	public List<IP4Stream> getFilteredStreams(boolean unloaded, List<String> paths,
			String filter, int size) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	public IP4Stream getStream(String path) {
		return p4stream;
	}

    public IP4Resource[] copyStream(P4FileIntegration integration,
            String description, CopyFilesOptions options) {
        // TODO Auto-generated method stub
        return new IP4Resource[0];
    }

    public IP4Resource[] mergeStream(P4FileIntegration integration,
            String description, MergeFilesOptions options) {
        // TODO Auto-generated method stub
        return new IP4Resource[0];
    }

	public IP4File[] getFiles(IFileSpec[] specs) {
		// TODO Auto-generated method stub
		return new IP4File[0];
	}

	public void updateResources(IP4Resource[] resources) {
		// TODO Auto-generated method stub
		
	}

	public boolean updateResourceThenCheckNotify(IP4Resource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAttribute(String key, Object value) {
		// TODO Auto-generated method stub
		
	}

	public IStreamSummary getStreamSummary(String stream) {
		return p4stream.getStreamSummary();
	}

	@SuppressWarnings("unchecked")
	public List<IFileSpec> populateStream(IStream stream) {
		return Collections.EMPTY_LIST;
	}

	public void createStream(IStream stream){
		// TODO Auto-generated method stub
		
	}

	public void deleteStream(String streamPath, StreamOptions opts){
		// TODO Auto-generated method stub
		
	}

	public void reloadStream(String streamPath, ReloadOptions opts){
		// TODO Auto-generated method stub
		
	}

	public void unloadStream(String streamPath, UnloadOptions opts){
		// TODO Auto-generated method stub
		
	}

	public IP4Resource[] populate(String sourcePath, String targetPath,
			boolean preview, String description) {
		// TODO Auto-generated method stub
		return new IP4Resource[0];
	}

	public List<IClientSummary> getClients(GetClientsOptions opts) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	public void createClient(IClient iClient) {
		// TODO Auto-generated method stub
		
	}

	public List<String> getStreamViewMapping(String stream) {
		return Collections.emptyList();
	}

	public List<IDepot> getDepots() {
		return Collections.emptyList();
	}

	public List<IStreamSummary> getStreams(List<String> paths,
			GetStreamsOptions opts) {
		return Collections.emptyList();
	}

	public void updateStream(IStream editStream) {
		// TODO Auto-generated method stub
		
	}

	public List<Map<String, Object>> execMapCmdList(String name, String[] strings,
			HashMap<String, Object> hashMap) {
				return Collections.emptyList();
	}

	public void clearCache() {
		// TODO Auto-generated method stub
		
	}

}
