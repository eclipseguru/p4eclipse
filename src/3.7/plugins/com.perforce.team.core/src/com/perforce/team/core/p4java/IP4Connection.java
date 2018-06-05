/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

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

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Connection extends IP4Container {

    /**
     * CLIENT_NON_EXISTENT_PREFIX
     */
    String CLIENT_NON_EXISTENT_PREFIX = "Client '";

    /**
     * CLIENT_NON_EXISTENT_SUFFIX
     */
    String CLIENT_NON_EXISTENT_SUFFIX = "' does not exist";

    /**
     * INTEGRATE_DEFAULT_DESCRIPTION
     */
    String INTEGRATE_DEFAULT_DESCRIPTION = Messages.IP4Connection_2;

    /**
     * Minimum server version to use 'move -k' command
     */
    int MOVE_SERVER_ONLY_SERVER_VERSION = 20092;

    /**
     * Minimum server version to use 'move' command
     */
    int MOVE_SERVER_VERSION = 20091;

    /**
     * Minimum server version supported by p4eclipse
     */
    int MINIMUM_SERVER_VERSION = 20052;

    /**
     * -u option available for clients, branches, and labels
     */
    int USER_FILTER_SERVER_VERSION = 20062;

    /**
     * -e option available for clients, branches, and labels
     */
    int NAME_FILTER_SERVER_VERSION = 20081;

    /**
     * -m option available for clients, branches, labels, fstat, etc. Does not
     * apply to changes or jobs commands.
     */
    int MAX_FILTER_SERVER_VERSION = 20061;

    /**
     * shelve/unshelve commands available
     */
    int SHELVE_SERVER_VERSION = 20092;

    /**
     * SEARCH_SERVER_VERSION
     */
    int SEARCH_SERVER_VERSION = 20101;

    /**
     * MINIMUM_SERVER_LABEL
     */
    String MINIMUM_SERVER_LABEL = "2005.2"; //$NON-NLS-1$

    /**
     * ROOT - //...
     */
    String ROOT = DEPOT_PREFIX + ELLIPSIS;
    
    /**
     * GET attribute
     */
    Object getAttribute(String key);
    
    /**
     * SET attribute
     */
    void setAttribute(String key, Object value);
    
    /**
     * Gets the connection parameters used by this connection
     * 
     * @return - connection parameters
     */
    ConnectionParameters getParameters();

    /**
     * Gets the p4j server object this connection is using
     * 
     * @return - p4j server
     */
    IServer getServer();

    /**
     * Has this connection obtained successfully a p4j server from the p4java
     * server factory?
     * 
     * @return - true if server field is set, false otherwise
     */
    boolean hasServer();

    /**
     * Refreshes the cached p4java server object
     * 
     * @return - true if successfully obtained
     */
    boolean refreshServer();

    /**
     * Refreshes the cached p4java client object
     * 
     * @return - true if successfully obtained
     */
    boolean refreshClient();

    /**
     * Connect to the server configured for this connection
     */
    void connect();

    /**
     * Refresh the cached information about the server used by this connection
     */
    void refresh();

    /**
     * Sets the offline state of this connection
     * 
     * @param offline
     */
    void setOffline(boolean offline);

    /**
     * Log in to the server configured for this connection
     * 
     * @param password
     * @return - true if login succeeded, false otherwise
     */
    boolean login(String password);

    /**
     * Logs out of the server configured for this connection
     */
    void logout();

    /**
     * Was the last call to {@link IServer}{@link #login(String)} successful?
     * 
     * @return - true if login call succeeded
     */
    boolean isLoggedIn();

    /**
     * Get the last timestamp of a successful login
     * 
     * @return - last successful login time or -1 if login never occured
     *         successfully
     */
    long getLoggedInTime();

    /**
     * Mark the connection as logged out
     */
    void markLoggedOut();

    /**
     * Is the connection offline?
     * 
     * @return - true if offline, false otherwise
     */
    boolean isOffline();

    /**
     * Is the connection to a sandbox server?
     * 
     * @return - true if a sandbox connection, false otherwise
     */
    boolean isSandbox();
    
    /**
     * Is the connection connected?
     * 
     * @return - true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Is this connection no longer in the workspace model?
     * 
     * @return - true if not in model and should go un-used
     */
    boolean isDisposed();

    /**
     * Gets the user of this connection
     * 
     * @return - user name
     */
    String getUser();

    /**
     * Gets the client name of this connection
     * 
     * @return - client name
     */
    String getClientName();

    /**
     * Gets the client root of this connection
     * 
     * @return - client root
     */
    String getClientRoot();

    /**
     * Gets the server version of this connection
     * 
     * @return - server version
     */
    String getVersion();

    /**
     * Get the server version as an integer
     * 
     * @return - server version as an integer
     */
    int getIntVersion();

    /**
     * Gets the server address of this connection
     * 
     * @return - server address
     */
    String getAddress();

    /**
     * Gets the server root of this connection
     * 
     * @return - server root
     */
    String getRoot();

    /**
     * Gets the date of the server
     * 
     * @return - server date
     */
    String getDate();

    /**
     * Gets the uptime of the server
     * 
     * @return - server uptime
     */
    String getUptime();

    /**
     * Gets the license of the server
     * 
     * @return - server license
     */
    String getLicense();

    /**
     * Is this connection secure?
     * 
     * @return - true if secure, false otherwise
     */
    boolean isSecure();

    /**
     * Gets stream summaries.
     * 
     * @param unloaded streams -U option, <em>true</em> to show unloaded streams only, <em>false</em> to show loaded streams only
     * @param paths
     * @param filter must be null if no filter
     * @param size maximum size of stream summaries to get. (see p4 streams -m)
     * @return stream summaries.
     */
    List<IP4Stream> getFilteredStreams(boolean unloaded, List<String> paths, String filter, int size);

    /**
     * Get stream.
     * 
     * @param path String path
     * @return A stream
     */
    IP4Stream getStream(String path);
    
    /**
     * Gets a submitted changelist with the specified id. This will be retrieved
     * from the server.
     * 
     * @param id
     * @return - submitted changelist
     */
    IP4SubmittedChangelist getSubmittedChangelistById(int id);

    /**
     * Gets a submitted changelist with the specified id. This will be retrieved
     * from the server.
     * 
     * @param id
     * @param ignoreErrors
     * @return - submitted changelist
     */
    IP4SubmittedChangelist getSubmittedChangelistById(int id,
            boolean ignoreErrors);

    /**
     * Gets the submitted changelists. If the size is zero then this method will
     * return all the submitted changelists.
     * 
     * @param size
     *            - number of changelists to retrieve
     * @return - array of submitted changelists
     */
    IP4SubmittedChangelist[] getSubmittedChangelists(int size);

    /**
     * Gets all the submitted changelists
     * 
     * @return - array of submitted changelists
     */
    IP4SubmittedChangelist[] getSubmittedChangelists();

    /**
     * Gets all the submitted changelists for an array of paths
     * 
     * @param paths
     * @return - array of submitted changelists
     */
    IP4SubmittedChangelist[] getSubmittedChangelists(String[] paths);

    /**
     * Gets the submitted changelists for an array of paths
     * 
     * @param paths
     * @param size
     * @return - array of submitted changelists
     */
    IP4SubmittedChangelist[] getSubmittedChangelists(String[] paths, int size);

    /**
     * Gets the
     * 
     * @param paths
     * @param size
     * @param user
     * @param clientWorkspace
     * @return - array of submitted changelists
     */
    IP4SubmittedChangelist[] getSubmittedChangelists(String[] paths, int size,
            String user, String clientWorkspace);

    /**
     * Gets all the submitted changelists for a path
     * 
     * @param path
     * @return - array of submitted changelists
     */
    IP4SubmittedChangelist[] getSubmittedChangelists(String path);

    /**
     * Gets the submitted changelists for a path
     * 
     * @param path
     * @param size
     * @return - array of submitted changelists
     */
    IP4SubmittedChangelist[] getSubmittedChangelists(String path, int size);

    /**
     * Gets the pending changelists for this connection
     * 
     * @param all
     * @return - array of p4 changelists
     */
    IP4PendingChangelist[] getPendingChangelists(boolean all);

    /**
     * Gets the pending changelist
     * 
     * @param changelistId
     * @return - p4 changelist
     */
    IP4PendingChangelist getPendingChangelist(int changelistId);

    /**
     * Gets the pending changelist
     * 
     * @param changelistId
     * @param fetchIfNotFound
     *            - fetch from server if not in cache
     * @return - p4 changelist
     */
    IP4PendingChangelist getPendingChangelist(int changelistId,
            boolean fetchIfNotFound);

    /**
     * Gets the pending changelist
     * 
     * @param changelistId
     * @param fetchIfNotFound
     *            - fetch from server if not in cache
     * @param ignoreErrors
     *            - ignore errors if changelist is unknown
     * @return - p4 changelist
     */
    IP4PendingChangelist getPendingChangelist(int changelistId,
            boolean fetchIfNotFound, boolean ignoreErrors);

    /**
     * Gets a pending changelist with the specified id. This will be retrieved
     * from the server.
     * 
     * @param id
     * @return - pending changelist
     */
    IP4PendingChangelist getPendingChangelistById(int id);

    /**
     * Load a pending changelist into the connection cache of changelists. Will
     * only add changelists to the cache if the specified id is successfully
     * obtained and is on the client.
     * 
     * @param id
     */
    void loadPendingChangelist(int id);

    /**
     * Creates a changelist from a description and array of files that will be
     * re-opened for the new list
     * 
     * @param description
     * @param files
     * @return - p4 changelist
     */
    IP4PendingChangelist createChangelist(String description, IP4File[] files);

    /**
     * Creates a changelist from a description and array of files that will be
     * re-opened for the new list and array of jobs that will be fixed by the
     * created list
     * 
     * @param description
     * @param files
     * @param jobs
     * @return - p4 changelist
     */
    IP4PendingChangelist createChangelist(String description, IP4File[] files,
            IP4Job[] jobs);

    /**
     * Gets a p4 resource either that is mapped by local or depot path from the
     * cache
     * 
     * @param localOrDepotPath
     * @return - p4 resource
     */
    IP4Resource getResource(String localOrDepotPath);

    /**
     * Gets a p4 file
     * 
     * @param localOrDepotPath
     * @return - p4 file
     */
    IP4File getFile(String localOrDepotPath);

    /**
     * Gets a p4 file matching the path of the spec
     * 
     * @param spec
     * @return - p4 file, either existing or created with the specified spec.
     */
    IP4File getFile(IFileSpec spec);

    /**
     * Gets p4 files matching the path of the specs
     * 
     * @param specs
     * @return - p4 files, either existing or created with the specified specs.
     */
    IP4File[] getFiles(IFileSpec specs[]);

    /**
     * Gets a p4 folder that is cached or newly created and optionally refreshed
     * 
     * @param depotPath
     * @param refreshIfNotFound
     * @return - p4 folder
     */
    IP4Folder getFolder(String depotPath, boolean refreshIfNotFound);

    /**
     * Gets a p4 folder retrieved by the depot path specified
     * 
     * @param depotPath
     * @return - p4 folder
     */
    IP4Folder getFolder(String depotPath);

    /**
     * Gets a p4 resource for the local path of the resource passed in
     * 
     * @param resource
     * @return - p4 resource
     */
    IP4Resource getResource(IResource resource);

    /**
     * Updates a p4 resource with the latest local and depot path
     * 
     * @param resource
     */
    void updateResource(IP4Resource resource);

    /**
     * Updates p4 resources with the latest local and depot path
     * 
     * @param resources
     */
    void updateResources(IP4Resource[] resources);

    /**
     * Update a p4 resource and return whether this resource change need fire a notification or not
     * @param resource
     * @return - boolean
     */
    boolean updateResourceThenCheckNotify(IP4Resource resource);
    
    /**
     * Disposes this connection
     */
    void dispose();

    /**
     * Does copy to firmer stream.
     * @param integration
     * @param description
     * @param options
     * @return
     */
    IP4Resource[] copyStream(P4FileIntegration integration,
            String description, CopyFilesOptions options);
    
    /**
     * Does merge to softer stream.
     * @param integration
     * @param description
     * @param options
     * @return
     */
    IP4Resource[] mergeStream(P4FileIntegration integration,
            String description, 
            MergeFilesOptions options);
    
    /**
     * Does an integrate with a branch
     * 
     * @param integration
     * @param changelist
     * @param preview
     * @param all
     * @param options
     * @return - integrated resources
     */
    IP4Resource[] integrate(P4FileIntegration integration, int changelist,
            boolean preview, boolean all, P4IntegrationOptions options);

    /**
     * Does an integrate with the specified parameters
     * 
     * @param integration
     * @param branch
     * @param changelist
     * @param preview
     * @param all
     * @param options
     * 
     * @return - integrated resources
     */
    IP4Resource[] integrate(P4FileIntegration integration, String branch,
            int changelist, boolean preview, boolean all,
            P4IntegrationOptions options);

    /**
     * Does an integrate with the specified parameters
     * 
     * @param integration
     * @param branch
     * @param changelist
     * @param description
     * @param preview
     * @param all
     * @param options
     * 
     * @return - integrated resources
     */
    IP4Resource[] integrate(P4FileIntegration integration, String branch,
            int changelist, String description, boolean preview, boolean all,
            P4IntegrationOptions options);

    /**
     * Gets the jobs. If the size is zero then this method will return all the
     * jobs.
     * 
     * @param size
     *            - number of jobs to retrieve
     * @return - array of jobs
     */
    IP4Job[] getJobs(int size);

    /**
     * Gets all the jobs
     * 
     * @return - array of jobs
     */
    IP4Job[] getJobs();

    /**
     * Gets all the jobs for an array of paths
     * 
     * @param paths
     * @return - array of jobs
     */
    IP4Job[] getJobs(String[] paths);

    /**
     * Gets all the jobs for an array of paths
     * 
     * @param paths
     * @param size
     * @return - array of jobs
     */
    IP4Job[] getJobs(String[] paths, int size);

    /**
     * Gets the jobs that satisfy the paths and jobs view filter path
     * 
     * @param paths
     * @param size
     * @param jobViewPath
     * @return - array of jobs
     */
    IP4Job[] getJobs(String[] paths, int size, String jobViewPath);

    /**
     * Gets all the jobs a path
     * 
     * @param path
     * @return - array of jobs
     */
    IP4Job[] getJobs(String path);

    /**
     * Gets all the jobs for a path
     * 
     * @param path
     * @param size
     * @return - array of jobs
     */
    IP4Job[] getJobs(String path, int size);

    /**
     * Get a job by id from the server, non-cached
     * 
     * @param id
     * @return - p4 job
     */
    IP4Job getJob(String id);

    /**
     * Gets the job fields from the currently cached job spec. This method will
     * fetch the job spec if it hasn't been fetched yet.
     * 
     * @return - array of job field names
     */
    String[] getJobFields();

    /**
     * Refreshes the cached p4j job spec
     */
    void refreshJobSpec();

    /**
     * Get the job spec associated with this connection, will be fetched from
     * the server if not current set
     * 
     * @return - job spec
     */
    IJobSpec getJobSpec();

    /**
     * Gets a p4 file array of all the opened version of a particular path
     * 
     * @param path
     * @return - p4 file array
     */
    IP4File[] getOpenedBy(String path);

    /**
     * Removes reverted files from any cached pending changelists
     * 
     * @param collection
     */
    void updateRevertedFiles(P4Collection collection);

    /**
     * Removes a file from any cached pending changelists
     * 
     * @param file
     */
    void removeFileFromChangelists(IP4File file);

    /**
     * Removes a changelist from this connection's changelist cache, pending
     * changelist must have an id to be removed
     * 
     * @param changelist
     */
    void removeChangelist(IP4PendingChangelist changelist);

    /**
     * Removes a changelist from this connection's changelist cache
     * 
     * @param changelist
     *            - id of changelist
     */
    void removeChangelist(int changelist);

    /**
     * Gets the cached pending changelists
     * 
     * @return - pending changelist array
     */
    IP4PendingChangelist[] getCachedPendingChangelists();

    /**
     * Have pending changelist been loaded?
     * 
     * @return - true if loaded, false otherwise
     */
    boolean isPendingLoaded();

    /**
     * Get the missing files
     * 
     * @param paths
     * @return - missing files
     */
    IFileSpec[] getMissingFiles(String[] paths);

    /**
     * Get the differing files
     * 
     * @param paths
     * @return - differing files
     */
    IFileSpec[] getDifferingFiles(String[] paths);

    /**
     * Are any project managed by this connection?
     * 
     * @return - true if at least one project is mapped to this connection
     */
    boolean hasMappedProjects();

    /**
     * Gets the array of projects currently mapped to this connection
     * 
     * @return - non-null array of mapped projects
     */
    IProject[] getMappedProjects();

    /**
     * Is this connection's {@link IP4Container} children only showing files
     * mapped through the client view?
     * 
     * @return - true if showing client mapped files only
     */
    boolean showClientOnly();

    /**
     * Set to only show files mapped in the client view when getting the members
     * of {@link IP4Container} objects under this connection
     * 
     * @param showClientOnly
     *            the showClientOnly to set
     */
    void setShowClientOnly(boolean showClientOnly);

    /**
     * Is this connection's {@link IP4Container} members displaying folders that
     * only contain deleted files
     * 
     * @return - true if showing folders that only contain deleted files
     */
    boolean showFoldersWithOnlyDeletedFiles();

    /**
     * Set to show folders that only contains deleted files. This affects the
     * members returned from this connection's {@link IP4Container} members.
     * 
     * @param showDeleted
     */
    void setShowFoldersWIthOnlyDeletedFiles(boolean showDeleted);

    /**
     * Find all the files cached in this connection that start with the
     * specified path prefix
     * 
     * @param startsWithPath
     * @return - array of p4 files
     */
    IP4File[] findFiles(String startsWithPath);

    /**
     * Is this server version supported?
     * 
     * @return - true if supported, false otherwise
     */
    boolean isSupported();

    /**
     * Get the client root file spec
     * 
     * @return - client root file spec
     */
    String getRootSpec();

    /**
     * Get the changelists that fix the specified job i
     * 
     * @param jobId
     * @return - pending or submitted changelists
     */
    IP4Changelist[] getFixes(String jobId);

    /**
     * Get the changelists that fix this job
     * 
     * @param job
     * @return - pending or submitted changelists
     */
    IP4Changelist[] getFixes(IP4Job job);

    /**
     * Get the changelists id that fix the specified job id.
     * 
     * @param jobId
     * @return - non-null but possible empty array of pending/submitted
     *         changelist ids
     */
    Integer[] getFixIds(String jobId);

    /**
     * Get the changelists id that fix the specified job
     * 
     * @param job
     * @return - non-null but possible empty array of pending/submitted
     *         changelist ids
     */
    Integer[] getFixIds(IP4Job job);

    /**
     * Do a perforce print on a spec path into a temp file
     * 
     * @param specPath
     * @return - file created or null if failed
     */
    File printToTempFile(String specPath);

    /**
     * Get all the owned client specs
     * 
     * @return - array of p4j client specs
     */
    IClientSummary[] getOwnedClients();

    /**
     * Get all the clients on this connection
     * 
     * @return - array of p4j client specs
     */
    IClientSummary[] getAllClients();

    /**
     * Get all the owned local client specs
     * 
     * @return - array of p4j client spec
     */
    IClientSummary[] getOwnedLocalClients();

    /**
     * Update a perforce client on the server
     * 
     * @param newClient
     * @throws P4JavaException
     *             - error saving client
     */
    void updateClient(IClient newClient);

    /**
     * Get server info
     * 
     * @return - server info
     */
    IServerInfo getServerInfo();
    
    /**
     * Get client root from server info
     * 
     * @return - client root
     */
    String getServerInfoClientRoot();

    /**
     * Get client address from server info
     * 
     * @return - client address
     */
    String getServerInfoClientAddress();

    /**
     * Get client host from server info
     * 
     * @return - client host
     */
    String getServerInfoClientHost();

    /**
     * Get client name from server info
     * 
     * @return - client name
     */
    String getServerInfoClientName();

    /**
     * Create a new job
     * 
     * @param jobFields
     * @return - created job or null if create fails
     * @throws P4JavaException
     *             - if job creation fails
     */
    IP4Job createJob(Map<String, Object> jobFields) throws P4JavaException;

    /**
     * Create a branch
     * 
     * @param spec
     * @return - created branch
     * @throws P4JavaException
     *             - if branch creation fails
     */
    IP4Branch createBranch(IBranchSpec spec) throws P4JavaException;

    /**
     * Is the 'move' command supported by the server?
     * 
     * @return - true if move is supported, false otherwise
     */
    boolean isMoveSupported();

    /**
     * Is the 'move -k' command supported by the server?
     * 
     * @return - true if move -k is supported, false otherwise
     */
    boolean isMoveServerOnlySupported();

    /**
     * Is shelve/unshelve supported?
     * 
     * @return - true if supported, false otherwise
     */
    boolean isShelvingSupported();

    /**
     * Get current computed working directory
     * 
     * @return the currentDirectory
     */
    String getCurrentDirectory();

    /**
     * Gets the labels. If the size is zero then this method will return all the
     * labels.
     * 
     * @param size
     *            - number of labels to retrieve
     * @return - array of labels
     */
    IP4Label[] getLabels(int size);

    /**
     * Gets all the labels
     * 
     * @return - array of labels
     */
    IP4Label[] getLabels();

    /**
     * Gets all the labels for an array of paths
     * 
     * @param paths
     * @return - array of labels
     */
    IP4Label[] getLabels(String[] paths);

    /**
     * Gets all the labels for an array of paths
     * 
     * @param paths
     * @param size
     * @return - array of labels
     */
    IP4Label[] getLabels(String[] paths, int size);

    /**
     * Gets the labels that satisfy the paths and name filter
     * 
     * @param paths
     * @param size
     * @param nameFilter
     * @return - array of labels
     */
    IP4Label[] getLabels(String[] paths, int size, String nameFilter);

    /**
     * Gets the labels that satisfy the paths and name filter
     * 
     * @param user
     * @param paths
     * @param size
     * @param nameFilter
     * @return - array of labels
     */
    IP4Label[] getLabels(String user, String[] paths, int size,
            String nameFilter);

    /**
     * Gets all the labels for a path
     * 
     * @param path
     * @return - array of jobs
     */
    IP4Label[] getLabels(String path);

    /**
     * Gets all the labels for a path limiting to a specified size.
     * 
     * @param path
     * @param size
     * @return - array of labels
     */
    IP4Label[] getLabels(String path, int size);

    /**
     * Get the label with the specified name
     * 
     * @param labelName
     * @return - p4 label
     */
    IP4Label getLabel(String labelName);

    /**
     * Gets the branches. If the size is zero then this method will return all
     * the branches.
     * 
     * @param size
     *            - number of branches to retrieve
     * @return - array of branches
     */
    IP4Branch[] getBranches(int size);

    /**
     * Gets all the branches
     * 
     * @return - array of branches
     */
    IP4Branch[] getBranches();

    /**
     * Gets all the branches for a user limiting to a specified size and
     * matching the specified name.
     * 
     * @param user
     * @param size
     *            - number of branches to retrieve
     * @param nameFilter
     * @return - array of labels
     */
    IP4Branch[] getBranches(String user, int size, String nameFilter);

    /**
     * Gets all the branches for a user limiting to a specified size.
     * 
     * @param user
     * @param size
     * @return - array of branches
     */
    IP4Branch[] getBranches(String user, int size);

    /**
     * Get the branch with the specified name
     * 
     * @param branchName
     * @return - p4 branch
     */
    IP4Branch getBranch(String branchName);

    /**
     * Get the active pending changelist
     * 
     * @return - current pending changelist id
     */
    IP4PendingChangelist getActivePendingChangelist();

    /**
     * Set the active pending changelist by id
     * 
     * @param id
     */
    void setActivePendingChangelist(int id);

    /**
     * Get shelved changelists
     * 
     * @return - array of shelved changelists
     */
    IP4ShelvedChangelist[] getShelvedChangelists();

    /**
     * Gets the shelved changelists. If the size is zero then this method will
     * return all the shelved changelists.
     * 
     * @param size
     *            - number of changelists to retrieve
     * @return - array of shelved changelists
     */
    IP4ShelvedChangelist[] getShelvedChangelists(int size);

    /**
     * Gets all the shelved changelists for an array of paths
     * 
     * @param paths
     * @return - array of shelved changelists
     */
    IP4ShelvedChangelist[] getShelvedChangelists(String[] paths);

    /**
     * Gets the shelved changelists for an array of paths
     * 
     * @param paths
     * @param size
     * @return - array of shelved changelists
     */
    IP4ShelvedChangelist[] getShelvedChangelists(String[] paths, int size);

    /**
     * Gets the shelved changelists for an array of paths, user, and client
     * workspace limiting to a max.
     * 
     * @param paths
     * @param size
     * @param user
     * @param clientWorkspace
     * @return - array of shelved changelists
     */
    IP4ShelvedChangelist[] getShelvedChangelists(String[] paths, int size,
            String user, String clientWorkspace);

    /**
     * Gets all the shelved changelists for a path
     * 
     * @param path
     * @return - array of shelved changelists
     */
    IP4ShelvedChangelist[] getShelvedChangelists(String path);

    /**
     * Gets the shelved changelists for a path
     * 
     * @param path
     * @param size
     * @return - array of shelved changelists
     */
    IP4ShelvedChangelist[] getShelvedChangelists(String path, int size);

    /**
     * Get changelist by id, either pending or submitted
     * 
     * @param id
     * @return - p4 changelist or null if not found
     */
    IP4Changelist getChangelistById(int id);

    /**
     * Get changelist by id, either pending or submitted
     * 
     * @param id
     * @param type
     * @param checkClient
     * @param ignoreErrors
     * @return - p4 changelist or null if not found
     */
    IP4Changelist getChangelistById(int id, ChangelistStatus type,
            boolean checkClient, boolean ignoreErrors);

    /**
     * Get changelist by id, either pending or submitted
     * 
     * @param id
     * @param type
     * @param checkClient
     * @return - p4 changelist or null if not found
     */
    IP4Changelist getChangelistById(int id, ChangelistStatus type,
            boolean checkClient);

    /**
     * Get the spec depot for this connection
     * 
     * @return - spec depot or null if server doesn't have one
     */
    P4Depot getSpecDepot();

    /**
     * Get opened file manager
     * 
     * @return - opened file manager
     */
    PendingResourceManager getOpenedManager();

    /**
     * Get interchanges between source path and depot path
     * 
     * @param sourcePath
     * @param targetPath
     * @return - non-null but possibly empty array of submitted changelists
     */
    IP4SubmittedChangelist[] getInterchanges(String sourcePath,
            String targetPath);

    /**
     * Is depot search supported by this connection?
     * 
     * @return - true if supported, false otherwise
     */
    boolean isSearchSupported();

    /**
     * Search depot for specified pattern
     * 
     * @param pattern
     * @return non-null but possibly empty array of file line matches
     */
    IFileLineMatch[] searchDepot(String pattern);

    /**
     * Search depot for specified pattern at path
     * 
     * @param pattern
     * @param path
     * @return non-null but possibly empty array of file line matches
     */
    IFileLineMatch[] searchDepot(String pattern, String path);

    /**
     * Search depot for specified pattern at paths
     * 
     * @param pattern
     * @param paths
     * @return non-null but possibly empty array of file line matches
     */
    IFileLineMatch[] searchDepot(String pattern, String[] paths);

    /**
     * Search depot using specified options and path
     * 
     * @param pattern
     * @param options
     * @param path
     * 
     * @return non-null but possibly empty array of file line matches
     */
    IFileLineMatch[] searchDepot(String pattern, MatchingLinesOptions options,
            String path);

    /**
     * Search depot using specified options and paths
     * 
     * @param pattern
     * @param options
     * @param paths
     * @return non-null but possibly empty array of file line matches
     */
    IFileLineMatch[] searchDepot(String pattern, MatchingLinesOptions options,
            String[] paths);

    /**
     * Get diffs between two paths in the depot
     * 
     * @param path1
     * @param path2
     * @return non-null but possibly empty array of diffs
     */
    IFileDiff[] getDiffs(String path1, String path2);

    /**
     * Get history of specified file paths
     * 
     * @param paths
     * @param max
     * @param monitor
     * @return non-null but possibly empty array of file revisions
     */
    IP4Revision[] getHistory(String[] paths, int max);

    /**
     * Is the specified user name the owner of this connection?
     * 
     * @param user
     * @return true is user name is connection owner name, false otherwise
     */
    boolean isOwner(String user);

    /**
     * Get depot by name
     * 
     * @param name
     * @return depot or null if none found in current members
     */
    P4Depot getDepot(String name);

    /**
     * Get list of depots.
     * @return
     */
	List<IDepot> getDepots();

	/**
	 * Get streams from give paths with options.
	 * @param paths
	 * @param opts
	 * @return
	 */
	List<IStreamSummary> getStreams(List<String> paths, GetStreamsOptions opts);

    /**
     * Retrieve stream summary
     * @param stream
     * @return
     */
	IStreamSummary getStreamSummary(String stream);

	/**
	 * Return stream veiw mapping
	 * @param stream
	 * @return
	 */
	List<String> getStreamViewMapping(String stream);

	/**
	 * Populate the stream
	 * @param stream
	 * @return
	 */
	List<IFileSpec> populateStream(IStream stream);

	IP4Resource[] populate(String sourcePath, String targetPath, boolean preview,
			String description);
	
    void createStream(IStream stream);

    void deleteStream(String streamPath, StreamOptions opts);

    void reloadStream(String streamPath, ReloadOptions opts);
    
	void unloadStream(String streamPath, UnloadOptions opts);

    List<IClientSummary> getClients(final GetClientsOptions opts);

	void createClient(IClient iClient);

	void updateStream(IStream editStream);

	List<Map<String, Object>> execMapCmdList(String name, String[] strings,
			HashMap<String, Object> hashMap);

	void clearCache();

}
