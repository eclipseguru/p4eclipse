/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.MessageSeverityCode;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.PerforceProviderPlugin;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4Resource extends PlatformObject implements IP4Resource,
        IEventObject {

    /**
     * Is this reasource read only?
     */
    protected boolean readOnly = false;

    /**
     * Does this resource need refreshing?
     */
    protected boolean needsRefresh = true;

    /**
     * Normalize a local path
     * 
     * @param localPath
     * @return - normalized path
     */
    public static String normalizeLocalPath(String localPath) {
        String normalized = null;
        if (localPath != null) {
            normalized = localPath.replace('/', File.separatorChar);
        }
        return normalized;
    }

    /**
     * Run client operation
     * 
     * @param operation
     */
    protected void runOperation(IP4ClientOperation operation) {
        if (operation != null) {
            boolean retry = true;
            IClient client = getClient();
            while (retry && client != null) {
                retry = false;
                try {
                    operation.run(client);
                } catch (P4JavaException exception) {
                    PerforceProviderPlugin.logError(exception);
                    operation.exception(exception);
                    retry = handleError(exception);
                    if (retry) {
                        client = getClient();
                    }
                } catch (P4JavaError error) {
                    PerforceProviderPlugin.logError(error);
                }
            }
        }
    }

    /**
     * Run a server operation
     * 
     * @param operation
     */
    protected void runOperation(IP4ServerOperation operation) {
        if (operation != null) {
            boolean retry = true;
            IServer server = getServer();
            while (retry && server != null) {
                retry = false;
                try {
                    operation.run(server);
                } catch (P4JavaException exception) {
                    PerforceProviderPlugin.logError(exception);
                    operation.exception(exception);
                    retry = handleError(exception);
                    if (retry) {
                        server = getServer();
                    }
                } catch (P4JavaError error) {
                    PerforceProviderPlugin.logError(error);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#markForRefresh()
     */
    public void markForRefresh() {
        needsRefresh = true;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#needsRefresh()
     */
    public boolean needsRefresh() {
        return needsRefresh;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getServer()
     */
    public IServer getServer() {
        IP4Connection connection = getConnection();
        if (connection != null) {
            return connection.getServer();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isReadOnly()
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Normalize a file spec path using either the local or client path from the
     * spec in that order
     * 
     * @param spec
     * @return - normalized local path
     */
    public static String normalizeLocalPath(IFileSpec spec) {
        String localPath = null;
        if (spec != null) {
            FilePath filePath = spec.getLocalPath();
            String clientOrLocalPath = filePath != null ? filePath
                    .getPathString() : null;
            if (clientOrLocalPath == null) {
                filePath = spec.getClientPath();
                clientOrLocalPath = filePath != null
                        ? filePath.getPathString()
                        : null;
            }
            if (clientOrLocalPath != null) {
                if (clientOrLocalPath.startsWith("//")) { //$NON-NLS-1$
                    String root = spec.getClientName();
                    if (root != null) {
                        localPath = getPathHostSyntax(root, clientOrLocalPath);
                    } else {
                        localPath = normalizeLocalPath(clientOrLocalPath);
                        localPath = unformatFilename(localPath);
                    }
                } else {
                    localPath = normalizeLocalPath(clientOrLocalPath);
                }
            }
        }
        return localPath;
    }

    /**
     * Unformats a filename containing escaped characters
     * 
     * @param filename
     * @return - unformatted filename
     */
    public static String unformatFilename(String filename) {
        if (filename != null && filename.indexOf('%') > -1) {
            StringBuilder buff = new StringBuilder(filename.length());
            int end;
            for (int start = 0;; start = end + 3) {
                end = filename.indexOf('%', start);
                if (end == -1) {
                    buff.append(filename.substring(start));
                    break;
                }
                buff.append(filename.substring(start, end));
                String fch = filename.substring(end, end + 3);
                if (fch.equals("%40")) { //$NON-NLS-1$
                    buff.append('@');
                } else if (fch.equals("%23")) { //$NON-NLS-1$
                    buff.append('#');
                } else if (fch.equals("%25")) { //$NON-NLS-1$
                    buff.append('%');
                } else if (fch.equals("%2A")) { //$NON-NLS-1$
                    buff.append('*');
                }
            }
            filename = buff.toString();
        }
        return filename;
    }

    /**
     * Convert a path containing the client root to the actual path on disk
     * 
     * @param root
     * @param path
     * @return - converted path
     */
    public static String getPathHostSyntax(String root, String path) {
        if (path != null && root != null) {
            int idx = path.indexOf('/', 2);
            if (root.toLowerCase().equals("null")) { //$NON-NLS-1$
                path = path.substring(idx + 1);
            } else if (root.endsWith("\\") || root.endsWith("/")) { //$NON-NLS-1$//$NON-NLS-2$
                path = root + path.substring(idx + 1);
            } else {
                if(idx>0)
                    path = root + path.substring(idx);
            }
            path = path.replace('/', File.separatorChar);
            // Convert formated special chars into local syntax
            path = unformatFilename(path);
        }
        return path;
    }

    /**
     * Listener list for event broadcasting
     */
    protected ListenerList listeners = new ListenerList();

    /**
     * Current error handler
     */
    protected IErrorHandler errorHandler = null;

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#getErrorHandler()
     */
    public IErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * Gets a local resource path from a possible client or local path passed in
     * 
     * @param clientOrLocalPath
     * @return - local path
     */
    protected String getLocalResourcePath(String clientOrLocalPath) {
        String localPath = null;
        if (clientOrLocalPath != null) {
            if (clientOrLocalPath.startsWith("//")) { //$NON-NLS-1$
                localPath = getPathHostSyntax(clientOrLocalPath);
            } else {
                localPath = normalizeLocalPath(clientOrLocalPath);
            }
        }
        return localPath;
    }

    /**
     * Gets the path host syntax
     * 
     * @param path
     * @return - local path
     */
    protected String getPathHostSyntax(String path) {
        IClient client = getClient();
        if (client != null) {
            String root = client.getRoot();
            path = getPathHostSyntax(root, path);
        }
        return path;
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#setErrorHandler(com.perforce.team.core.p4java.IErrorHandler)
     */
    public void setErrorHandler(IErrorHandler handler) {
        this.errorHandler = handler;
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
     * @see com.perforce.team.core.p4java.IEventObject#clearListeners()
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#notifyListeners(com.perforce.team.core.p4java.P4Event)
     */
    public void notifyListeners(P4Event event) {
        for (Object listener : listeners.getListeners()) {
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
     * Does this resource have an equal connection to the specified other
     * resource
     * 
     * @param other
     * @return - true if connections are equal, false otherwise
     */
    protected boolean connectionEquals(IP4Resource other) {
        IP4Connection connection = getConnection();
        IP4Connection otherConnection = null;
        if (other != null) {
            otherConnection = other.getConnection();
        }
        return connection != null && connection.equals(otherConnection);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof IP4Resource) {
            IP4Resource resource = (IP4Resource) obj;
            if (!connectionEquals(resource)) {
                return false;
            }
            // Compare a common path if any exist. This is required since a
            // local path may only exist if an added file is reverted and fstat
            // no longer returns a file spec
            if (this.getRemotePath() != null
                    && resource.getRemotePath() != null) {
                return this.getRemotePath().equals(resource.getRemotePath());
            } else if (this.getClientPath() != null
                    && resource.getClientPath() != null) {
                return this.getClientPath().equals(resource.getClientPath());
            } else if (this.getLocalPath() != null
                    && resource.getLocalPath() != null) {
                return this.getLocalPath().equals(resource.getLocalPath());
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        String path = this.getLocalPath();
        if (path == null) {
            path = this.getRemotePath();
        }
        if (path == null) {
            path = this.getClientPath();
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
     * @see com.perforce.team.core.p4java.IP4Resource#add()
     */
    public void add() {
        add(0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#add(int)
     */
    public void add(int changelist) {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#delete()
     */
    public void delete() {
        delete(0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#delete(int)
     */
    public void delete(int changelist) {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#edit()
     */
    public void edit() {
        edit(0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#edit(int)
     */
    public void edit(int changelist) {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#ignore()
     */
    public void ignore() {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#revert()
     */
    public void revert() {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#sync()
     */
    public void sync(IProgressMonitor monitor, IP4ProgressListener callback) {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#sync()
     */
    public void sync(IProgressMonitor monitor) {
    	sync(monitor, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#handleError(com.perforce.p4java.exception.P4JavaException)
     */
    public boolean handleError(P4JavaException exception) {
        boolean retry = false;
        IP4Connection connection = getConnection();
        if (this.errorHandler != null) {
            retry = this.errorHandler.shouldRetry(connection, exception);
        } else if (connection != null) {
            retry = connection.handleError(exception);
        }
        return retry;
    }

    /**
     * Helper method to determine if a spec is an actual error spec. Accounts
     * for some special conditions
     * 
     * @param spec
     * @return - true if error
     */
    protected boolean isErrorSpec(IFileSpec spec) {
        return spec != null
                && FileSpecOpStatus.ERROR == spec.getOpStatus()
                && spec.getStatusMessage() != null
                && spec.getSeverityCode()>MessageSeverityCode.E_WARN
                && !spec.getStatusMessage().endsWith("- no such file(s).")
                && !spec.getStatusMessage().endsWith("file(s) up-to-date.")
                && !spec.getStatusMessage().endsWith(
                        "file(s) not opened on this client.")
                && !spec.getStatusMessage().endsWith(
                        "file(s) not opened for edit.")
                && !spec.getStatusMessage().endsWith("file(s) not on client.");
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorReporter#handleErrors(com.perforce.p4java.core.file.IFileSpec[])
     */
    public void handleErrors(IFileSpec[] specs) {
        IErrorHandler handler = this.errorHandler;
        IP4Connection connection = getConnection();
        if (handler != null) {
            for (IFileSpec spec : specs) {
                if (isErrorSpec(spec)) {
                    handler.handleErrorSpecs(specs);
                    break;
                }
            }
        } else if (connection != null) {
            connection.handleErrors(specs);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isFile()
     */
    public boolean isFile() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isCaseSensitive()
     */
    public boolean isCaseSensitive() {
        IP4Connection connection = getConnection();
        return connection != null ? connection.isCaseSensitive() : true;
    }

}
