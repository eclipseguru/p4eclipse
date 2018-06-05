/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;

import com.perforce.p4java.PropertyDefs;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IUser;
import com.perforce.p4java.core.IUserSummary;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.NoSuchObjectException;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.ResourceException;
import com.perforce.p4java.impl.generic.sys.ISystemFileCommandsHelper;
import com.perforce.p4java.impl.mapbased.rpc.RpcPropertyDefs;
import com.perforce.p4java.option.UsageOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;
import com.perforce.p4java.server.ServerStatus;
import com.perforce.p4java.server.callback.ILogCallback.LogTraceLevel;
import com.perforce.p4java.server.callback.ISSOCallback;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.P4JavaCallback;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.P4Event.EventType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class P4Workspace implements IEventObject, IErrorReporter {

    /**
     * LISTENER_ELEMENT
     */
    public static final String LISTENER_ELEMENT = "listener"; //$NON-NLS-1$

    /**
     * CONNECTION_LISTENER
     */
    public static final String CONNECTION_LISTENER = "connectionListener"; //$NON-NLS-1$

    /**
     * CONFIGURER_ELEMENT
     */
    public static final String CONFIGURER_ELEMENT = "configurer"; //$NON-NLS-1$

    /**
     * WORKSPACE_EXTENSION_POINT
     */
    public static final String WORKSPACE_EXTENSION_POINT = "com.perforce.team.core.workspace"; //$NON-NLS-1$

    /**
     * CLASS_ATTR
     */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    /**
     * CONNECTIONS
     */
    public static final String CONNECTIONS = "com.perforce.team.core.p4java.P4_CONNECTIONS"; //$NON-NLS-1$

    /**
     * VERSION_UNKNOWN
     */
    public static final String VERSION_UNKNOWN = "unknown"; //$NON-NLS-1$

    /**
     * PROG_NAME - used for -zprog
     */
    public static final String PROG_NAME = "p4eclipse"; //$NON-NLS-1$

    /**
     * BUNDLE_VERSION - used to set -zversion to current bundle version
     */
    public static final String BUNDLE_VERSION = "Bundle-Version"; //$NON-NLS-1$

    private static P4Workspace cache;

    private Map<ConnectionParameters, IP4Connection> connections;

    private IErrorHandler errorHandler = null;
    private ListenerList listeners;
    private ListenerList connectionListeners;
    private ISchedulingRule rule;
    private P4JavaCallback callbackHandler;
    private LogTraceLevel traceLevel = null;
    private Properties advancedProperties = new Properties();
    private boolean persistOffline = false;
    private String version = null;

    private ISystemFileCommandsHelper fileHelper = new P4JavaSysFileCommandsHelper();

    /**
     * This is the server properties object that bridges the launcher path
     */
    private final Properties serverProperties = new Properties() {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        /**
         * @see java.util.Properties#getProperty(java.lang.String,
         *      java.lang.String)
         */
        @Override
        public String getProperty(String key, String defaultValue) {
            String property = advancedProperties.getProperty(key);
            if (property == null) {
                property = super.getProperty(key, defaultValue);
            }
            return property;
        }

        /**
         * @see java.util.Properties#getProperty(java.lang.String)
         */
        @Override
        public String getProperty(String key) {
            String property = advancedProperties.getProperty(key);
            if (property == null) {
                property = super.getProperty(key);
            }
            return property;
        }

    };

    private P4Workspace() {
        connections = Collections
                .synchronizedMap(new HashMap<ConnectionParameters, IP4Connection>());
        listeners = new ListenerList();
        rule = P4Runner.createRule();
        connectionListeners = new ListenerList();
        callbackHandler = new P4JavaCallback();
//        callbackHandler.addProgressListener(new P4ProgressListener());
    }

	private void load() {
        loadServerProperties();
        loadConfigurer();
        // Set rpc helper, configurers can override default
        ServerFactory.setRpcFileSystemHelper(getFileHelper());
        loadConnections();
        loadProjectProperties();
        loadExtensionPointListeners();
    }

    private void loadConfigurer() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(WORKSPACE_EXTENSION_POINT);
        for (IConfigurationElement element : elements) {
            if (CONFIGURER_ELEMENT.equals(element.getName())) {
                String className = element.getAttribute(CLASS_ATTR);
                if (className != null) {
                    try {
                        Object createdListener = element
                                .createExecutableExtension(CLASS_ATTR);
                        if (createdListener instanceof IP4WorkspaceConfigurer) {
                            ((IP4WorkspaceConfigurer) createdListener)
                                    .configure(this);
                        }
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            }
        }
    }

    /**
     * Get Perforce style version string of core plugin bundle
     * 
     * @return non-null string
     */
    public String getVersion() {
        if (this.version == null) {
            String parsed = VERSION_UNKNOWN;
            try {
                Bundle bundle = PerforceProviderPlugin.getPlugin().getBundle();
                Object versionValue = bundle.getHeaders().get(BUNDLE_VERSION);
                if (versionValue != null) {
                    parsed = versionValue.toString();
                    int lastDot = parsed.lastIndexOf('.');
                    if (lastDot > -1 && lastDot + 1 < parsed.length()) {
                        parsed = parsed.substring(0, lastDot) + "/" //$NON-NLS-1$
                                + parsed.substring(lastDot + 1);
                    }

                }
            } catch (Exception e) {
                PerforceProviderPlugin.logError(e);
            }
            this.version = parsed;
        }
        return version;
    }

    private void loadServerProperties() {
        serverProperties.put(PropertyDefs.PROG_VERSION_KEY, getVersion());
        serverProperties.put(PropertyDefs.PROG_NAME_KEY, PROG_NAME);
        serverProperties.put(RpcPropertyDefs.RPC_RELAX_CMD_NAME_CHECKS_NICK,true);
    }

    private void loadProjectProperties() {
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
                .getProjects()) {
            getConnection(project);
        }
    }

    private void loadConnections() {
        // Can not use InstanceScope.INSTANCE on Eclipse 3.6.
        String s = InstanceScope.INSTANCE.getNode(PerforceProviderPlugin.ID).get(
                CONNECTIONS, IConstants.EMPTY_STRING);
        if (s != null && s.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(s, ":"); //$NON-NLS-1$
            while (tokenizer.hasMoreTokens()) {
                String conString = unescapeColons(tokenizer.nextToken());
                ConnectionParameters params = new ConnectionParameters(
                        conString);
                IP4Connection connection = getConnection(params);
                if (persistOffline) {
                    // Set initial offline state of connection based on
                    // serialized
                    // offline setting, fix for job026681
                    connection.setOffline(params.isOffline());
                }
            }
        }
    }

    private String unescapeColons(String s) {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\') {
                i++;
                if (s.charAt(i) == '\\') {
                    buff.append('\\');
                } else {
                    buff.append(':');
                }
            } else {
                buff.append(ch);
            }
        }
        return buff.toString();
    }

    private String escapeColons(String s) {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
            case ':':
                buff.append("\\c"); //$NON-NLS-1$
                break;
            case '\\':
                buff.append("\\\\"); //$NON-NLS-1$
                break;
            default:
                buff.append(ch);
                break;
            }
        }
        return buff.toString();
    }

    /**
     * Get scheduling rule used by {@link #asyncGetResource(IResource)}
     * 
     * @return - scheduling rule
     */
    public ISchedulingRule getRule() {
        return this.rule;
    }

    /**
     * Helper method to test if a server is reachable
     * 
     * @param serverAddress
     * @return - true if the server is reachable
     * @throws P4JavaException
     */
    public boolean canConnect(String serverAddress) throws P4JavaException {
        try {
            ConnectionParameters params = new ConnectionParameters();
            params.setPort(serverAddress);
            tryToConnect(params);
            return true;
        } catch (P4JavaError e) {
            PerforceProviderPlugin.logError(e);
        }
        return false;
    }

    public IP4Connection tryToConnect(ConnectionParameters params, IErrorHandler handler) throws P4JavaException {
        IP4Connection connection = new P4Connection(params);
        connection.setErrorHandler(handler);
        IServer server = connection.getServer();
        if (server != null) {
            if(server.getStatus() == ServerStatus.READY)
            	return connection;
            else
            	throw new P4JavaException(Messages.P4Workspace_ServerNotReady);
        }
        throw new P4JavaException(MessageFormat.format(Messages.P4Workspace_CannotReachServer,params.toString()));
	}

    public IP4Connection tryToConnect(ConnectionParameters params) throws P4JavaException {
        final P4JavaException[] exceptions = new P4JavaException[] { null };
        IErrorHandler handler = new IErrorHandler() {

            public boolean shouldRetry(IP4Connection connection,
                    P4JavaException exception) {
                exceptions[0] = exception;
                return false;
            }

            public void handleErrorSpecs(IFileSpec[] specs) {

            }
        };
        IP4Connection connection = new P4Connection(params);
        connection.setErrorHandler(handler);
        IServer server = connection.getServer();
        if (server != null) {
            if(server.getStatus() == ServerStatus.READY)
            	return connection;
            else
            	throw new P4JavaException(Messages.P4Workspace_ServerNotReady);
        }
        if (exceptions[0] != null) {
            throw exceptions[0];
        }
        throw new P4JavaException(MessageFormat.format(Messages.P4Workspace_CannotReachServer,params.toString()));
	}

    /**
     * Get the client spec for the specified client name
     * 
     * @param serverAddress
     * @param user
     * @param client
     * @return - client or null if client lookup fails
     * @throws P4JavaException
     */
    public IClient getClient(String serverAddress, String user, String client)
            throws P4JavaException {
        return getClient(serverAddress, user, client, null);
    }

    /**
     * Get the client spec for the specified client name
     * 
     * @param serverAddress
     * @param user
     * @param client
     * @param authTicket
     * @return - client or null if client lookup fails
     * @throws P4JavaException
     */
    public IClient getClient(String serverAddress, String user, String client,
            String authTicket) throws P4JavaException {
        IClient p4jClient = null;
        try {
            String serverUri = "p4java://" + serverAddress; //$NON-NLS-1$
            IServer server = createServer(serverUri);
            
            if (server != null) {
                server.setUserName(user);
                server.setAuthTicket(authTicket);
                server.connect();
                p4jClient = server.getClient(client);
            }
        } catch (URISyntaxException e) {
            PerforceProviderPlugin.logError(e);
        } catch (P4JavaError e) {
            PerforceProviderPlugin.logError(e);
        }
        return p4jClient;
    }

    /**
     * Get the raw p4java user objects
     * 
     * @param host
     * @param port
     * @param max
     * @param usernames
     * @return - array of p4j user objects
     * @throws P4JavaException
     */
    public IUserSummary[] getUsers(String host, int port, int max,
            List<String> usernames) throws P4JavaException {
        List<IUserSummary> users = new ArrayList<IUserSummary>();
        try {
            String serverUri = "p4java://" + host + ":" + port; //$NON-NLS-1$ //$NON-NLS-2$
            IServer server = createServer(serverUri);
            
            if (server != null) {
                server.connect();
                users = server.getUsers(usernames, max);
            }
        } catch (URISyntaxException e) {
            PerforceProviderPlugin.logError(e);
        } catch (P4JavaError e) {
            PerforceProviderPlugin.logError(e);
        }
        return users.toArray(new IUser[0]);
    }

    /**
     * Get the raw p4java user objects
     * 
     * @param host
     * @param port
     * @return - array of p4j user objects
     * @throws P4JavaException
     */
    public IUserSummary[] getUsers(String host, int port)
            throws P4JavaException {
        return getUsers(host, port, -1, null);
    }

    /**
     * Save the p4 cache connections
     */
    public void saveConnections() {
        StringBuilder s = new StringBuilder();
        // Save servers
        for (IP4Connection connection : this.connections.values()) {
            ConnectionParameters params = connection.getParameters();
            if (params != null) {
                // Update parameters with latest offline state from connection
                params.setOffline(connection.isOffline());
                s.append(escapeColons(params.toString()));
                s.append(':');
            }
        }
        if (s.length() > 0) {
            s.deleteCharAt(s.length() - 1);
        }

        // Can not use InstanceScope.INSTANCE on Eclipse 3.6.
        IEclipsePreferences node = InstanceScope.INSTANCE
                .getNode(PerforceProviderPlugin.ID);
        node.put(CONNECTIONS, s.toString());
        try {
            node.flush();
        } catch (BackingStoreException e) {
            PerforceProviderPlugin.logError(e);
        }
    }

    private void loadExtensionPointListeners() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(WORKSPACE_EXTENSION_POINT);
        for (IConfigurationElement element : elements) {
            if (LISTENER_ELEMENT.equals(element.getName())) {
                String className = element.getAttribute(CLASS_ATTR);
                if (className != null) {
                    try {
                        Object createdListener = element
                                .createExecutableExtension(CLASS_ATTR);
                        if (createdListener instanceof IP4Listener) {
                            addListener((IP4Listener) createdListener);
                        }
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            } else if (CONNECTION_LISTENER.equals(element.getName())) {
                String className = element.getAttribute(CLASS_ATTR);
                if (className != null) {
                    try {
                        Object createdListener = element
                                .createExecutableExtension(CLASS_ATTR);
                        if (createdListener instanceof IP4ConnectionListener) {
                            addConnectionListener((IP4ConnectionListener) createdListener);
                        }
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            }
        }
    }

    /**
     * Gets the callback instance
     * 
     * @return - p4j command callback
     */
    public P4JavaCallback getCallback() {
        return this.callbackHandler;
    }

    /**
     * Gets the sso callback instance
     * 
     * @return - p4j sso callback
     */
    ISSOCallback getSSOCallback() {
        return this.callbackHandler;
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#addListener(com.perforce.team.core.p4java.IP4Listener)
     */
    public void addListener(IP4Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#removeListener(com.perforce.team.core.p4java.IP4Listener)
     */
    public void removeListener(IP4Listener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#clearListeners()
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Add a connection listener to this workspace
     * 
     * @param listener
     */
    public void addConnectionListener(IP4ConnectionListener listener) {
        if (listener != null) {
            connectionListeners.add(listener);
        }
    }

    /**
     * Remove a connection listener from this workspace
     * 
     * @param listener
     */
    public void removeConnectionListener(IP4ConnectionListener listener) {
        if (listener != null) {
            connectionListeners.remove(listener);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#notifyListeners(com.perforce.team.core.p4java.P4Event)
     */
    public void notifyListeners(final P4Event event) {
    	Tracing.printTrace(getClass().getSimpleName()+":notifyListners()", event.toString());//$NON-NLS-1$
        for (final Object listener : listeners.getListeners()) {
        	Tracing.printExecTime(Policy.DEBUG, getClass().getSimpleName()+":notifyListeners()", MessageFormat.format("{0}",((IP4Listener)listener).getName()), new Runnable() {//$NON-NLS-1$,$NON-NLS-2$
        		public void run() {
        			try {
        				((IP4Listener) listener).resoureChanged(event);
        			} catch (Exception e) {
        				// Prevent exceptions from affecting other listeners
        				PerforceProviderPlugin.logError(e);
        			} catch (Error e) {
        				// Prevent errors from affecting other listeners
        				PerforceProviderPlugin.logError(e);
        			}
        		}
        	});
        }
    }

    private IP4Resource loadResource(IResource resource) {
        IP4Resource p4Resource = null;
        if (resource != null) {
            IProject project = resource.getProject();
            if (project != null) {
                IP4Connection connection = getConnection(project);
                if (connection != null && !connection.isOffline()) {
                    p4Resource = connection.getResource(resource);
                }
            }
        }
        return p4Resource;
    }

    private IP4Resource findResource(IResource resource)
            throws IllegalArgumentException {
        IP4Resource p4Resource = null;
        if (resource != null) {
            IProject project = resource.getProject();
            if (project != null) {
                PerforceTeamProvider provider = PerforceProviderPlugin
                        .getPerforceProviderFor(project);
                if (provider != null) {
                    try {
                        ConnectionParameters params = provider
                                .getProjectProperties(false);
                        IP4Connection connection = getConnection(params);
                        if (connection != null) {
                            String path = resource.getLocation().makeAbsolute()
                                    .toOSString();
                            p4Resource = connection.getResource(path);
                        }
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                } else {
                    throw new IllegalArgumentException(Messages.P4Workspace_NonPerforceProject);
                }
            }
        }
        return p4Resource;
    }

    /**
     * Asynchronously fetches a resource if not current in the cache. This
     * method will return null if the resource is not in the cache and will fire
     * an ADDED event when it is retrieved.
     * 
     * @param resource
     * @return - p4 resource if found, null if fetching
     */
    public IP4Resource asyncGetResource(final IResource resource) {
        IP4Resource p4Resource = null;
        boolean valid = true;
        try {
            p4Resource = findResource(resource);
        } catch (IllegalArgumentException e) {
            valid = false;
        }
        if (p4Resource == null && valid) {
            P4Runner.schedule(new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    loadResource(resource);
                }

            }, rule);
        }
        return p4Resource;
    }

    /**
     * Gets a resource from the cache
     * 
     * @param resource
     * @return - p4 resource
     */
    public IP4Resource getResource(IResource resource) {
        return loadResource(resource);
    }

    /**
     * Gets the current connection being used by the specified project
     * 
     * @param project
     * @return - p4 connection
     */
    public IP4Connection getConnection(IProject project) {
        IP4Connection connection = null;
        if (project != null) {
            PerforceTeamProvider provider = PerforceProviderPlugin
                    .getPerforceProviderFor(project);
            if (provider != null) {
                try {
                    ConnectionParameters params = provider
                            .getProjectProperties(false);
                    connection = getConnection(params);
                } catch (CoreException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
        return connection;
    }

    /**
     * Gets a connection from the cache or creates one if it isn't currently in
     * the cache
     * 
     * @param parameters
     * @return - new or cached connection
     */
    public IP4Connection getConnection(ConnectionParameters parameters) {
        boolean newConnection = false;
        IP4Connection connection = null;
        synchronized (this) {
            connection = this.connections.get(parameters);
            if (connection == null) {
                connection = new P4Connection(parameters);
                if (this.errorHandler != null) {
                    connection.setErrorHandler(this.errorHandler);
                }
                this.connections.put(parameters, connection);
                newConnection = true;
            }
        }
        if (connection != null && newConnection) {
            notifyListeners(new P4Event(EventType.ADDED, connection));
            final IP4Connection eventConnection = connection;
            SafeRunner.run(new ISafeRunnable() {

                public void run() throws Exception {
                    for (Object listener : connectionListeners.getListeners()) {
                        ((IP4ConnectionListener) listener)
                                .connectionAdded(eventConnection);
                    }
                }

                public void handleException(Throwable exception) {
                    PerforceProviderPlugin.logError(exception);
                }
            });

        }
        return connection;
    }

    /**
     * Does the workspace contain a connection?
     * 
     * @param parameters
     * @return - true if the connection exists for the parameters, false
     *         otherwise
     */
    public boolean containsConnection(ConnectionParameters parameters) {
        return parameters != null && this.connections.containsKey(parameters);
    }

    /**
     * Gets the connections in the cache
     * 
     * @return - array of cached connections
     */
    public IP4Connection[] getConnections() {
        return this.connections.values().toArray(new P4Connection[0]);
    }

    /**
     * Get number of connections in workspace
     * 
     * @return number of connections
     */
    public int size() {
        return this.connections.size();
    }

    /**
     * Removes a connection from the cache
     * 
     * @param parameters
     * @throws ConnectionMappedException
     *             - connection still mapped to projects
     */
    public void removeConnection(final ConnectionParameters parameters)
            throws ConnectionMappedException {
        if (parameters != null) {
            if (hasMappedProjects(parameters)) {
                throw new ConnectionMappedException(
                        getMappedProjects(parameters));
            } else {
                SafeRunner.run(new ISafeRunnable() {

                    public void run() throws Exception {
                        for (Object listener : connectionListeners
                                .getListeners()) {
                            ((IP4ConnectionListener) listener)
                                    .connectionRemovalRequested(parameters);
                        }
                    }

                    public void handleException(Throwable exception) {
                        PerforceProviderPlugin.logError(exception);
                    }
                });

                final IP4Connection removed = this.connections
                        .remove(parameters);
                if (removed != null) {
                    removed.dispose();
                    notifyListeners(new P4Event(EventType.REMOVED, removed));
                    SafeRunner.run(new ISafeRunnable() {

                        public void run() throws Exception {
                            for (Object listener : connectionListeners
                                    .getListeners()) {
                                ((IP4ConnectionListener) listener)
                                        .connectionRemoved(removed);
                            }
                        }

                        public void handleException(Throwable exception) {
                            PerforceProviderPlugin.logError(exception);
                        }
                    });
                }
            }
        }
    }

    /**
     * Removes a connection from the cache
     * 
     * @param connection
     * @throws ConnectionMappedException
     *             - connection still mapped to projects
     */
    public void removeConnection(IP4Connection connection)
            throws ConnectionMappedException {
        if (connection != null) {
            removeConnection(connection.getParameters());
        }
    }

    /**
     * Clears the cache
     */
    public void clear() {
        this.connections.clear();
    }

    /**
     * Gets the connection cache
     * 
     * @return - connection cache instance
     */
    public static synchronized P4Workspace getWorkspace() {
        if (cache == null) {
            cache = new P4Workspace();
            cache.load();
        }
        return cache;
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#addListeners(com.perforce.team.core.p4java.IP4Listener[])
     */
    public void addListeners(IP4Listener[] listeners) {
        if (listeners != null) {
            for (IP4Listener listener : listeners) {
                addListener(listener);
            }
        }
    }

    /**
     * Adds a command listener
     * 
     * @param listener
     */
    public void addCommandListener(IP4CommandListener listener) {
        if (listener != null) {
            this.callbackHandler.addCommandListener(listener);
        }
    }

    /**
     * Removes a command listener
     * 
     * @param listener
     */
    public void removeCommandListener(IP4CommandListener listener) {
        if (listener != null) {
            this.callbackHandler.removeCommandListener(listener);
        }
    }

    /**
     * Gets the server properties to use
     * 
     * @return - server properties
     */
    public Properties getServerProperties() {
        return this.serverProperties;
    }

    /**
     * Does the specified connection have any mapped projects?
     * 
     * @param connection
     * @return - true if projects are map to the specified connection
     */
    public boolean hasMappedProjects(IP4Connection connection) {
        boolean mapped = false;
        if (connection != null) {
            mapped = connection.hasMappedProjects();
        }
        return mapped;
    }

    /**
     * Get the mapped projects for the specified connection parameters
     * 
     * @param parameters
     * @return - non-null array of project
     */
    public IProject[] getMappedProjects(ConnectionParameters parameters) {
        IProject[] projects = new IProject[0];
        if (parameters != null) {
            IP4Connection connection = this.connections.get(parameters);
            projects = getMappedProjects(connection);
        }
        return projects;
    }

    /**
     * Get the mapped projects for the specified connection
     * 
     * @param connection
     * @return - non-null array of project
     */
    public IProject[] getMappedProjects(IP4Connection connection) {
        IProject[] projects = new IProject[0];
        if (connection != null) {
            projects = connection.getMappedProjects();
        }
        return projects;
    }

    /**
     * Does the specified connection parameters have any mapped projects?
     * 
     * @param parameters
     * @return - true if projects are map to the specified connection parameters
     */
    public boolean hasMappedProjects(ConnectionParameters parameters) {
        boolean mapped = false;
        if (parameters != null) {
            IP4Connection connection = this.connections.get(parameters);
            mapped = hasMappedProjects(connection);
        }
        return mapped;
    }

    /**
     * Edit the connection. This will updated the settings of any project
     * current mapped to this connection
     * 
     * @param connection
     * @param newParameters
     * @return - new connection
     */
    public synchronized IP4Connection editConnection(IP4Connection connection,
            ConnectionParameters newParameters) {
        IP4Connection newConnection = null;
        if (connection != null && newParameters != null
                && !newParameters.equals(connection.getParameters())) {
            IP4Connection oldConnection = connection;

            IErrorHandler oldHandler = oldConnection.getErrorHandler();

            // Get previously mapped projects
            IProject[] projects = connection.getMappedProjects();

            // Create connection for new params p4 connection
            newConnection = getConnection(newParameters);
            connection = newConnection;

            // Update error handler from previous connection
            if (oldHandler != null) {
                connection.setErrorHandler(oldHandler);
            }

            // Update project persistent properties that are mapped to the old
            // connection settings
            for (IProject project : projects) {
                PerforceTeamProvider provider = PerforceTeamProvider
                        .getPerforceProvider(project);
                if (provider != null) {
                    provider.setProjectProperties(connection.getParameters());
                }
            }

            final ConnectionParameters oldParams = oldConnection
                    .getParameters();
            if (oldParams != null && newConnection != null) {
                final IP4Connection eventConnection = connection;
                SafeRunner.run(new ISafeRunnable() {

                    public void run() throws Exception {
                        for (Object listener : connectionListeners
                                .getListeners()) {
                            ((IP4ConnectionListener) listener)
                                    .connectionChanged(eventConnection,
                                            oldParams);
                        }
                    }

                    public void handleException(Throwable exception) {
                        PerforceProviderPlugin.logError(exception);
                    }
                });

            }

            // Remove old connection
            try {
                removeConnection(oldConnection);
            } catch (ConnectionMappedException cme) {
                // Should never happen since projects were updated
                PerforceProviderPlugin.logError(cme);
            }

        }
        return newConnection;
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#getErrorHandler()
     */
    public IErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#handleError(com.perforce.p4java.exception.P4JavaException)
     */
    public boolean handleError(P4JavaException exception) {
        // P4 Workspace does not support handling p4j exceptions directly
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#handleErrors(com.perforce.p4java.core.file.IFileSpec[])
     */
    public void handleErrors(IFileSpec[] specs) {
        // P4 Workspace does not support handling error specs directly
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#setErrorHandler(com.perforce.team.core.p4java.IErrorHandler)
     */
    public void setErrorHandler(IErrorHandler handler) {
        this.errorHandler = handler;
    }

    /**
     * @return the traceLevel
     */
    public LogTraceLevel getTraceLevel() {
        return traceLevel;
    }

    /**
     * @param traceLevel
     *            the traceLevel to set
     */
    public void setTraceLevel(LogTraceLevel traceLevel) {
        this.traceLevel = traceLevel;
    }

    /**
     * Get the advanced properties that override any properties set via
     * modifying {@link #getServerProperties()}
     * 
     * @return - advanced properties
     */
    public Properties getAdvancedProperties() {
        return this.advancedProperties;
    }

    /**
     * @return the fileHelper
     */
    public ISystemFileCommandsHelper getFileHelper() {
        return fileHelper;
    }

    /**
     * @param fileHelper
     *            the fileHelper to set
     */
    public void setFileHelper(ISystemFileCommandsHelper fileHelper) {
        this.fileHelper = fileHelper;
    }

    /**
     * @return the persistOffline
     */
    public boolean isPersistOffline() {
        return persistOffline;
    }

    /**
     * @param persistOffline
     *            the persistOffline to set
     */
    public void setPersistOffline(boolean persistOffline) {
        this.persistOffline = persistOffline;
    }

    static public boolean addTrust(String serverIpPort, String fingerprint) throws P4JavaException {
        String serverUri = "p4javassl://" + serverIpPort; //$NON-NLS-1$
        IOptionsServer server;
        try {
            server = ServerFactory.getOptionsServer(serverUri, null);
            String result;
                result = server.addTrust(fingerprint);
                return result.contains("Added trust for Perforce server"); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

	public static IServer createServer(String serverUri) throws ConnectionException, NoSuchObjectException, ConfigException, ResourceException, URISyntaxException {
        Properties props = P4Workspace
                .getWorkspace().getServerProperties();
        return createServer(serverUri,props);
	}
	
	public static IServer createServer(String serverUri, Properties props) throws ConnectionException, NoSuchObjectException, ConfigException, ResourceException, URISyntaxException{
		props.put("enableProgress", "true"); // enable progress reporting
		
		UsageOptions opts=new UsageOptions(props);
        opts.setHostName(getP4HOST());

        IServer newServer = ServerFactory.getOptionsServer(serverUri, props, opts);
		return newServer;
	}
	
	public static String getP4HOST(){
        Properties props = P4Workspace
                .getWorkspace().getServerProperties();
        String hostname=props.getProperty("P4HOST");
        if(StringUtils.isEmpty(hostname)){
        	hostname=System.getenv("P4HOST");
        }
        return hostname;
	}
	
}
