/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.server.IServer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Base interface representing a p4 resource
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Resource extends IErrorReporter, IAdaptable {

	String MIRROR_STREAM="//streams/mirror"; //$NON-NLS-1$
	String MIRROR_FOLDER="mirror"; //$NON-NLS-1$

    /**
     * Empty resource array
     */
    IP4Resource[] EMPTY = new IP4Resource[0];

    /**
     * An enumeration of the path types
     */
    public enum Type {
        /**
         * Use the local path for resources in this collection
         */
        LOCAL,

        /**
         * Use the remote path for resources in this collection
         */
        REMOTE,
    }

    /**
     * Gets the name of this p4 resource. This method should include the name
     * including any file extension
     * 
     * @return - string name
     */
    String getName();

    /**
     * Gets the local path of the resource in an OS-specific absolute file path
     * 
     * @return - local path of the resource
     */
    String getLocalPath();

    /**
     * Gets the client path of the resource
     * 
     * @return - client path of the resource
     */
    String getClientPath();

    /**
     * Gets the remote depot path of the resource in the depot path syntax
     * 
     * @return - remote depot path
     */
    String getRemotePath();

    /**
     * Gets the action path of this resource which is the path used when passed
     * to p4 commands.
     * 
     * @return - action path to use when passing this p4 resource path as a
     *         parameter to a p4 command
     */
    String getActionPath();

    /**
     * Gets the action path of this resource which is the path used when passed
     * to p4 commands.
     * 
     * @param preferredType
     *            - which path to use
     * 
     * @return - action path to use when passing this p4 resource path as a
     *         parameter to a p4 command
     */
    String getActionPath(Type preferredType);

    /**
     * Gets the parent of this resource or null if this is a root resource
     * 
     * @return - p4 container parent
     */
    IP4Container getParent();

    /**
     * Gets the P4Java client object for this resource
     * 
     * @return - p4j client
     */
    IClient getClient();

    /**
     * Gets the P4Java server object from this resource's connection
     * 
     * @return - p4j server
     */
    IServer getServer();

    /**
     * Gets the connection object containing client, username, password, and
     * server information
     * 
     * @return - p4 connection
     */
    IP4Connection getConnection();

    /**
     * Opens this resource for edit in the default changelist
     * 
     */
    void edit();

    /**
     * Opens this resources for edit in the specified changelist
     * 
     * @param changelist
     */
    void edit(int changelist);

    /**
     * Opens this resource for add in the default changelist
     * 
     */
    void add();

    /**
     * Opens this resource for add in the specified changelist
     * 
     * @param changelist
     */
    void add(int changelist);

    /**
     * Opens this resource for delete in the default changelist
     * 
     */
    void delete();

    /**
     * Opens this resource for delete in the specified changelist
     * 
     * @param changelist
     */
    void delete(int changelist);

    /**
     * Reverts this resource
     * 
     */
    void revert();

    /**
     * Adds this resource to a peer .p4ignore file
     * 
     */
    void ignore();

    /**
     * Syncs this resource against the depot
     * 
     * @param monitor
     */
    void sync(IProgressMonitor monitor,IP4ProgressListener callback);

    /**
     * Syncs this resource against the depot
     * 
     * @param monitor
     */
    void sync(IProgressMonitor monitor);

    /**
     * Refreshes this resource with the latest state from the server
     * 
     * @param depth
     *            - IResource#DEPTH_ONE or IResource#DEPTH_INFINITE
     */
    void refresh(int depth);

    /**
     * Refreshes this resource and any direct children if this resource is a
     * container
     */
    void refresh();

    /**
     * Is this resource a container for other resources?
     * 
     * @return - true if a container, false otherwise
     */
    boolean isContainer();

    /**
     * Is this a resource that represents a file?
     * 
     * @return - true if a file, false otherwise
     */
    boolean isFile();

    /**
     * Can any actions be made against this resource?
     * 
     * @return -true if supports actions, false otherwise
     */
    boolean isReadOnly();

    /**
     * Marks this container as need to refresh the child members
     */
    void markForRefresh();

    /**
     * True if this container should be refreshed
     * 
     * @return - true for refresh, false otherwise
     */
    boolean needsRefresh();

    /**
     * Is the connected server case sensitive?
     * 
     * @return - true if case sensitive, false if case insensitive
     */
    boolean isCaseSensitive();

}
