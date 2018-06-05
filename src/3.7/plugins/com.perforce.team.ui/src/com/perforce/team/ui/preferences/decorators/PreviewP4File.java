/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences.decorators;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileAnnotation;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.ChangelistSelection;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4ProgressListener;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4File;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PreviewP4File implements IP4File {

    private FileAction headAction;
    private FileAction action;
    private boolean unresolved = false;
    private boolean synced = false;
    private String name = ""; //$NON-NLS-1$
    private boolean locked = false;
    private boolean openedByOwner = true;
    private boolean openedElsewhere = false;
    private String headType = null;
    private boolean unmanaged = false;
    private int haveRevision = 0;
    private int headRevision = 0;
    private IFileSpec spec = new FileSpec("//preview/p4/file"); //$NON-NLS-1$

    /**
     * Creates an empty preview file
     */
    public PreviewP4File() {
    }

    /**
     * @param openedByOwner
     *            the openedByOwner to set
     */
    public void setOpenedByOwner(boolean openedByOwner) {
        this.openedByOwner = openedByOwner;
    }

    /**
     * @param openedElsewhere
     *            the openedElsewhere to set
     */
    public void setOpenedElsewhere(boolean openedElsewhere) {
        this.openedElsewhere = openedElsewhere;
    }

    /**
     * @param locked
     *            the locked to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @param headAction
     *            the headAction to set
     */
    public void setHeadAction(FileAction headAction) {
        this.headAction = headAction;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(FileAction action) {
        this.action = action;
    }

    /**
     * @param unresolved
     *            the unresolved to set
     */
    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
    }

    /**
     * @param synced
     *            the synced to set
     */
    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAction()
     */
    public FileAction getAction() {
        return this.action;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getChangelistId()
     */
    public int getChangelistId() {
        return 0;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getClientName()
     */
    public String getClientName() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHaveRevision()
     */
    public int getHaveRevision() {
        return this.haveRevision;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadAction()
     */
    public FileAction getHeadAction() {
        return this.headAction;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadChange()
     */
    public int getHeadChange() {
        return 1984;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadRevision()
     */
    public int getHeadRevision() {
        return this.headRevision;
    }

    /**
     * @param haveRevision
     *            the haveRevision to set
     */
    public void setHaveRevision(int haveRevision) {
        this.haveRevision = haveRevision;
    }

    /**
     * @param headRevision
     *            the headRevision to set
     */
    public void setHeadRevision(int headRevision) {
        this.headRevision = headRevision;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadTime()
     */
    public long getHeadTime() {
        return 0;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadType()
     */
    public String getHeadType() {
        return this.headType;
    }

    /**
     * @param headType
     *            the headType to set
     */
    public void setHeadType(String headType) {
        this.headType = headType;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHistory()
     */
    public IFileRevisionData[] getHistory() {
        return new IFileRevisionData[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHistory(boolean)
     */
    public IFileRevisionData[] getHistory(boolean displayBranching) {
        return new IFileRevisionData[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getLocalFileForLocation()
     */
    public IFile getLocalFileForLocation() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getLocalFiles()
     */
    public IFile[] getLocalFiles() {
        return new IFile[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getOpenedType()
     */
    public String getOpenedType() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getOtherActions()
     */
    public List<String> getOtherActions() {
        return Collections.emptyList();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getOtherChangelists()
     */
    public List<String> getOtherChangelists() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getOtherEditors()
     */
    public List<String> getOtherEditors() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getP4JFile()
     */
    public IFileSpec getP4JFile() {
        return this.spec;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getRemoteContents()
     */
    public InputStream getRemoteContents() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getRemoteContents(int)
     */
    public InputStream getRemoteContents(int revision) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getResolvePath()
     */
    public String getResolvePath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getStatus()
     */
    public FileSpecOpStatus getStatus() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getStatusMessage()
     */
    public String getStatusMessage() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getUserName()
     */
    public String getUserName() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isIgnored()
     */
    public boolean isIgnored() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isLocal()
     */
    public boolean isLocal() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isLocked()
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isOpened()
     */
    public boolean isOpened() {
        return openedForAdd() || openedForDelete() || openedForEdit();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isRemote()
     */
    public boolean isRemote() {
        return false;
    }

    /**
     * @param unmanaged
     *            the unmanaged to set
     */
    public void setUnmanaged(boolean unmanaged) {
        this.unmanaged = unmanaged;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isSynced()
     */
    public boolean isSynced() {
        return this.synced;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isUnresolved()
     */
    public boolean isUnresolved() {
        return this.unresolved;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedByOwner()
     */
    public boolean openedByOwner() {
        return this.openedByOwner;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedElsewhere()
     */
    public boolean openedElsewhere() {
        return this.openedElsewhere;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedForAdd()
     */
    public boolean openedForAdd() {
        return P4File.isActionAdd(action);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedForDelete()
     */
    public boolean openedForDelete() {
        return P4File.isActionDelete(action);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedForEdit()
     */
    public boolean openedForEdit() {
        return P4File.isActionEdit(action);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#setFileSpec(com.perforce.p4java.core.file.IFileSpec)
     */
    public void setFileSpec(IFileSpec spec) {
        this.spec = spec;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#setParent(com.perforce.team.core.p4java.IP4Container)
     */
    public void setParent(IP4Container parent) {

    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#addListener(com.perforce.team.core.p4java.IP4Listener)
     */
    public void addListener(IP4Listener listener) {

    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#addListeners(com.perforce.team.core.p4java.IP4Listener[])
     */
    public void addListeners(IP4Listener[] listeners) {

    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#clearListeners()
     */
    public void clearListeners() {

    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#notifyListeners(com.perforce.team.core.p4java.P4Event)
     */
    public void notifyListeners(P4Event event) {

    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#removeListener(com.perforce.team.core.p4java.IP4Listener)
     */
    public void removeListener(IP4Listener listener) {

    }

    /**
     * @see com.perforce.team.core.p4java.IContentEmitter#getAs(java.lang.String)
     */
    public String getAs(String contentType) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IContentEmitter#getSupportedTypes()
     */
    public String[] getSupportedTypes() {
        return new String[0];
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
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        return ""; //$NON-NLS-1$
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
        return this.name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getName();
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
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {

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

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#sync()
     */
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
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (IResource.class.equals(adapter) && !unmanaged) {
            return ResourcesPlugin.getWorkspace().getRoot();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#move(com.perforce.team.core.p4java.IP4File,
     *      boolean)
     */
    public boolean move(IP4File toFile, boolean useMoveCommand) {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getServer()
     */
    public IServer getServer() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#move(com.perforce.team.core.p4java.IP4File,
     *      boolean, int)
     */
    public boolean move(IP4File toFile, boolean useMoveCommand, int changelist) {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHaveContents()
     */
    public InputStream getHaveContents() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadContents()
     */
    public InputStream getHeadContents() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isHeadActionAdd()
     */
    public boolean isHeadActionAdd() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isHeadActionDelete()
     */
    public boolean isHeadActionDelete() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isHeadActionEdit()
     */
    public boolean isHeadActionEdit() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedByOtherOwner()
     */
    public boolean openedByOtherOwner() {
        return !this.openedByOwner;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getRemoteContents(java.lang.String)
     */
    public InputStream getRemoteContents(String revision) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getCompleteHistory(boolean,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public IFileRevision[] getCompleteHistory(boolean includeBranches,
            IProgressMonitor monitor) {
        return new IFileRevision[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getChangelist(boolean)
     */
    public IP4PendingChangelist getChangelist(boolean fetchIfNotFound) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getChangelist()
     */
    public IP4PendingChangelist getChangelist() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAnnotations(boolean)
     */
    public IFileAnnotation[] getAnnotations(boolean followBranches) {
        return new IFileAnnotation[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#markForRefresh()
     */
    public void markForRefresh() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#needsRefresh()
     */
    public boolean needsRefresh() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getShelvedVersions()
     */
    public IP4ShelveFile[] getShelvedVersions() {
        return new IP4ShelveFile[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAnnotations()
     */
    public IFileAnnotation[] getAnnotations() {
        return new IFileAnnotation[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAnnotations(boolean,
     *      com.perforce.team.core.p4java.IP4File.WhitespaceIgnoreType)
     */
    public IFileAnnotation[] getAnnotations(boolean followBranches,
            WhitespaceIgnoreType ignoreType) {
        return new IFileAnnotation[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#setIntegrationSpec(com.perforce.p4java.core.file.IFileSpec)
     */
    public void setIntegrationSpecs(IFileSpec[] integSpec) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getIntegrationSpec()
     */
    public IFileSpec getIntegrationSpec() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getIntegrationSpec()
     */
    public IFileSpec[] getIntegrationSpecs() {
        return new IFileSpec[0];
    }
    
    /**
     * @see com.perforce.team.core.p4java.IP4File#move(com.perforce.team.core.p4java.IP4File,
     *      boolean, int, boolean)
     */
    public boolean move(IP4File toFile, boolean useMoveCommand, int changelist,
            boolean bypassClient) {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getMovedFile()
     */
    public String getMovedFile() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isFile()
     */
    public boolean isFile() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#toFile()
     */
    public File toFile() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAnnotations(boolean,
     *      com.perforce.team.core.p4java.IP4File.WhitespaceIgnoreType, boolean)
     */
    public IFileAnnotation[] getAnnotations(boolean followBranches,
            WhitespaceIgnoreType ignoreType, boolean outputChangeNumbers) {
        return new IFileAnnotation[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getChangelist(boolean,
     *      boolean)
     */
    public IP4PendingChangelist getChangelist(boolean fetchIfNotFound,
            boolean ignoreErrors) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isCaseSensitive()
     */
    public boolean isCaseSensitive() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#move(com.perforce.team.core.p4java.IP4File,
     *      boolean, com.perforce.team.core.p4java.ChangelistSelection, boolean)
     */
    public boolean move(IP4File toFile, boolean useMoveCommand,
            ChangelistSelection selection, boolean bypassClient) {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#setFileSpec(com.perforce.p4java.core.file.IFileSpec,
     *      com.perforce.team.core.p4java.P4Collection)
     */
    public void setFileSpec(IFileSpec spec, P4Collection refreshEventContainer, boolean updateChangelist) {
        setFileSpec(spec);
    }

}
