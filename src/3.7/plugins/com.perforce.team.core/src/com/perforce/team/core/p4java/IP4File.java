/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileAnnotation;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IFileSpec;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;

/**
 * Base interface for a file in a Perforce repository
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4File extends IEventObject, IContentEmitter, IP4Resource {


    /**
     * Whitespace ignore types
     */
    enum WhitespaceIgnoreType {

        /**
         * Ignore all -dw
         */
        ALL,

        /**
         * Ignore whitespace -db
         */
        WHITESPACE,

        /**
         * Ignore line endings -dl
         */
        LINE_ENDINGS,

    }

    /**
     * Gets the workspace resource corresponding to the local path of this p4
     * file. This method will only return an IFile if the local path of this
     * resource is contained in the Eclipse workspace being used.
     * 
     * @return - workspace file
     */
    IFile[] getLocalFiles();

    /**
     * Gets the single workspace resource corresponding to the local of this p4
     * file.
     * 
     * @return - workspace file
     */
    IFile getLocalFileForLocation();

    /**
     * Sets the file spec of this p4 file.
     * 
     * @param spec
     */
    void setFileSpec(IFileSpec spec);

    /**
     * Set file spec and place resources needing a refresh event in the
     * specified collection
     * 
     * @param spec
     * @param refreshEventContainer
     * @param updateChangelist whether update changelist or not
     */
    void setFileSpec(IFileSpec spec, P4Collection refreshEventContainer, boolean updateChangelist);

    /**
     * Sets the integration specs for this file
     * 
     * @param integSpecs
     */
    void setIntegrationSpecs(IFileSpec[] integSpecs);

    /**
     * Gets the first integration spec for this file
     * 
     * @return - integration spec or null
     */
    IFileSpec getIntegrationSpec();

    /**
     * Gets the integration specs for this file
     * 
     * @return - integration specs or null
     */
    IFileSpec[] getIntegrationSpecs();
    
    /**
     * Sets the parent of this p4 file.
     * 
     * @param parent
     */
    void setParent(IP4Container parent);

    /**
     * Returns the changelist that this file is currently part of if it is
     * opened on this client. If fetchIfNotFound is set to true then if the
     * changelist if not currently in the cache a p4 describe/change will be
     * done to load the changelist. This can occur if changes are made outside
     * of this model and the fstat output for the changelist of a file is not
     * currently in the pending changelist cache.
     * 
     * This method should not be called on the UI-thread if fetchIfNotFound is
     * specified as true.
     * 
     * This method will not ignore errors in the case where the changelist id of
     * the file spec no longer returns a pending changelist when a
     * change/describe is done.
     * 
     * @param fetchIfNotFound
     *            - fetch changelist from server if not found
     * 
     * @return - p4 changelist or null if not opened
     */
    IP4PendingChangelist getChangelist(boolean fetchIfNotFound);

    /**
     * Returns the changelist that this file is currently part of if it is
     * opened on this client. If fetchIfNotFound is set to true then if the
     * changelist if not currently in the cache a p4 describe/change will be
     * done to load the changelist. This can occur if changes are made outside
     * of this model and the fstat output for the changelist of a file is not
     * currently in the pending changelist cache.
     * 
     * This method should not be called on the UI-thread if fetchIfNotFound is
     * specified as true.
     * 
     * @param fetchIfNotFound
     *            - fetch changelist from server if not found
     * @param ignoreErrors
     * 
     * @return - p4 changelist or null if not opened
     */
    IP4PendingChangelist getChangelist(boolean fetchIfNotFound,
            boolean ignoreErrors);

    /**
     * Returns the changelist that this file is currently part of if it is
     * opened on this client
     * 
     * @return - p4 changelist or null if not opened
     */
    IP4PendingChangelist getChangelist();

    /**
     * Gets the file spec object retrieved from P4Java
     * 
     * @return - p4 java file spec
     */
    IFileSpec getP4JFile();

    /**
     * Get local and remote history of this file. The local history may be
     * non-existent or the remote history may be non-existent depending on the
     * workspace and file state.
     * 
     * @param includeBranches
     * @param monitor
     * 
     * @return - array of file revisions
     */
    IFileRevision[] getCompleteHistory(boolean includeBranches,
            IProgressMonitor monitor);

    /**
     * Gets the revision history for this file
     * 
     * @return - array of history objects
     */
    IFileRevisionData[] getHistory();

    /**
     * Gets the history with the option to leave out branching history
     * 
     * @param displayBranching
     *            - true to display branching history
     * @return - array of history objects
     */
    IFileRevisionData[] getHistory(boolean displayBranching);

    /**
     * Gets the changelist number of the head revision
     * 
     * @return - head changelist revision
     */
    int getHeadChange();

    /**
     * Gets the mod time of the head revision
     * 
     * @return - head mod time
     */
    long getHeadTime();

    /**
     * Gets the current head revision number of this file
     * 
     * @return - int revision number
     */
    int getHeadRevision();

    /**
     * Gets the revision number of the file current in the perforce client
     * workspace
     * 
     * @return - int revision number
     */
    int getHaveRevision();

    /**
     * Gets the head type if available
     * 
     * @return - head type or null
     */
    String getHeadType();

    /**
     * Gets the open type if available
     * 
     * @return - open type or null
     */
    String getOpenedType();

    /**
     * Gets the action
     * 
     * @return - file action
     */
    FileAction getAction();

    /**
     * Gets the head action if available
     * 
     * @return - file action or null
     */
    FileAction getHeadAction();

    /**
     * Is the current head action one denoting a delete operation
     * 
     * @return - true if delete, false otherwise
     */
    boolean isHeadActionDelete();

    /**
     * Is the current head action one denoting an edit operation
     * 
     * @return - true if edit, false otherwise
     */
    boolean isHeadActionEdit();

    /**
     * Is the current head action one denoting an add operation
     * 
     * @return - true if add, false otherwise
     */
    boolean isHeadActionAdd();

    /**
     * Gets the resolve path. This will only be valid when this file needs
     * resolve
     * 
     * @return - string of path resolving from
     */
    String getResolvePath();

    /**
     * Returns true if this p4 file exists in the p4 client workspace
     * 
     * @return - true if exists in the p4 client workspace, false otherwise
     */
    boolean isLocal();

    /**
     * Returns true if this p4 files exists in the p4 depot or false if it
     * hasn't been added to the depot yet
     * 
     * @return - true if in the depot, false otherwise
     */
    boolean isRemote();

    /**
     * Returns true if the have revision is the same as the head revision for
     * the current state
     * 
     * @return - true if synced, false otherwise
     */
    boolean isSynced();

    /**
     * Returns true if this file is contained in a peer .p4ignore file
     * 
     * @return - true if ignored, false otherwise
     */
    boolean isIgnored();

    /**
     * Returns true if this file is currently opened for add
     * 
     * @return - true if opened for add, false otherwise
     */
    boolean openedForAdd();

    /**
     * Returns true if the action owner of this file is the user associated with
     * the parent or connection. If no action owner can be found then this will
     * return false. This can occur write after a p4 add but before a full fstat
     * is done since the action is set but the owner is unknown. Note it is
     * possible in this case that this method and {@link #openedByOtherOwner()}
     * will both be false.
     * 
     * 
     * @return - true if opened by the connection owner
     */
    boolean openedByOwner();

    /**
     * Returns true if the action owner of this file is not the user associated
     * with the parent or connection. If no action owner can be found then this
     * will return false. This can occur write after a p4 add but before a full
     * fstat is done since the action is set but the owner is unknown. Note it
     * is possible in this case that this method and {@link #openedByOwner()}
     * will both be false.
     * 
     * @return - true if opened by a user other than the connection owner
     */
    boolean openedByOtherOwner();

    /**
     * Returns true if this file is currently opened for edit
     * 
     * @return - true if opened for edit, false otherwise
     */
    boolean openedForEdit();

    /**
     * Returns true if this file is currently opened for delete
     * 
     * @return - true if opened for delete, false otherwise
     */
    boolean openedForDelete();

    /**
     * Returns true if this file is opened on this client
     * 
     * @return - true if open, false otherwise
     */
    boolean isOpened();

    /**
     * Returns true if this file is unresolved
     * 
     * @return - true if unresolved, false otherwise
     */
    boolean isUnresolved();

    /**
     * Gets the remote contents for the current revision
     * 
     * @return - input stream
     */
    InputStream getRemoteContents();

    /**
     * Gets the remote contents for a specific revision
     * 
     * @param revision
     * @return - input stream
     */
    InputStream getRemoteContents(int revision);

    /**
     * Gets the remote contents for a specific revision
     * 
     * @param revision
     * @return - input stream
     */
    InputStream getRemoteContents(String revision);

    /**
     * Gets the remote content for the revision returned by
     * {@link #getHeadRevision()}.
     * 
     * @return - input stream
     */
    InputStream getHeadContents();

    /**
     * Gets the remote content for the revision returned by
     * {@link #getHaveRevision()}.
     * 
     * @return - input stream
     */
    InputStream getHaveContents();

    /**
     * Moves this file to the new path
     * 
     * @param toFile
     * @param useMoveCommand
     * @return - true if the move was successful, false otherwise
     */
    boolean move(IP4File toFile, boolean useMoveCommand);

    /**
     * Moves this file to the new path opened in the specified changelist
     * 
     * @param toFile
     * @param useMoveCommand
     * @param changelist
     * @return - true if the move was successful, false otherwise
     */
    boolean move(IP4File toFile, boolean useMoveCommand, int changelist);

    /**
     * Moves this file to the new path opened in the specified changelist
     * 
     * @param toFile
     * @param useMoveCommand
     * @param changelist
     * @param bypassClient
     * @return - true if the move was successful, false otherwise
     */
    boolean move(IP4File toFile, boolean useMoveCommand, int changelist,
            boolean bypassClient);

    /**
     * Moves this file to the new path opened in the specified changelist
     * 
     * @param toFile
     * @param useMoveCommand
     * @param selection
     * @param bypassClient
     * @return - true if the move was successful, false otherwise
     */
    boolean move(IP4File toFile, boolean useMoveCommand,
            ChangelistSelection selection, boolean bypassClient);

    /**
     * Gets the user name for this file from the underlying p4 java file spec
     * 
     * @return - user name
     */
    String getUserName();

    /**
     * Gets the client name for this file from the underlying p4 java file spec
     * 
     * @return - client name
     */
    String getClientName();

    /**
     * Gets the changelist id for this file from the underlying p4 java file
     * spec
     * 
     * @return - changelist id
     */
    int getChangelistId();

    /**
     * Returns the status of the underlying p4 java file spec. This will usually
     * be the VALID value since p4 files are usually only created for valid file
     * spec.
     * 
     * @return - file spec status
     */
    FileSpecOpStatus getStatus();

    /**
     * Returns the status message of the underlying p4java file spec.
     * 
     * @return - status message
     */
    String getStatusMessage();

    /**
     * Returns true if this file is locked
     * 
     * @return - true if locked
     */
    boolean isLocked();

    /**
     * Returns true if this file is opened elsewhere
     * 
     * @return - true if opened elsewhere
     */
    boolean openedElsewhere();

    /**
     * Get the other action that are currently open against this file
     * 
     * @return - other actions being opened on this file
     */
    List<String> getOtherActions();

    /**
     * Get the other users who are currently editing this file
     * 
     * @return - other users editing this file (part of a changelist)
     */
    List<String> getOtherEditors();

    /**
     * Get the other pending changelists that this file is in
     * 
     * @return - other pending changelists
     */
    List<String> getOtherChangelists();

    /**
     * Get the annotations of this file
     * 
     * @param followBranches
     * @return - non-null array of {@link IFileAnnotation}
     */
    IFileAnnotation[] getAnnotations(boolean followBranches);

    /**
     * Get the annotations of this file
     * 
     * @return - non-null array of {@link IFileAnnotation}
     */
    IFileAnnotation[] getAnnotations();

    /**
     * Get the annotations of this file
     * 
     * @param followBranches
     * @param ignoreType
     * @return - non-null array of {@link IFileAnnotation}
     */
    IFileAnnotation[] getAnnotations(boolean followBranches,
            WhitespaceIgnoreType ignoreType);

    /**
     * Get the annotations of this file
     * 
     * @param followBranches
     * @param ignoreType
     * @param outputChangeNumbers
     * @return - non-null array of {@link IFileAnnotation}
     */
    IFileAnnotation[] getAnnotations(boolean followBranches,
            WhitespaceIgnoreType ignoreType, boolean outputChangeNumbers);

    /**
     * Get all the shelved versions of this file
     * 
     * @return - non-null array of {@link IP4ShelveFile}
     */
    IP4ShelveFile[] getShelvedVersions();

    /**
     * Get the moved file if this file is currently open for move/add or
     * move/delete
     * 
     * @return - moved file or null if not opened for move
     */
    String getMovedFile();

    /**
     * Get the local filesystem file representing this perforce file. May return
     * or a non-existent file handle.
     * 
     * @return - file, may be null if this perforce file does not have a local
     *         path
     */
    File toFile();

}
