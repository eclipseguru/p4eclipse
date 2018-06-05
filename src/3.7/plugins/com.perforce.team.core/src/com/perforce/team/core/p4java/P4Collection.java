/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.p4java.option.client.RevertFilesOptions;
import com.perforce.p4java.option.client.SyncOptions;
import com.perforce.p4java.server.CmdSpec;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceConnectionFactory;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.Tracing.IRunnable;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;

/**
 * Represents a loose collection of p4 resources. This class should be used to
 * run a single command against a group of p4 resources that are selected by the
 * user. If the resources added span multiple clients then the action methods
 * will be executed once per client.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Collection extends P4Resource implements IP4Container {

    /**
     * Edit default changelist description
     */
    public static final String EDIT_DEFAULT_DESCRIPTION = Messages.P4Collection_0;

    /**
     * Add default changelist description
     */
    public static final String ADD_DEFAULT_DESCRIPTION = Messages.P4Collection_1;

    /**
     * Delete default changelist description
     */
    public static final String DELETE_DEFAULT_DESCRIPTION = Messages.P4Collection_2;

    private Type type = Type.REMOTE;
    private Set<IP4Resource> resources = new HashSet<IP4Resource>();

    /**
     * Creates a collection of resources from the connection and list of valid
     * files specs specified
     * 
     * @param connection
     * @param specs
     * @return - collection
     */
    public static P4Collection createCollection(IP4Connection connection,
            List<IFileSpec> specs) {
        P4Collection collection = null;
        if (connection != null && specs != null) {
            collection = getValidCollection(connection, specs,
                    IP4Resource.Type.REMOTE);
        } else {
            collection = new P4Collection();
        }
        return collection;
    }

    /**
     * Creates an empty collection
     */
    public P4Collection() {

    }

    private void runOperation(IP4Connection connection,
            IP4ClientOperation operation) {
        if (connection != null && operation != null) {
            boolean retry = true;
            IClient client = connection.getClient();
            while (retry && client != null) {
                retry = false;
                try {
                    operation.run(client);
                } catch (P4JavaException e) {
                    retry = handleError(connection, e);
                    if (retry) {
                        client = connection.getClient();
                    }
                    PerforceProviderPlugin.logError(e);
                } catch (P4JavaError e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
    }

    private void runOperation(IP4Connection connection,
            IP4ServerOperation operation) {
        if (connection != null && operation != null) {
            boolean retry = true;
            IServer server = connection.getServer();
            while (retry && server != null) {
                retry = false;
                try {
                    operation.run(server);
                } catch (P4JavaException e) {
                    retry = handleError(connection, e);
                    if (retry) {
                        server = connection.getServer();
                    }
                    PerforceProviderPlugin.logError(e);
                } catch (P4JavaError e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
    }

    /**
     * Creates a collection from an array of files
     * 
     * @param resources
     */
    public P4Collection(IP4Resource[] resources) {
        if (resources != null) {
            for (IP4Resource resource : resources) {
                add(resource);
            }
        }
    }

    /**
     * Creates a p4 collection from a standard collection
     * 
     * @param resources
     */
    public P4Collection(Collection<IP4Resource> resources) {
        if (resources != null) {
            for (IP4Resource resource : resources) {
                add(resource);
            }
        }
    }

    /**
     * Creates a p4 collection from a specified p4 collection
     * 
     * @param collection
     */
    public P4Collection(P4Collection collection) {
        if (collection != null) {
            addAll(collection);
        }
    }

    /**
     * Add all the resources in the specified collection
     * 
     * @param collection
     */
    public void addAll(P4Collection collection) {
        if (collection != null) {
            this.resources.addAll(collection.resources);
        }
    }

    /**
     * Convert collection to array
     * 
     * @param <T>
     * @param array
     * @return - array containing elements in collection
     */
    public <T> T[] toArray(T[] array) {
        return this.resources.toArray(array);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#size()
     */
    public int size() {
        return this.resources.size();
    }

    /**
     * Reopens the files in this collection in the specified changelist
     * 
     * @param changelist
     */
    public void reopen(final IP4PendingChangelist changelist) {
        if (changelist != null) {
            IClient client = changelist.getClient();
            if (client != null && changelist.getId() > -1) {
                final List<String> paths = new ArrayList<String>();
                for (IP4Resource resource : this.members()) {
                    String path = resource.getActionPath(type);
                    if (path != null) {
                        paths.add(path);
                    }
                }
                if (paths.size() > 0) {
                    final List<IFileSpec> specList = P4FileSpecBuilder
                            .makeFileSpecList(paths.toArray(new String[0]));
                    IP4ClientOperation operation = new P4ClientOperation() {

                        public void run(IClient client) throws P4JavaException,
                                P4JavaError {
                            List<IFileSpec> reopened = client.reopenFiles(
                                    specList, changelist.getId(), null);
                            P4Collection reopenedResources = getValidOpenCollection(
                                    changelist.getConnection(), reopened);
                            if (reopenedResources.members().length > 0) {
                                changelist.getConnection().updateRevertedFiles(
                                        reopenedResources);
                                updateChangelist(changelist, reopenedResources);
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.REVERTED,
                                                reopenedResources));
                                reopenedResources.refresh();
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.OPENED,
                                                reopenedResources));
                            }
                            handleErrors(reopened.toArray(new IFileSpec[0]));
                        }
                    };
                    runOperation(changelist.getConnection(), operation);
                }
            }
        }
    }

    /**
     * Locks the files in this collection
     */
    public void lock() {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection=entry.getKey();
            List<String> clientResources = entry.getValue();
            IClient client = connection.getClient();
            if (client != null && !clientResources.isEmpty()) {
                final List<IFileSpec> specList = P4FileSpecBuilder
                        .makeFileSpecList(clientResources
                                .toArray(new String[0]));
                IP4ClientOperation operation = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {
                        List<IFileSpec> locked = client.lockFiles(specList,
                                IChangelist.UNKNOWN);
                        P4Collection lockedResources = getValidCollection(
                                connection, locked, getType());
                        if (!lockedResources.isEmpty()) {
                            lockedResources.refresh();
                            P4Workspace.getWorkspace().notifyListeners(
                                    new P4Event(EventType.LOCKED,
                                            lockedResources));
                        }
                        handleErrors(locked.toArray(new IFileSpec[0]));
                    }
                };
                runOperation(connection, operation);
            }
        }
    }

    /**
     * Unlocks the files in this collection
     */
    public void unlock() {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection=entry.getKey();
            List<String> clientResources = entry.getValue();
            IClient client = connection.getClient();
            if (client != null && !clientResources.isEmpty()) {
                final List<IFileSpec> specList = P4FileSpecBuilder
                        .makeFileSpecList(clientResources
                                .toArray(new String[0]));
                IP4ClientOperation operation = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {
                        List<IFileSpec> unlocked = client.unlockFiles(specList,
                                IChangelist.UNKNOWN, false);
                        P4Collection unlockedResources = getValidCollection(
                                connection, unlocked, getType());
                        if (!unlockedResources.isEmpty()) {
                            unlockedResources.refresh();
                            P4Workspace.getWorkspace().notifyListeners(
                                    new P4Event(EventType.UNLOCKED,
                                            unlockedResources));
                        }
                        handleErrors(unlocked.toArray(new IFileSpec[0]));
                    }
                };
                runOperation(connection, operation);
            }
        }
    }

    /**
     * Fixes the jobs in this collection with the specified changelist
     * 
     * @param changelist
     */
    public void fix(IP4Changelist changelist) {
        if (changelist != null) {
            List<IP4Job> jobs = new ArrayList<IP4Job>();
            for (IP4Resource resource : members()) {
                if (resource instanceof IP4Job) {
                    jobs.add((IP4Job) resource);
                }
            }
            if (jobs.size() > 0) {
                IP4Job[] fixed = changelist.fix(jobs.toArray(new IP4Job[0]));
                if (fixed.length > 0) {
                    P4Workspace.getWorkspace().notifyListeners(
                            new P4Event(EventType.FIXED,
                                    new P4Collection(fixed)));
                }
            }
        }
    }

    /**
     * Un-fixes the jobs in this collection with the specified changelist
     * 
     * @param changelist
     */
    public void unfix(IP4Changelist changelist) {
        if (changelist != null) {
            List<IP4Job> jobs = new ArrayList<IP4Job>();
            for (IP4Resource resource : members()) {
                if (resource instanceof IP4Job) {
                    jobs.add((IP4Job) resource);
                }
            }
            if (jobs.size() > 0) {
                IP4Job[] unfixed = changelist
                        .unfix(jobs.toArray(new IP4Job[0]));
                if (unfixed.length > 0) {
                    P4Workspace.getWorkspace().notifyListeners(
                            new P4Event(EventType.UNFIXED, new P4Collection(
                                    unfixed)));
                }
            }
        }
    }

    private boolean handleError(IP4Connection connection,
            P4JavaException exception) {
        boolean retry = false;
        if (connection != null && exception != null) {
            IErrorHandler handler = connection.getErrorHandler();
            if (handler != null) {
                retry = handler.shouldRetry(connection, exception);
            }
        }
        return retry;
    }

    /**
     * Resolves this collection
     * 
     * @param type
     * @param preview
     * @return - array of resources that were resolved
     */
    public IP4Resource[] resolve(final ResolveFilesAutoOptions options) {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        final List<IP4Resource> resolvedFiles = new ArrayList<IP4Resource>();
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection=entry.getKey();
            IClient client = connection.getClient();
            if (client != null) {
                List<String> clientResources = entry.getValue();

                final List<IFileSpec> resolveSpecs = P4FileSpecBuilder
                        .makeFileSpecList(clientResources
                                .toArray(new String[0]));

                IP4ClientOperation operation = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {

                        List<IFileSpec> resolved = client.resolveFilesAuto(
                                resolveSpecs,
                                options);
                        P4Collection resolvedResources = null;
                        if (options.isShowActionsOnly()) {
                            resolvedResources = getValidCollection(connection,
                                    resolved, getType(), true);
                        } else {
                            resolved = client
                                    .resolvedFiles(resolveSpecs, false);
                            resolvedResources = getValidCollection(connection,
                                    resolved, getType(), true);
                            if (!resolvedResources.isEmpty()) {
                                resolvedResources.refresh();
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.RESOLVED,
                                                resolvedResources));
                            }
                        }
                        handleErrors(resolveSpecs.toArray(new IFileSpec[0]));
                        resolvedFiles.addAll(Arrays.asList(resolvedResources
                                .members()));
                    }
                };
                runOperation(connection, operation);
            }
        }
        return resolvedFiles.toArray(new IP4Resource[0]);
    }

    /**
     * Resolve the files in this collection with the contents of the input
     * stream. This method is generally used for collections with one object
     * since it is uncommon to resolve multiple files with the same merge file
     * specified as an input stream.
     * 
     * If useTextual merge is true, binary files will be resolved as text.
     * 
     * If startFromRev and endFromRev are not -1, resolve will be restricted
     * to that range of source revisions.
     * 
     * @param stream
     * @param useTextualMerge
     * @param startFromRev
     * @param endFromRev
     * @return - array of resolved resources
     */
    public IP4Resource[] resolve(final InputStream stream, final boolean useTextualMerge,
            final int startFromRev, final int endFromRev) {
        final List<IFileSpec> resolvedSpecs = new ArrayList<IFileSpec>();
        final P4Collection resolvedResources = new P4Collection();
        for (IP4Resource resource : members()) {
            if (resource instanceof IP4File) {
                final IP4File file = (IP4File) resource;
                IP4ClientOperation operation = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {
                        String path = file.getActionPath(getType());
                        if (client != null && path != null) {
                            IFileSpec resolveSpec = new FileSpec(path);
                            IFileSpec resolvedSpec = client.resolveFile(
                                    resolveSpec, stream, useTextualMerge, startFromRev, endFromRev);
                            if (resolvedSpec != null) {
                                resolvedSpecs.add(resolvedSpec);
                                if (FileSpecOpStatus.VALID == resolvedSpec
                                        .getOpStatus()) {
                                    resolvedResources.add(file);
                                }
                            }
                        }
                    }
                };
                runOperation(file.getConnection(), operation);
            }
        }
        if (!resolvedResources.isEmpty()) {
            resolvedResources.refresh();
            resolvedResources.refreshLocalResources(IResource.DEPTH_INFINITE);
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.RESOLVED, resolvedResources));
        }
        handleErrors(resolvedSpecs.toArray(new IFileSpec[0]));
        return resolvedResources.members();
    }

    /**
     * Adds a file to this collection
     * 
     * @param file
     */
    public void add(IP4Resource file) {
        if (file != null && !file.isReadOnly()) {
            this.resources.add(file);
        }
    }

    /**
     * Adds the resources in the specified collection to this collection
     * 
     * @param collection
     */
    public void add(P4Collection collection) {
        if (collection != null) {
            for (IP4Resource resource : collection.members()) {
                add(resource);
            }
        }
    }

    /**
     * Does this collection contain the specified file?
     * 
     * @param file
     * @return - true if contained in this collection, false otherwise
     */
    public boolean contains(IP4Resource file) {
        if (file != null && this.resources != null) {
            return this.resources.contains(file);
        }
        return false;
    }

    /**
     * Generates a client spec mapping where {@link IClient} objects map to
     * lists of {@link IP4Resource}
     * 
     * @param items
     * @return - map of clients to p4 resource lists
     */
    protected Map<IClient, List<IP4Resource>> generateClientMap(
            List<IP4Resource> items) {
        Map<IClient, List<IP4Resource>> mappedClients = new HashMap<IClient, List<IP4Resource>>();
        for (IP4Resource resource : items) {
            IClient client = resource.getClient();
            if (client != null) {
                List<IP4Resource> clientResources = null;
                if (mappedClients.containsKey(client)) {
                    clientResources = mappedClients.get(client);
                } else {
                    clientResources = new ArrayList<IP4Resource>();
                    mappedClients.put(client, clientResources);
                }
                clientResources.add(resource);
            }
        }
        return mappedClients;
    }

    /**
     * Gets the valid specs as p4 resources put into a p4 collection
     * 
     * @param connection
     * @param specs
     * @param type
     * @param updateIntegSpec
     * @return - p4 collection
     */
    public static P4Collection getValidCollection(IP4Connection connection,
            List<? extends IFileSpec> specs, IP4Resource.Type type,
            boolean updateIntegSpec) {
        P4Collection collection = new P4Collection();
        collection.setType(type);
        List<IP4Resource> resources=new ArrayList<IP4Resource>();

        if (specs != null) {
            Set<String> paths = new HashSet<String>();
            for (IFileSpec spec : specs) {
                if (FileSpecOpStatus.VALID == spec.getOpStatus()) {
                    String path = spec.getDepotPathString();
                    if (path == null) {
                        path = P4Resource.normalizeLocalPath(spec);
                    }
                    if (path != null) {
                        IP4Resource resource = connection.getResource(path);
                        if (resource == null) {
                            // Add if not found, this is the case for an open
                            // for add
                            resource = new P4File(spec, connection);
                            if(connection.updateResourceThenCheckNotify(resource)){
                            	resources.add(resource); // no notify, batch them
                            }
                        }
                        if (updateIntegSpec && resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            List<IFileSpec> list = new ArrayList<IFileSpec>();
                            // if this is the first occurrence of this path, ignore previous contents
                            // otherwise, append this spec to the new list for this path
                            if (paths.contains(path))
                                if (file.getIntegrationSpecs() != null)
                                list.addAll(Arrays.asList(file
                                        .getIntegrationSpecs()));
                            list.add(spec);
                            file.setIntegrationSpecs(list
                                    .toArray(new IFileSpec[0]));
                            paths.add(path);
                        }
                        collection.add(resource);
                    }
                }
            }
        }
        if(resources.size()>0){// batch notifying
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.ADDED, resources.toArray(new IP4Resource[0])));
        }
        return collection;
    }

    /**
     * Gets the valid specs as p4 resources put into a p4 collection
     * 
     * @param connection
     * @param specs
     * @param type
     * @return - p4 collection
     */
    public static P4Collection getValidCollection(IP4Connection connection,
            List<? extends IFileSpec> specs, IP4Resource.Type type) {
        return getValidCollection(connection, specs, type, false);
    }

    private P4Collection getValidOpenCollection(IP4Connection connection,
            List<? extends IFileSpec> specs) {
        P4Collection collection = new P4Collection();
        List<IP4Resource> resources=new ArrayList<IP4Resource>();

        if (specs != null) {
            for (IFileSpec spec : specs) {
                if (spec.getChangelistId() > -1
                        && FileSpecOpStatus.VALID == spec.getOpStatus()) {
                    String path = spec.getDepotPathString();
                    if (path == null) {
                        path = P4Resource.normalizeLocalPath(spec);
                    }
                    if (path != null) {
                        IP4Resource resource = connection.getResource(path);
                        if (resource == null) {
                            // Add if not found, this is the case for an open
                            // for add
                            resource = new P4File(spec, connection);
                            if(connection.updateResourceThenCheckNotify(resource)){
                            	resources.add(resource); // no notify, batch them
                            }
                        }
                        collection.add(resource);
                    }
                }
            }
        }

        if(resources.size()>0){// batch notifying
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.ADDED, resources.toArray(new IP4Resource[0])));
        }

        return collection;
    }

    private P4Collection getValidSyncedCollection(IP4Connection connection,
            List<? extends IFileSpec> specs) {
        P4Collection collection = new P4Collection();
        List<IP4Resource> resources=new ArrayList<IP4Resource>();
        
        if (specs != null) {
            for (IFileSpec spec : specs) {
                if (FileSpecOpStatus.VALID == spec.getOpStatus()) {
                    String path = spec.getDepotPathString();
                    if (path == null) {
                        path = P4Resource.normalizeLocalPath(spec);
                    }
                    if (path != null) {
                        IP4Resource resource = connection.getResource(path);
                        if (resource == null) {
                            // Add if not found, this is the case for an open
                            // for add
                            resource = new P4File(spec, connection);
                            if(connection.updateResourceThenCheckNotify(resource)){
                            	resources.add(resource); // no notify, batch them
                            }
                        }
                        collection.add(resource);
                    }
                } else if (FileSpecOpStatus.INFO == spec.getOpStatus()) {
                    // This case is because a sync that causes a resolve comes
                    // back as an INFO spec and the path must be parsed from the
                    // message field
                    String message = spec.getStatusMessage();
                    if (message != null) {
                        int index = message.indexOf(" - must resolve"); //$NON-NLS-1$
                        if (index > -1) {
                            String path = message.substring(0, index);
                            if (path.length() > 0) {
                                IP4Resource resource = connection
                                        .getResource(path);
                                if (resource == null) {
                                    // Add if not found, this is the case for an
                                    // open for add
                                    resource = new P4File(spec, connection);
                                    if(connection.updateResourceThenCheckNotify(resource)){
                                    	resources.add(resource); // no notify, batch them
                                    }
                                }
                                collection.add(resource);
                            }
                        }
                    }
                }
            }
        }
        if(resources.size()>0){// batch notifying
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.ADDED, resources.toArray(new IP4Resource[0])));
        }
        return collection;
    }

    /**
     * Generates a client spec mapping where {@link IClient} objects map to
     * lists of {@link String} that are the action path of {@link IP4Resource}
     * 
     * @param items
     * @return - map of clients to action path lists
     */
    protected Map<IClient, List<String>> generateClientSpecMapping(
            Set<IP4Resource> items) {
        return generateClientSpecMapping(items, true);
    }

    /**
     * Generates a client spec mapping where {@link IClient} objects map to
     * lists of {@link String} that are the action path of {@link IP4Resource}
     * 
     * @param items
     * @param formatted
     * @return - map of clients to action path lists
     */
    protected Map<IClient, List<String>> generateClientSpecMapping(
            Set<IP4Resource> items, boolean formatted) {
        Map<IClient, List<String>> mappedClients = new HashMap<IClient, List<String>>();
        for (IP4Resource resource : items) {
            IClient client = resource.getClient();
            if (client != null) {
                List<String> clientResources = null;
                if (mappedClients.containsKey(client)) {
                    clientResources = mappedClients.get(client);
                } else {
                    clientResources = new ArrayList<String>();
                    mappedClients.put(client, clientResources);
                }
                String actionPath = resource.getActionPath(type);
                if (actionPath != null) {
                    if (formatted) {
                        actionPath = PerforceConnectionFactory
                                .formatFilename(actionPath);
                    }
                    clientResources.add(actionPath);
                }
            }
        }
        return mappedClients;
    }

    /**
     * Generated a mapping of p4 connections to string action paths
     * 
     * @param items
     * @return - map of connection to string action paths
     */
    protected Map<IP4Connection, List<String>> generateConnectionSpecMapping(
            Collection<IP4Resource> items) {
        return generateConnectionSpecMapping(items, true);
    }

    /**
     * Generated a mapping of p4 connections to string action paths
     * 
     * @param items
     * @param formatted
     * @return - map of connection to string action paths
     */
    protected Map<IP4Connection, List<String>> generateConnectionSpecMapping(
            Collection<IP4Resource> items, boolean formatted) {
        Map<IP4Connection, List<String>> mappedClients = new HashMap<IP4Connection, List<String>>();
        for (IP4Resource resource : items) {
            IP4Connection connection = resource.getConnection();
            if (connection != null) {
                List<String> clientResources = null;
                if (mappedClients.containsKey(connection)) {
                    clientResources = mappedClients.get(connection);
                } else {
                    clientResources = new ArrayList<String>();
                    mappedClients.put(connection, clientResources);
                }
                String actionPath = resource.getActionPath(type);
                if (actionPath != null && !"".equals(actionPath)) { //$NON-NLS-1$
                    if (formatted) {
                        actionPath = PerforceConnectionFactory
                                .formatFilename(actionPath);
                    }
                    clientResources.add(actionPath);
                }
            }
        }
        return mappedClients;
    }

    /**
     * Generated a mapping of p4 connections to p4 resources
     * 
     * @return - map of connections to resources
     */
    public Map<IP4Connection, List<IP4Resource>> toResourceMap() {
        Map<IP4Connection, List<IP4Resource>> mappedClients = new HashMap<IP4Connection, List<IP4Resource>>();
        for (IP4Resource resource : members()) {
            IP4Connection connection = resource.getConnection();
            if (connection != null) {
                List<IP4Resource> resources = null;
                if (mappedClients.containsKey(connection)) {
                    resources = mappedClients.get(connection);
                } else {
                    resources = new ArrayList<IP4Resource>();
                    mappedClients.put(connection, resources);
                }
                resources.add(resource);
            }
        }
        return mappedClients;
    }

    /**
     * Generated a mapping of p4 connections to p4 resources
     * 
     * @return - map of connections to resources
     */
    public Map<IP4Connection, List<IP4File>> toFileMap() {
        Map<IP4Connection, List<IP4File>> mappedClients = new HashMap<IP4Connection, List<IP4File>>();
        for (IP4Resource resource : members()) {
            if (resource instanceof IP4File) {
                IP4Connection connection = resource.getConnection();
                if (connection != null) {
                    List<IP4File> resources = null;
                    if (mappedClients.containsKey(connection)) {
                        resources = mappedClients.get(connection);
                    } else {
                        resources = new ArrayList<IP4File>();
                        mappedClients.put(connection, resources);
                    }
                    resources.add((IP4File) resource);
                }
            }
        }
        return mappedClients;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#members()
     */
    public IP4Resource[] members() {
        return resources.toArray(new IP4Resource[0]);
    }

    private void loadChildResource(IP4Container container,
            Set<IP4Resource> resources) {
        IP4Resource[] members = container.members();
        for (IP4Resource member : members) {
            if (member instanceof IP4Container) {
                loadChildResource((IP4Container) member, resources);
            }
            resources.add(member);
        }
    }

    /**
     * Gets direct members and sub-members at all depths for items in this
     * collection that are {@link IP4Container} objects
     * 
     * @return - array of p4 resources
     */
    public IP4Resource[] allMembers() {
        Set<IP4Resource> all = new HashSet<IP4Resource>();
        for (IP4Resource resource : resources) {
            if (resource instanceof IP4Container) {
                loadChildResource((IP4Container) resource, all);
            }
            all.add(resource);
        }
        return all.toArray(new IP4Resource[0]);
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
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getConnection()
     */
    public IP4Connection getConnection() {
        return null;
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
        return null;
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

    private void updateChangelist(IP4PendingChangelist list,
            P4Collection editedFiles) {
        if (list != null) {
            for (IP4Resource resource : editedFiles.members()) {
                if (resource instanceof IP4File) {
                    list.addFile((IP4File) resource);
                }
            }
        }
    }

    private int createChangelist(IP4Connection connection, String description) {
        int id = IChangelist.UNKNOWN;
        IP4PendingChangelist newList = connection.createChangelist(description,
                null);
        if (newList != null) {
            id = newList.getId();
        }
        return id;
    }

    /**
     * Add this collection the specified pending changelist's model
     * 
     * @param list
     */
    public void addToChangelistModel(IP4PendingChangelist list) {
        if (list != null) {
            for (IP4Resource resource : members()) {
                if (resource instanceof IP4File) {
                    list.addFile((IP4File) resource);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#add(int)
     */
    @Override
    public void add(int changelist) {
        addToChangelist(changelist);
    }

    /**
     * Opens for add the files in this collection
     * 
     * @param changelist
     * @return - collection of opened for add files
     */
    public P4Collection addToChangelist(int changelist) {
        return addToChangelist(changelist, false);
    }

    /**
     * Opens for add the files in this collection
     * 
     * @param changelist
     * @param setActive
     *            - set changelist as active pending changelist for connection
     * @return - collection of opened for add files
     */
    public P4Collection addToChangelist(int changelist, boolean setActive) {
        return addToChangelist(changelist, null, setActive);
    }

    /**
     * Opens for add the files in this collection
     * 
     * @param changelist
     * @param description
     * @param setActive
     *            - set changelist as active pending changelist for connection
     * @return - collection of opened for add files
     */
    public P4Collection addToChangelist(int changelist, String description,
            boolean setActive) {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(
                this.resources, false);
        final P4Collection allResources = new P4Collection();
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection = entry.getKey();
            List<String> clientResources = entry.getValue();
            IClient client = connection.getClient();
            if (client != null && !clientResources.isEmpty()) {

                if (changelist == IP4PendingChangelist.NEW) {
                    if (description == null) {
                        description = ADD_DEFAULT_DESCRIPTION;
                    }
                    changelist = createChangelist(connection, description);
                }

                if (changelist >= 0) {

                    if (setActive) {
                        connection.setActivePendingChangelist(changelist);
                    }

                    final int listId = changelist;
                    final List<IFileSpec> specList = P4FileSpecBuilder
                            .makeFileSpecList(clientResources
                                    .toArray(new String[0]));

                    IP4ClientOperation operation = new P4ClientOperation() {

                        public void run(IClient client) throws P4JavaException,
                                P4JavaError {
                            List<IFileSpec> added = client.addFiles(specList,
                                    false, listId, null, true);
                            P4Collection addedResources = getValidCollection(
                                    connection, added, getType());
                            if (!addedResources.isEmpty()) {
                                addedResources.refresh();
                                updateChangelist(
                                        connection.getPendingChangelist(listId),
                                        addedResources);
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.OPENED,
                                                addedResources));
                                allResources.addAll(addedResources);
                            }
                            handleErrors(added.toArray(new IFileSpec[0]));
                        }
                    };
                    runOperation(connection, operation);
                }
            }
        }
        return allResources;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#delete(int)
     */
    @Override
    public void delete(int changelist) {
        delete(changelist, false);
    }

    /**
     * Open files for delete and optionally set active changelist on connection
     * 
     * @see #delete(int)
     * @param changelist
     * @param setActive
     */
    public void delete(int changelist, boolean setActive) {
        delete(changelist, null, setActive);
    }

    /**
     * Open files for delete and optionally set active changelist on connection
     * 
     * @see #delete(int)
     * @param changelist
     * @param description
     * @param setActive
     */
    public void delete(int changelist, String description, boolean setActive) {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        for (Map.Entry<IP4Connection, List<String>> entry: items.entrySet()) {
        	final IP4Connection connection = entry.getKey();
        	List<String> clientResources = entry.getValue();
            IClient client = connection.getClient();
            if (client != null && clientResources.size() > 0) {

                if (changelist == IP4PendingChangelist.NEW) {
                    if (description == null) {
                        description = DELETE_DEFAULT_DESCRIPTION;
                    }
                    changelist = createChangelist(connection, description);
                }

                if (changelist >= 0) {

                    if (setActive) {
                        connection.setActivePendingChangelist(changelist);
                    }

                    final int listId = changelist;
                    final List<IFileSpec> specList = P4FileSpecBuilder
                            .makeFileSpecList(clientResources
                                    .toArray(new String[0]));

                    IP4ClientOperation operation = new P4ClientOperation() {

                        public void run(IClient client) throws P4JavaException,
                                P4JavaError {
                            List<IFileSpec> deleted = client.deleteFiles(
                                    specList, listId, false);
                            P4Collection deletedResources = getValidCollection(
                                    connection, deleted, getType());
                            if (!deletedResources.isEmpty()) {
                                deletedResources.refresh();
                                updateChangelist(
                                        connection.getPendingChangelist(listId),
                                        deletedResources);
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.OPENED,
                                                deletedResources));
                            }
                            handleErrors(deleted.toArray(new IFileSpec[0]));
                        }
                    };
                    runOperation(connection, operation);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#edit(int)
     */
    @Override
    public void edit(int changelist) {
        edit(changelist, false);
    }

    /**
     * Open the files for edit and optionally set active changelist on
     * connection
     * 
     * @see #edit(int)
     * @param changelist
     * @param setActive
     */
    public void edit(int changelist, boolean setActive) {
        edit(changelist, null, setActive);
    }

    /**
     * Revert (-k) file and then edit and optionally set active changelist on
     * connection
     * 
     * @see #edit(int)
     * @param changelist
     * @param description
     * @param setActive
     */
    public void revertThenEdit(int changelist, String description, boolean setActive) {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection=entry.getKey();        			
            List<String> clientResources = entry.getValue();
            IClient client = connection.getClient();
            if (client != null && clientResources.size() > 0) {

                if (changelist == IP4PendingChangelist.NEW) {
                    if (description == null) {
                        description = EDIT_DEFAULT_DESCRIPTION;
                    }
                    changelist = createChangelist(connection, description);
                }

                if (changelist >= 0) {

                    if (setActive) {
                        connection.setActivePendingChangelist(changelist);
                    }

                    final List<IFileSpec> specList = P4FileSpecBuilder
                            .makeFileSpecList(clientResources
                                    .toArray(new String[0]));
                    final int listId = changelist;
                    IP4ClientOperation operation = new P4ClientOperation() {

                        public void run(IClient client) throws P4JavaException,
                                P4JavaError {
                            List<IFileSpec> edited = client.revertFiles(specList,new RevertFilesOptions().setNoClientRefresh(true));
                            edited = client.editFiles(specList,
                                    false, false, listId, null);
                            P4Collection editedCollection = getValidCollection(
                                    connection, edited, getType());
                            if (!editedCollection.isEmpty()) {
                                editedCollection.refresh();
                                updateChangelist(
                                        connection.getPendingChangelist(listId),
                                        editedCollection);
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.OPENED,
                                                editedCollection));
                            }
                            handleErrors(edited.toArray(new IFileSpec[0]));
                        }
                    };
                    runOperation(connection, operation);
                }
            }
        }
    }

    /**
     * Open the files for edit and optionally set active changelist on
     * connection
     * 
     * @see #edit(int)
     * @param changelist
     * @param description
     * @param setActive
     */
    public void edit(int changelist, String description, boolean setActive) {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection=entry.getKey();
        	List<String> clientResources = entry.getValue();
            IClient client = connection.getClient();
            if (client != null && clientResources.size() > 0) {

                if (changelist == IP4PendingChangelist.NEW) {
                    if (description == null) {
                        description = EDIT_DEFAULT_DESCRIPTION;
                    }
                    changelist = createChangelist(connection, description);
                }

                if (changelist >= 0) {

                    if (setActive) {
                        connection.setActivePendingChangelist(changelist);
                    }

                    final List<IFileSpec> specList = P4FileSpecBuilder
                            .makeFileSpecList(clientResources
                                    .toArray(new String[0]));
                    final int listId = changelist;
                    IP4ClientOperation operation = new P4ClientOperation() {

                        public void run(IClient client) throws P4JavaException,
                                P4JavaError {
                            List<IFileSpec> edited = client.editFiles(specList,
                                    false, false, listId, null);
                            P4Collection editedCollection = getValidCollection(
                                    connection, edited, getType());
                            if (!editedCollection.isEmpty()) {
                                editedCollection.refresh();
                                updateChangelist(
                                        connection.getPendingChangelist(listId),
                                        editedCollection);
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.OPENED,
                                                editedCollection));
                            }
                            handleErrors(edited.toArray(new IFileSpec[0]));
                        }
                    };
                    runOperation(connection, operation);
                }
            }
        }
    }

    /**
     * Get a p4 collection of all the files in the collection that would be
     * reverted due to the fact that they are unchanged.
     * 
     * @return - collection of unchanged files that would have been reverted
     */
    public P4Collection previewUnchangedRevert() {
        return revert(true, true);
    }

    /**
     * Get a p4 collection of all the files in the collection that would be
     * reverted
     * 
     * @param preview
     * @param unchangedOnly
     * @return - collection of files returned from the revert command
     */
    public P4Collection revert(final boolean preview,
            final boolean unchangedOnly) {
        final P4Collection collection = new P4Collection();
        for (IP4Resource resource : this.resources) {
            if (resource instanceof IP4PendingChangelist) {
                final IP4PendingChangelist list = (IP4PendingChangelist) resource;
                final IP4Connection connection = list.getConnection();
                IClient client = list.getClient();
                if (connection != null && client != null) {
                    IP4ClientOperation operation = new P4ClientOperation() {

                        public void run(final IClient client) throws P4JavaException,
                                P4JavaError {
                            final List<IFileSpec> specs = new ArrayList<IFileSpec>();
                            String name = client.getName();
                            if (name != null) {
                                name = "//" + name + "/..."; //$NON-NLS-1$ //$NON-NLS-2$
                                specs.add(new FileSpec(name));
                            }
                            final List<IFileSpec> reverted=new ArrayList<IFileSpec>();
                            try {
                            	Tracing.printExecTime2(Policy.DEBUG_TIME, "revert", "list-"+list.getId(),new IRunnable() { //$NON-NLS-1$ //$NON-NLS-2$
                            		public void run() throws Throwable {
                            			List<IFileSpec> rlist = client.revertFiles(
                            					specs, preview, list.getId(),
                            					unchangedOnly, false);
                            			reverted.addAll(rlist);
                            		}
                            	}); 
							} catch (P4JavaException e) {
								throw e;
							} catch (P4JavaError e){
								throw e;
							} catch (Throwable e) {
								e.printStackTrace();
							}

//                            List<IFileSpec> reverted = client.revertFiles(
//                                    specs, preview, list.getId(),
//                                    unchangedOnly, false);
                            P4Collection revertedCollection = getValidCollection(
                                    connection, reverted, getType());
                            if (!preview && !revertedCollection.isEmpty()) {
                                revertedCollection.refresh();
                                // Remove reverted files from pending
                                // changelists
                                connection
                                        .updateRevertedFiles(revertedCollection);
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.REVERTED,
                                                revertedCollection));
                            }
                            collection.add(revertedCollection);
                            handleErrors(reverted.toArray(new IFileSpec[0]));
                        }
                    };
                    runOperation(connection, operation);
                }
            }
        }
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection=entry.getKey();
            IClient client = connection.getClient();
            if (client != null) {
                final List<String> clientResources = entry.getValue();
                if (clientResources.size() > 0) {
                    final List<IFileSpec> specList = P4FileSpecBuilder
                            .makeFileSpecList(clientResources
                                    .toArray(new String[0]));
                    IP4ClientOperation operation = new P4ClientOperation() {

                        public void run(IClient client) throws P4JavaException,
                                P4JavaError {
                            List<IFileSpec> reverted = client
                                    .revertFiles(specList, preview, -1,
                                            unchangedOnly, false);
                            P4Collection revertedCollection = getValidCollection(
                                    connection, reverted, getType());
                            if (!preview && !revertedCollection.isEmpty()) {
                                revertedCollection.refresh();
                                // Remove reverted files from pending
                                // changelists
                                connection
                                        .updateRevertedFiles(revertedCollection);
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.REVERTED,
                                                revertedCollection));
                            }
                            collection.add(revertedCollection);
                            handleErrors(reverted.toArray(new IFileSpec[0]));
                        }
                    };
                    runOperation(connection, operation);
                }
            }
        }
        return collection;
    }

    /**
     * Get a p4 collection of files that would be reverted in this collection.
     * 
     * @return - collection of files that would be reverted
     */
    public P4Collection previewRevert() {
        return revert(true, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#revert()
     */
    @Override
    public void revert() {
        revert(false, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#sync()
     */
    @Override
    public void sync(IProgressMonitor monitor, IP4ProgressListener callback) {
        sync(false, false, null, monitor, callback);
    }

    /**
     * Previews a sync for the collection
     * @param monitor
     * @return - list of file specs
     */
    public List<IFileSpec> previewSync(IProgressMonitor monitor) {
        return sync(false, true, null, monitor, null);
    }

    /**
     * Forces a sync of the collection
     * @param monitor
     */
    public void forceSync(IProgressMonitor monitor) {
        sync(true, false, null, monitor, null);
    }

    /**
     * Syncs the collection to a specific revision
     * 
     * @param revision
     * @param monitor
     */
    public void sync(String revision, IProgressMonitor monitor) {
        sync(false, false, revision, monitor, null);
    }

    /**
     * Gets the file in this collection that are unresolved.
     * 
     * @return - array of unresolved files
     */
    public IP4Resource[] getUnresolved() {
        return resolve(new ResolveFilesAutoOptions().setShowActionsOnly(true).setShowBase(true));
    }

    /**
     * Syncs the collection. This is a long time slow operation.
     * 
     * @param force
     * @param preview
     * @param revision
     * @param monitor
     * @param callback progress monitor
     * @return - list of synced specs
     */
    public List<IFileSpec> sync(final boolean force, final boolean preview,
            String revision, final IProgressMonitor monitor, final IP4ProgressListener callback) {
    	monitor.beginTask("sync", 200);// $NON-NLS-1$ //$NON-NLS-1$
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        final List<IFileSpec> specs = new ArrayList<IFileSpec>();
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection=entry.getKey();
        	List<String> clientResources = entry.getValue();
            IClient client = connection.getClient();
            if (client != null) {
                if (revision != null) {
                    List<String> revisioned = new ArrayList<String>();
                    if (!clientResources.isEmpty()) {
                        for (String path : clientResources) {
                            revisioned.add(path + revision);
                        }
                    } else {
                        revisioned.add(revision);
                    }
                    clientResources = revisioned;
                }
                if (!clientResources.isEmpty()) {
                    final List<IFileSpec> specList = P4FileSpecBuilder
                            .makeFileSpecList(clientResources
                                    .toArray(new String[0]));
                    IP4ClientOperation operation = new P4ClientOperation() {
    
                        public void run(IClient client) throws P4JavaException,
                                P4JavaError {
                        	List<String> oplist=new ArrayList<String>();
    
                        	// Previously, we use -q is to avoid to many out put to
                        	// pollute the console. But then use may loose the ablility
                        	// to preview. So we remove the -q option.
                        	// int serverVer = connection.getServer().getServerVersionNumber();
                        	// if(serverVer>=IP4ServerConstants.PROGRESS_SERVERID_VERSION){
                        	// 	oplist.add("-q"); //$NON-NLS-1$
                        	// }
    
                        	if(force)
                        		oplist.add("-f"); //$NON-NLS-1$
                        	if(preview)
                        		oplist.add("-n"); //$NON-NLS-1$
                        	SyncOptions opt=new SyncOptions(oplist.toArray(new String[0]));
                        	int key = P4CoreUtils.getRandomInt();
                        	List<IFileSpec> syncSpecs=null;
                        	if(callback!=null){
                        		callback.setConnection(connection);
                        		client.sync(specList,opt, callback, key);
                        		syncSpecs = callback.getFileSpecs();
                        	}else{
                        		P4ProgressListener cb= P4CoreUtils.createStreamCallback(connection, CmdSpec.SYNC, new SubProgressMonitor(monitor, 100));
                        		client.sync(specList, opt, cb, key);
                        		syncSpecs = cb.getFileSpecs();
                        	}
    
                            specs.addAll(syncSpecs);
                            P4Collection syncedResources = getValidSyncedCollection(
                                    connection, syncSpecs);
                            
                            monitor.setTaskName(Messages.P4Collection_RefreshResourceAfterSync);
                            monitor.worked(100);
                            if (!preview && !syncedResources.isEmpty()) {
                                syncedResources.refresh();
                                // Refresh local resources after sync
                                syncedResources.refreshLocalResources(IResource.DEPTH_INFINITE);
                            }
                            handleErrors(syncSpecs.toArray(new IFileSpec[0]));
                        }
                    };
                    runOperation(connection, operation);
                }
            }
        }
        return specs;
    }

    /**
     * Changes the type of the files in this collection
     * 
     * @param newType
     */
    public void changeType(final String newType) {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        for (Map.Entry<IP4Connection, List<String>> entry: items.entrySet()) {
        	final IP4Connection connection = entry.getKey();
            List<String> clientResources = entry.getValue();
            IClient client = connection.getClient();
            if (client != null && !clientResources.isEmpty()) {
                final List<IFileSpec> specList = P4FileSpecBuilder
                        .makeFileSpecList(clientResources
                                .toArray(new String[0]));
                IP4ClientOperation operation = new P4ClientOperation() {

                    public void run(IClient client) throws P4JavaException,
                            P4JavaError {
                        List<IFileSpec> changed = client.reopenFiles(specList,
                                -1, newType);
                        P4Collection changedCollection = getValidCollection(
                                connection, changed, getType());
                        if (!changedCollection.isEmpty()) {
                            changedCollection.refresh();
                        }
                    }
                };
                runOperation(connection, operation);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isContainer()
     */
    public boolean isContainer() {
        return true;
    }

    /**
     * Is this collection empty
     * 
     * @return - true if empty, false otherwise
     */
    public boolean isEmpty() {
        return resources.isEmpty();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {
        refresh(IResource.DEPTH_ONE);
    }

    /**
     * Refresh the state validation for any text file buffers in this selection.
     */
    public void resetStateValidation() {
        ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        if (manager == null) {
            return;
        }
        for (IP4Resource resource : members()) {
            if (resource instanceof IP4File) {
                IFile[] files = ((IP4File) resource).getLocalFiles();
                for (IFile file : files) {
                    IPath location = file.getLocation();
                    if (location != null) {
                        IFileBuffer buffer = manager.getFileBuffer(location,LocationKind.NORMALIZE);
                        if (buffer != null) {
                            buffer.resetStateValidation();
                        }
                    }
                }
            }
        }
    }

    /**
     * Refreshes the local resource that are associated with the p4 resources in
     * this collection
     * 
     * @param depth
     */
    public void refreshLocalResources(int depth) {
        for (IP4Resource resource : members()) {
            if (resource instanceof IP4Folder) {
                IContainer[] containers = ((IP4Folder) resource)
                        .getLocalContainers();
                for (IContainer container : containers) {
                    if (container != null) {
                        try {
                            container.refreshLocal(depth, null);
                        } catch (CoreException e) {
                            PerforceProviderPlugin.logError(e);
                        }
                    }
                }
            } else if (resource instanceof IP4File) {
                IFile[] files = ((IP4File) resource).getLocalFiles();
                for (IFile file : files) {
                    try {
                        file.refreshLocal(depth, null);
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            }
        }
    }

    /**
     * Tag the resources in this collection with the specified label
     * 
     * @param label
     */
    public void tag(String label) {
        tag(label, null, false, false);
    }

    /**
     * Tag the resources in this collection with the specified label
     * 
     * @param label
     * @param revision
     */
    public void tag(String label, String revision) {
        tag(label, revision, false, false);
    }

    /**
     * Tag the resources in this collection with the specified label
     * 
     * @param label
     * @param revision
     * @param delete
     * @param preview
     */
    public void tag(final String label, String revision, final boolean delete,
            final boolean preview) {
        if (label == null) {
            return;
        }
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	IP4Connection connection=entry.getKey();
            IServer server = connection.getServer();
            if (server != null) {
                List<String> clientResources = entry.getValue();
                if (revision != null) {
                    List<String> revisioned = new ArrayList<String>();
                    for (String path : clientResources) {
                        revisioned.add(path + revision);
                    }
                    clientResources = revisioned;
                }
                final List<IFileSpec> specList = P4FileSpecBuilder
                        .makeFileSpecList(clientResources
                                .toArray(new String[0]));
                IP4ServerOperation operation = new P4ServerOperation() {

                    public void run(IServer server) throws P4JavaException,
                            P4JavaError {
                        List<IFileSpec> specs = server.tagFiles(specList,
                                label, preview, delete);
                        handleErrors(specs.toArray(new IFileSpec[0]));
                    }
                };
                runOperation(connection, operation);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#markForRefresh()
     */
    @Override
    public void markForRefresh() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#needsRefresh()
     */
    @Override
    public boolean needsRefresh() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh(int)
     */
    public void refresh(int depth) {
        if (resources.size() > 0) {
            final Set<IP4Resource> files = new HashSet<IP4Resource>();
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4File) {
                    files.add(resource);
                } else if (resource instanceof IP4Container) {
                    IP4Connection connection = resource.getConnection();
                    if (connection != resource) {
                        IP4File[] found = connection.findFiles(resource
                                .getActionPath(type));
                        files.addAll(Arrays.asList(found));
                    }
                }
            }
            final P4Collection refreshCollection = new P4Collection(files);
            Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
            for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
            	final IP4Connection connection=entry.getKey();
                IServer server = connection.getServer();
                if (server != null) {
                    List<String> clientResources = entry.getValue();
                    if (!clientResources.isEmpty()) {
                        final List<IFileSpec> specList = P4FileSpecBuilder
                                .makeFileSpecList(clientResources
                                        .toArray(new String[clientResources
                                                .size()]));
                        IP4ClientOperation operation = new P4ClientOperation() {

                            public void run(IClient client)
                                    throws P4JavaException, P4JavaError {
                                IServer server = client.getServer();
                                if (server != null) {
                                    List<IExtendedFileSpec> specs = client
                                            .getServer().getExtendedFiles(
                                                    specList, 0, -1, -1, null,
                                                    null);
                                    
                                    List<IP4File> validFiles = new ArrayList<IP4File>();
                                    List<IExtendedFileSpec> validSpecs = new ArrayList<IExtendedFileSpec>();
                                    
                                    for (IExtendedFileSpec spec : specs) {
                                        if (FileSpecOpStatus.VALID == spec
                                                .getOpStatus()) {
                                            IP4File file = connection
                                                    .getFile(spec);
                                            if (file != null) {
                                                files.remove(file);
                                                validFiles.add(file);
                                                validSpecs.add(spec);
                                            }
                                        }
                                    }
                                    if(!validFiles.isEmpty()){
                                    	batchMoveFilesToSubmittedChangelist(validFiles, validSpecs, refreshCollection);
                                    }
                                    
                                    handleErrors(specs
                                            .toArray(new IFileSpec[specs.size()]));
                                }
                            }

                        };
                        runOperation(connection, operation);
                    }
                }
            }
            // Any files left over are ones that had a non-valid file spec
            // returned from the fstat
            // TODO: This need to do in a batch mode to improve the performance.
//            for (IP4Resource file : files) {
//                ((IP4File) file).setFileSpec(null);
//            }
            batchRemoveFilesFromChangelist(files);

            if (!refreshCollection.isEmpty()) {
                P4Workspace.getWorkspace().notifyListeners(
                        new P4Event(EventType.REFRESHED, refreshCollection));
            }
        }
    }

    protected void batchMoveFilesToSubmittedChangelist(List<IP4File> files, List<IExtendedFileSpec> specs, P4Collection refreshCollection) {
		Map<IP4Connection, Map<Integer, List<IP4Resource>>> removeMap=new HashMap<IP4Connection, Map<Integer,List<IP4Resource>>>();
		Map<IP4Connection, Map<Integer, List<IP4Resource>>> addMap=new HashMap<IP4Connection, Map<Integer,List<IP4Resource>>>();

		for(int i=0;i<files.size();i++){
			IP4File file = files.get(i);
			IExtendedFileSpec spec=specs.get(i);
	        int previousChangelist = file.getChangelistId();
	        file.setFileSpec(spec,
	                refreshCollection,false);
	        int newChangelist = file.getChangelistId();
	        
			IP4Connection connection = file.getConnection();
			if (connection != null) {
				if (previousChangelist > -1 && previousChangelist != newChangelist) {

					// construct remove map
					Map<Integer, List<IP4Resource>> submap = removeMap.get(connection);
					if(submap==null){
						submap=new HashMap<Integer, List<IP4Resource>>();
						removeMap.put(connection, submap);
					}
					
					List<IP4Resource> flist = submap.get(previousChangelist);
					if(flist==null){
						flist=new ArrayList<IP4Resource>();
						submap.put(previousChangelist, flist);
					}
					flist.add(file);
					
				}
				
				if (newChangelist > -1 && newChangelist!=previousChangelist) {
					// construct add map
					Map<Integer, List<IP4Resource>> submap = addMap.get(connection);
					if(submap==null){
						submap=new HashMap<Integer, List<IP4Resource>>();
						addMap.put(connection, submap);
					}
					
					List<IP4Resource> flist = submap.get(newChangelist);
					if(flist==null){
						flist=new ArrayList<IP4Resource>();
						submap.put(newChangelist, flist);
					}
					flist.add(file);

				}
			}
		}
		
		// remove files from changelist and batch update the old change list
		for(Map.Entry<IP4Connection, Map<Integer, List<IP4Resource>>> outerEntry: removeMap.entrySet()){
			IP4Connection connection=outerEntry.getKey();
			Map<Integer, List<IP4Resource>> submap = outerEntry.getValue();
			for(Map.Entry<Integer, List<IP4Resource>> entry: submap.entrySet()){
				int change = entry.getKey();
				List<IP4Resource> flist = entry.getValue();
				IP4PendingChangelist oldList = connection.getPendingChangelist(
						change, false, true);
				if(oldList!=null){
					for(IP4Resource file: flist)
						oldList.removeFile((IP4File) file);
	
					// auto refresh oldList
	                if (refreshCollection != null) {
	                	refreshCollection.add(oldList);
	                } else {
	                    P4Workspace.getWorkspace().notifyListeners(
	                            new P4Event(EventType.REFRESHED, oldList));
	                }
				}
			}
		}

		// add files to new changelist and batch update the new change list
		for(Map.Entry<IP4Connection, Map<Integer, List<IP4Resource>>> outerEntry: addMap.entrySet()){
			IP4Connection connection=outerEntry.getKey();
			Map<Integer, List<IP4Resource>> submap = outerEntry.getValue();
			for(Map.Entry<Integer, List<IP4Resource>> entry: submap.entrySet()){
				int change=entry.getKey();
				List<IP4Resource> flist = entry.getValue();
				for(IP4Resource file:flist){
					IP4PendingChangelist newList = connection.getPendingChangelist(
							change, ((IP4File)file).isOpened(), true);
					if (newList != null) {
						newList.addFile((IP4File)file);
					}
				}
			}
		}
		
	}

	/*
     * Remove files from changelist. This better is called on reverting large set of files. 
     * @param files files originally in some changelists and to be removed
     */
    private void batchRemoveFilesFromChangelist(Set<IP4Resource> files) {
		Map<IP4Connection, Map<Integer, List<IP4Resource>>> removeMap=new HashMap<IP4Connection, Map<Integer,List<IP4Resource>>>();
		
		// now batch update the changelist.
		
		// update file spec, and categorized the changes per changelist
		for(IP4Resource f: files){
			IP4File file=(IP4File) f;
	        int previousChangelist = file.getChangelistId();
	        file.setFileSpec(null,null,false); // update file spec without notifying listeners 

			IP4Connection connection = file.getConnection();
			if (connection != null) {
				if (previousChangelist > -1) {

					Map<Integer, List<IP4Resource>> submap = removeMap.get(connection);
					if(submap==null){
						submap=new HashMap<Integer, List<IP4Resource>>();
						removeMap.put(connection, submap);
					}
					
					List<IP4Resource> flist = submap.get(previousChangelist);
					if(flist==null){
						flist=new ArrayList<IP4Resource>();
						submap.put(previousChangelist, flist);
					}
					flist.add(file);
				}
			}
		}

		// remove files from changelist and batch update the old change list
		for(Map.Entry<IP4Connection, Map<Integer, List<IP4Resource>>> outerEntry: removeMap.entrySet()){
			IP4Connection connection=outerEntry.getKey();
			Map<Integer, List<IP4Resource>> submap = outerEntry.getValue();
			for(Map.Entry<Integer, List<IP4Resource>> entry: submap.entrySet()){
				int change=entry.getKey();
				List<IP4Resource> flist = entry.getValue();
				IP4PendingChangelist oldList = connection.getPendingChangelist(
						change, false, true);
				if(oldList!=null){
					for(IP4Resource file: flist)
						oldList.removeFile((IP4File) file);

					// auto refresh oldList
					P4Workspace.getWorkspace().notifyListeners(
							new P4Event(EventType.REFRESHED, oldList));
				}

			}
		}
    }

	/**
     * Sets the local files in this collection to the read only state specified
     * 
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly) {
        if (resources.size() > 0) {
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4File) {
                    IFile[] files = ((IP4File) resource).getLocalFiles();
                    for (IFile file : files) {
                        ResourceAttributes attrs = file.getResourceAttributes();
                        if (attrs != null) {
                            attrs.setReadOnly(readOnly);
                            try {
                                file.setResourceAttributes(attrs);
                            } catch (CoreException e) {
                                PerforceProviderPlugin.logError(e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @see #getAllLocalFiles()
     * 
     * @param monitor
     * @return - all local files
     */
    public IP4File[] getAllLocalFiles(IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        List<IP4File> locals = new ArrayList<IP4File>();
        List<IP4File> containerFiles = new ArrayList<IP4File>();
        List<IP4File> files = new ArrayList<IP4File>();

        for (IP4Resource resource : resources) {
            String path = resource.getActionPath(getType());
            if (path != null) {
                monitor.setTaskName(MessageFormat.format(
                        Messages.P4Collection_7, path));
            }
            if (resource instanceof IP4Container) {
                List<IP4File> filesFromContainer = Arrays
                        .asList(((IP4Container) resource).getAllLocalFiles());
                containerFiles.addAll(filesFromContainer);
                locals.addAll(filesFromContainer);
            } else if (resource instanceof IP4File) {
                files.add((IP4File) resource);
                locals.add((IP4File) resource);
            }
            monitor.worked(1);
        }

        // Refresh any files found in the current collection
        if (!files.isEmpty()) {
            P4Collection filesCollections = new P4Collection(
                    files.toArray(new IP4File[0]));
            filesCollections.setType(getType());
            filesCollections.refresh();
        }

        // Send refresh event for files found from IP4Container.getAllLocalFiles
        P4Collection filesFound = new P4Collection(
                containerFiles.toArray(new IP4File[0]));
        filesFound.setType(getType());
        P4Workspace.getWorkspace().notifyListeners(
                new P4Event(EventType.REFRESHED, filesFound));

        return locals.toArray(new IP4File[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#getAllLocalFiles()
     */
    public IP4File[] getAllLocalFiles() {
        return getAllLocalFiles(null);
    }

    /**
     * @return the type
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    public List<IFileSpec> pull() {
        Map<IP4Connection, List<String>> items = generateConnectionSpecMapping(this.resources);
        final List<IFileSpec> specs = new ArrayList<IFileSpec>();
        for (Map.Entry<IP4Connection, List<String>> entry : items.entrySet()) {
        	final IP4Connection connection=entry.getKey();
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    List<IFileSpec> pullSpecs = P4SandBoxUtils.pull(connection);

                    specs.addAll(pullSpecs);
                    P4Collection pulledResources = getValidSyncedCollection(
                            connection, pullSpecs);
                    pulledResources.refresh();
                    handleErrors(pullSpecs.toArray(new IFileSpec[0]));
                }
            };
            runOperation(connection, operation);
        }
        return specs;
    }

}
