/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.team.core.history.IFileRevision;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.file.DiffType;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileAnnotation;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.core.file.IntegrationOptions;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4File extends P4Resource implements IP4File {

    /**
     * Is the specified file action a delete action?
     *
     * @param action
     * @return - true if delete, false otherwise
     */
    public static boolean isActionDelete(FileAction action) {
        return FileAction.DELETE == action || FileAction.MOVE_DELETE == action;
    }

    /**
     * Is the specified file action an edit action?
     *
     * @param action
     * @return - true if edit, false otherwise
     */
    public static boolean isActionEdit(FileAction action) {
        return FileAction.EDIT == action || FileAction.INTEGRATE == action;
    }

    /**
     *
     * @param action
     * @return - true if add, false otherwise
     */
    public static boolean isActionAdd(FileAction action) {
        return FileAction.ADD == action || FileAction.BRANCH == action
                || FileAction.MOVE_ADD == action;
    }

    private IFileSpec fileSpec;
    private IP4Container parent;
    private String localPath = null;
    private IFileSpec integSpecs[] = null;

    /**
     * Creates a new p4 file.
     *
     * @param connection
     * @param localPath
     */
    public P4File(IP4Connection connection, String localPath) {
        if (connection != null) {
            this.parent = connection;
            this.localPath = getLocalResourcePath(localPath);
        }
    }

    /**
     * Creates a new p4 file from a P4Java file spec and container parent
     *
     * @param fileSpec
     * @param parent
     * @param readOnly
     */
    public P4File(IFileSpec fileSpec, IP4Container parent, boolean readOnly) {
        this(fileSpec, parent);
        this.readOnly = readOnly;
    }

    /**
     * Creates a new p4 file from a P4Java file spec and container parent
     *
     * @param fileSpec
     * @param parent
     */
    public P4File(IFileSpec fileSpec, IP4Container parent) {
        this.fileSpec = fileSpec;
        this.parent = parent;
        if (this.fileSpec != null) {
            this.localPath = getLocalResourcePath(this.fileSpec
                    .getClientPathString());
        }
        updateServer();
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IP4File)) {
            return false;
        }
        return super.equals(obj);
    }

    private List<IFileSpec> getActionSpec() {
        return P4FileSpecBuilder
                .makeFileSpecList(new String[] { getActionPath() });
    }

    private void updateServer() {
        if (this.fileSpec != null) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                IServer server = connection.getServer();
                if (server != null && this.fileSpec instanceof FileSpec) {
                    ((FileSpec) this.fileSpec).setServer(server);
                }
            }
        }
    }

    private void checkSpec() {
        if (this.fileSpec == null) {
            boolean retry = true;
            while (retry) {
                retry = false;
                try {
                    IClient client = getClient();
                    if (client != null) {
                        IServer server = client.getServer();
                        if (server != null) {
                            List<IFileSpec> specs = getClient()
                                    .getServer()
                                    .getDepotFiles(
                                            P4FileSpecBuilder
                                                    .makeFileSpecList(new String[] { getLocalPath() }),
                                            false);
                            if (specs.size() == 1
                                    && specs.get(0) != null
                                    && FileSpecOpStatus.VALID.equals(specs.get(
                                            0).getOpStatus())) {
                                this.fileSpec = specs.get(0);
                                updateServer();
                            }
                        }
                    }
                } catch (P4JavaException e) {
                    retry = handleError(e);
                    PerforceProviderPlugin.logError(e);
                } catch (P4JavaError e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#add(int)
     */
    @Override
    public void add(final int changelist) {
        if (readOnly) {
            return;
        }
        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                client.addFiles(getActionSpec(), false, changelist, null, true);
            }
        };
        runOperation(op);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#edit(int)
     */
    @Override
    public void edit(final int changelist) {
        if (readOnly) {
            return;
        }
        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                client.editFiles(getActionSpec(), false, false, changelist,
                        null);
            }
        };
        runOperation(op);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#delete(int)
     */
    @Override
    public void delete(final int changelist) {
        if (readOnly) {
            return;
        }
        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                client.deleteFiles(getActionSpec(), changelist, false);
            }
        };
        runOperation(op);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHaveRevision()
     */
    public int getHaveRevision() {
        int have = 0;
        if (this.fileSpec instanceof IExtendedFileSpec) {
            have = ((IExtendedFileSpec) this.fileSpec).getHaveRev();
            have = Math.max(0, have);
        }
        return have;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadRevision()
     */
    public int getHeadRevision() {
        int head = 0;
        if (this.fileSpec instanceof IExtendedFileSpec) {
            head = ((IExtendedFileSpec) this.fileSpec).getHeadRev();
        } else if (this.fileSpec != null) {
            head = this.fileSpec.getStartRevision();
        }
        head = Math.max(0, head);
        return head;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHistory()
     */
    public IFileRevisionData[] getHistory() {
        return getHistory(true);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getP4JFile()
     */
    public IFileSpec getP4JFile() {
        return this.fileSpec;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#ignore()
     */
    @Override
    public void ignore() {

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
        if (this.fileSpec != null) {
            return this.fileSpec.getClientPath() != null;
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isRemote()
     */
    public boolean isRemote() {
        if (this.fileSpec instanceof IExtendedFileSpec) {
            return ((IExtendedFileSpec) this.fileSpec).getHeadRev() > 0;
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedForAdd()
     */
    public boolean openedForAdd() {
        return isActionAdd(getAction());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedForDelete()
     */
    public boolean openedForDelete() {
        return isActionDelete(getAction());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedForEdit()
     */
    public boolean openedForEdit() {
        return isActionEdit(getAction());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#revert()
     */
    @Override
    public void revert() {
        if (readOnly) {
            return;
        }
        IP4ClientOperation op = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                client.revertFiles(getActionSpec(), false, -1, false, false);
            }
        };
        runOperation(op);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#sync()
     */
    @Override
    public void sync(IProgressMonitor monitor) {

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
     * @see com.perforce.team.core.p4java.IP4Resource#getLocalPath()
     */
    public String getLocalPath() {
        if (this.fileSpec != null) {
            String path = this.fileSpec.getLocalPathString();
            if (path == null) {
                path = this.fileSpec.getClientPathString();
            }
            path = getLocalResourcePath(path);
            return path;
        } else if (this.localPath != null) {
            return this.localPath;
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getName()
     */
    public String getName() {
        if (fileSpec != null) {
            String name = fileSpec.getDepotPathString();
            if (name == null) {
                name = fileSpec.getOriginalPathString();
            }
            if (name == null) {
                name = fileSpec.getPreferredPathString();
            }
            if (name != null) {
                int lastSlash = name.lastIndexOf('/');
                if (lastSlash != -1 && lastSlash + 1 < name.length()) {
                    return name.substring(lastSlash + 1);
                }
            }
        }
        if (this.localPath != null) {
            int lastSlash = this.localPath.lastIndexOf(File.separatorChar);
            if (lastSlash != -1 && lastSlash + 1 < this.localPath.length()) {
                return this.localPath.substring(lastSlash + 1);
            }
        }
        return this.localPath;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getRemotePath()
     */
    public String getRemotePath() {
        if (this.fileSpec != null) {
            return this.fileSpec.getDepotPathString();
        }
        return null;
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
        if (this.parent != null) {
            return this.parent.getConnection();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath()
     */
    public String getActionPath() {
        String path = getRemotePath();
        if (path == null) {
            path = getLocalPath();
        }
        // Last chance is the path field from the file spec object
        if (path == null && this.fileSpec != null) {
            path = this.fileSpec.getOriginalPathString();
        }
        return path;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath(com.perforce.team.core.p4java.IP4Resource.Type)
     */
    public String getActionPath(Type type) {
        String path = null;
        if (type == Type.REMOTE) {
            path = getRemotePath();
        } else if (type == Type.LOCAL) {
            path = getLocalPath();
        }
        if (path == null) {
            path = getActionPath();
        }
        return path;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHistory(boolean)
     */
    public IFileRevisionData[] getHistory(final boolean displayBranching) {
        checkSpec();
        final List<IFileRevisionData> fileHistory = new ArrayList<IFileRevisionData>();
        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                String path = getActionPath();
                if (path != null) {
                    List<IFileSpec> specs = P4FileSpecBuilder
                            .makeFileSpecList(path);
                    Map<IFileSpec, List<IFileRevisionData>> history = server
                            .getRevisionHistory(specs, 0, false,
                                    displayBranching, true, false);
                    if (history != null) {
                        for (List<IFileRevisionData> data : history.values()) {
                            if (data != null) {
                                fileHistory.addAll(data);
                            }
                        }
                    }
                }
            }
        };
        runOperation(operation);
        return fileHistory.toArray(new IFileRevisionData[fileHistory.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        if (this.fileSpec != null) {
            return this.fileSpec.getClientPathString();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isContainer()
     */
    public boolean isContainer() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadType()
     */
    public String getHeadType() {
        if (this.fileSpec instanceof IExtendedFileSpec) {
            return ((IExtendedFileSpec) this.fileSpec).getHeadType();
        } else if (this.fileSpec != null) {
            return this.fileSpec.getFileType();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getOpenedType()
     */
    public String getOpenedType() {
        String type = null;
        if (this.fileSpec != null) {
            if (this.fileSpec instanceof IExtendedFileSpec) {
                type = ((IExtendedFileSpec) this.fileSpec).getOpenType();
            }
            if (type == null) {
                type = this.fileSpec.getFileType();
            }
        }
        return type;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAction()
     */
    public FileAction getAction() {
        if (this.fileSpec != null) {
            return this.fileSpec.getAction();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadAction()
     */
    public FileAction getHeadAction() {
        if (this.fileSpec instanceof IExtendedFileSpec) {
            return ((IExtendedFileSpec) this.fileSpec).getHeadAction();
        }
        return null;
    }

    private void refreshFile() throws P4JavaException {
        this.integSpecs = null;
        IClient client = getClient();

        if (client != null) {
            try {
                List<IFileSpec> actionSpecs = getActionSpec();
                if (actionSpecs != null && !actionSpecs.isEmpty()) {
                    List<IExtendedFileSpec> specs = client.getServer()
                            .getExtendedFiles(getActionSpec(), 0, -1, -1, null,
                                    null);
                    if (specs.size() == 1) {
                        setFileSpec(specs.get(0));
                    }
                } else {
                    this.fileSpec = null;
                }
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {
        if (readOnly) {
            return;
        }
        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                refreshFile();
            } catch (P4JavaException e) {
                retry = handleError(e);
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isSynced()
     */
    public boolean isSynced() {
        return (getHeadRevision() > 0 || getHaveRevision() > 0)
                && getHeadRevision() == getHaveRevision();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getChangelist()
     */
    public IP4PendingChangelist getChangelist() {
        return getChangelist(false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getChangelist(boolean)
     */
    public IP4PendingChangelist getChangelist(boolean fetchIfNotFound) {
        return getChangelist(fetchIfNotFound, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getChangelist(boolean,
     *      boolean)
     */
    public IP4PendingChangelist getChangelist(boolean fetchIfNotFound,
            boolean ignoreErrors) {
        IP4PendingChangelist list = null;
        IP4Connection connection = getConnection();
        if (connection != null && this.fileSpec != null && isOpened()) {
            list = connection.getPendingChangelist(
                    this.fileSpec.getChangelistId(), fetchIfNotFound,
                    ignoreErrors);
        }
        return list;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isOpened()
     */
    public boolean isOpened() {
        return this.fileSpec != null && this.fileSpec.getAction() != null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getRemoteContents()
     */
    public InputStream getRemoteContents() {
        if (this.fileSpec != null) {
            boolean retry = true;
            while (retry) {
                retry = false;
                try {
                    return this.fileSpec.getContents(true);
                } catch (P4JavaException e) {
                    retry = handleError(e);
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadContents()
     */
    public InputStream getHeadContents() {
        return getRemoteContents(getHeadRevision());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHaveContents()
     */
    public InputStream getHaveContents() {
        return getRemoteContents(getHaveRevision());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getRemoteContents(int)
     */
    public InputStream getRemoteContents(int revision) {
    	InputStream stream = getRemoteContents("#" + Integer.toString(revision)); //$NON-NLS-1$
    	return stream;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getRemoteContents(java.lang.String)
     */
    public InputStream getRemoteContents(String revision) {
        final InputStream[] stream = new InputStream[] { null };
        if (this.fileSpec != null) {
            String path = this.fileSpec.getDepotPathString();
            if (path == null) {
                path = this.fileSpec.getOriginalPathString();
            }
            path += revision;

            final List<IFileSpec> specs = P4FileSpecBuilder
                    .makeFileSpecList(new String[] { path });
            IP4ServerOperation operation = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    stream[0] = server.getFileContents(specs, false, true);
                }
            };
            runOperation(operation);
        }
        return stream[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh(int)
     */
    public void refresh(int depth) {
        refresh();
    }

    private void updateChangelists(int previousChangelist, int newChangelist,
            P4Collection refreshEventContainer) {
        IP4Connection connection = getConnection();
        if (connection != null) {
            if (previousChangelist > -1 && previousChangelist != newChangelist) {
                IP4PendingChangelist oldList = connection.getPendingChangelist(
                        previousChangelist, false, true);
                if (oldList != null && oldList.removeFile(this)) {
                    // Send refresh event for old changelist if file has
                    // switched changelists.
                    if (refreshEventContainer != null) {
                        refreshEventContainer.add(oldList);
                    } else {
                        P4Workspace.getWorkspace().notifyListeners(
                                new P4Event(EventType.REFRESHED, oldList));
                    }
                }
            }
            if (newChangelist > -1 && newChangelist!=previousChangelist) {
                IP4PendingChangelist newList = connection.getPendingChangelist(
                        newChangelist, isOpened(), true);
                if (newList != null) {
                    newList.addFile(this);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#setFileSpec(com.perforce.p4java.core.file.IFileSpec)
     */
    public void setFileSpec(IFileSpec spec) {
        setFileSpec(spec, null, true);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#setFileSpec(com.perforce.p4java.core.file.IFileSpec,
     *      com.perforce.team.core.p4java.P4Collection)
     */
    public void setFileSpec(IFileSpec spec, final P4Collection refreshEventContainer, final boolean updateChangelist) {
        final int previousChangelist = getChangelistId();
        if (spec != null && FileSpecOpStatus.VALID == spec.getOpStatus()) {
            this.fileSpec = spec;
            int newChangelist = getChangelistId();
            updateServer();
            this.localPath = getLocalResourcePath(this.fileSpec
                    .getClientPathString());
            if (this.fileSpec.getDepotPath() != null
                    && this.parent instanceof IP4Connection) {
                IP4Connection connection = getConnection();
                if (connection != null) {
                    String depot = this.fileSpec.getDepotPathString();
                    int lastSlash = depot.lastIndexOf('/');
                    if (lastSlash >= 0) {
                        String parentPath = depot.substring(0, lastSlash);
                        this.parent = connection.getFolder(parentPath, false);
                    }
                }
            }
            notifyListeners(new P4Event(EventType.REFRESHED, this));
            IP4Connection connection = getConnection();
            if (connection != null) {
                connection.updateResource(this);
                // Update changelists association
                if(updateChangelist){
	                updateChangelists(previousChangelist, newChangelist,
	                        refreshEventContainer);
                }
            }
        } else if (this.fileSpec != null) {
			Tracing.printExecTime(() -> {
				P4File.this.fileSpec = null;
				int newChangelist = getChangelistId();
				if (updateChangelist) {
					updateChangelists(previousChangelist, newChangelist, refreshEventContainer);
				}
				IP4Connection connection = getConnection();
				if (connection != null) {
					connection.removeFileFromChangelists(P4File.this);
				}
				notifyListeners(new P4Event(EventType.REFRESHED, P4File.this));
			}, "P4File", "setFileSpec ");
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#setParent(com.perforce.team.core.p4java.IP4Container)
     */
    public void setParent(IP4Container parent) {
        this.parent = parent;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getLocalFiles()
     */
    public IFile[] getLocalFiles() {
        return PerforceProviderPlugin.getLocalFiles(getLocalPath());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getLocalFileForLocation()
     */
    public IFile getLocalFileForLocation() {
        return PerforceProviderPlugin.getLocalFile(getLocalPath());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#move(com.perforce.team.core.p4java.IP4File,
     *      boolean)
     */
    public boolean move(IP4File toFile, boolean useMoveCommand) {
        return move(toFile, useMoveCommand, IChangelist.DEFAULT);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#move(com.perforce.team.core.p4java.IP4File,
     *      boolean, int)
     */
    public boolean move(IP4File toFile, boolean useMoveCommand, int changelist) {
        return move(toFile, useMoveCommand, changelist, false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#move(com.perforce.team.core.p4java.IP4File,
     *      boolean, int, boolean)
     */
    public boolean move(IP4File toFile, boolean useMoveCommand, int changelist,
            boolean bypassClient) {
        ChangelistSelection selection = new ChangelistSelection(changelist,
                true);
        return move(toFile, useMoveCommand, selection, bypassClient);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#move(com.perforce.team.core.p4java.IP4File,
     *      boolean, com.perforce.team.core.p4java.ChangelistSelection, boolean)
     */
    public boolean move(IP4File toFile, boolean useMoveCommand,
            ChangelistSelection selection, boolean bypassClient) {
        if (readOnly) {
            return false;
        }
        if (toFile != null && selection != null) {
            IClient client = getClient();
            IP4Connection connection = getConnection();
            if (client != null && connection != null) {
                if (!useMoveCommand) {
                    return classicMove(client, toFile, selection.getId(),
                            bypassClient);
                } else {
                    return newMove(client, toFile, selection, bypassClient);
                }
            }
        }
        return false;
    }

    private boolean classicMove(IClient client, IP4File toFile, int changelist,
            boolean bypassClient) {
        String newPath = toFile.getLocalPath();
        // Integrate from old path to new path
        IFileSpec currentSpec = this.fileSpec;
        if (currentSpec != null && newPath != null) {
            IFileSpec toSpec = new FileSpec(newPath);
            try {
                IntegrationOptions options = new IntegrationOptions();
                options.setDontCopyToClient(bypassClient);
                client.integrateFiles(changelist, false, options, null,
                        currentSpec, toSpec);

                P4Collection toCollection = new P4Collection(
                        new IP4Resource[] { toFile });
                toCollection.add(changelist);

                P4Collection fromCollection = new P4Collection(
                        new IP4Resource[] { this });
                fromCollection.revert();
                fromCollection.delete(changelist);
                return true;
            } catch (P4JavaException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return false;
    }

    private boolean newMove(IClient client, IP4File toFile,
            ChangelistSelection selection, boolean bypassClient) {
        String newPath = toFile.getLocalPath();
        IServer server = client.getServer();
        IFileSpec currentSpec = this.fileSpec;
        if (currentSpec != null && newPath != null && server != null) {
            IFileSpec toSpec = new FileSpec(newPath);
            try {
                P4Collection fromCollection = new P4Collection(
                        new IP4Resource[] { this });
                fromCollection.edit();

                // Store move from file path in case this is not the first move
                // that this file has been through, the file at this path will
                // be added to the refresh collection after the move is done.
                // Fix for job037932.
                String fromFilePath = getMovedFile();

                int id = selection.isExplicitSelection()
                        ? selection.getId()
                        : IChangelist.UNKNOWN;
                List<IFileSpec> outputSpecs = server.moveFile(id, false,
                        bypassClient, null, currentSpec, toSpec);
                outputSpecs = P4FileSpecBuilder.getValidFileSpecs(outputSpecs);

                if (outputSpecs.size() == 1) {
                    P4Collection refreshCollection = new P4Collection();
                    if (fromFilePath != null) {
                        IP4Connection connection = getConnection();
                        if (connection != null) {
                            IP4File fromFile = connection.getFile(fromFilePath);
                            refreshCollection.add(fromFile);
                        }
                    }
                    refreshCollection.add(this);
                    refreshCollection.add(toFile);
                    refreshCollection.refresh();
                    return true;
                }

            } catch (P4JavaException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getResolvePath()
     */
    public String getResolvePath() {
        String resolvePath = null;
        if (integSpecs != null && integSpecs.length != 0) {
            IFileSpec integSpec = integSpecs[0];
            resolvePath = integSpec.getFromFile();
            if (resolvePath != null) {
                int start = integSpec.getStartFromRev();
                int end = integSpec.getEndFromRev();
                if (start > -1 && end > -1) {
                    resolvePath += "#" + (start + 1) + ",#" + end; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        return resolvePath;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#setIntegrationSpec(com.perforce.p4java.core.file.IFileSpec)
     */
    public void setIntegrationSpecs(IFileSpec[] integSpecs) {
        if (integSpecs != null && integSpecs.length != 0) {
            if (integSpecs[0].getOpStatus() == FileSpecOpStatus.VALID) {
                this.integSpecs = integSpecs;
            }
        } else {
            this.integSpecs = null;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isUnresolved()
     */
    public boolean isUnresolved() {
        if (this.fileSpec instanceof IExtendedFileSpec) {
            return ((IExtendedFileSpec) this.fileSpec).isUnresolved();
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getIntegrationSpec()
     */
    public IFileSpec getIntegrationSpec() {
        if (this.integSpecs == null)
            return null;
        return this.integSpecs[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getIntegrationSpec()
     */
    public IFileSpec[] getIntegrationSpecs() {
        return this.integSpecs;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter == IResource.class) {
            return getLocalFileForLocation();
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadChange()
     */
    public int getHeadChange() {
        int headChange = 0;
        if (this.fileSpec instanceof IExtendedFileSpec) {
            headChange = ((IExtendedFileSpec) this.fileSpec).getHeadChange();
        }
        return headChange;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getHeadTime()
     */
    public long getHeadTime() {
        long headTime = 0l;
        if (this.fileSpec instanceof IExtendedFileSpec) {
            Date date = ((IExtendedFileSpec) this.fileSpec).getHeadTime();
            if (date != null) {
                headTime = date.getTime();
            }
        }
        return headTime;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getClientName()
     */
    public String getClientName() {
        if (this.fileSpec != null) {
            return this.fileSpec.getClientName();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getUserName()
     */
    public String getUserName() {
        if (this.fileSpec != null) {
            return this.fileSpec.getUserName();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getChangelistId()
     */
    public int getChangelistId() {
        int changelist = IChangelist.UNKNOWN;
        if (this.fileSpec != null) {
            changelist = this.fileSpec.getChangelistId();
        }
        return changelist;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getStatus()
     */
    public FileSpecOpStatus getStatus() {
        FileSpecOpStatus status = FileSpecOpStatus.UNKNOWN;
        if (this.fileSpec != null) {
            status = this.fileSpec.getOpStatus();
        }
        return status;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getStatusMessage()
     */
    public String getStatusMessage() {
        String message = null;
        if (this.fileSpec != null) {
            message = this.fileSpec.getStatusMessage();
        }
        return message;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isLocked()
     */
    public boolean isLocked() {
        boolean locked = false;
        if (this.fileSpec != null) {
            locked = this.fileSpec.isLocked();
        }
        return locked;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedElsewhere()
     */
    public boolean openedElsewhere() {
        boolean elsewhere = false;
        if (this.fileSpec instanceof IExtendedFileSpec) {
            List<String> otherActionList = ((IExtendedFileSpec) this.fileSpec)
                    .getOtherActionList();
            elsewhere = otherActionList != null && !otherActionList.isEmpty();
        }
        return elsewhere;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getOtherActions()
     */
    public List<String> getOtherActions() {
        List<String> others = null;
        if (this.fileSpec instanceof IExtendedFileSpec) {
            others = ((IExtendedFileSpec) this.fileSpec).getOtherActionList();
        }
        if (others == null) {
            others = new ArrayList<String>();
        }
        return others;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getOtherChangelists()
     */
    public List<String> getOtherChangelists() {
        List<String> others = null;
        if (this.fileSpec instanceof IExtendedFileSpec) {
            others = ((IExtendedFileSpec) this.fileSpec).getOtherChangelist();
        }
        if (others == null) {
            others = new ArrayList<String>();
        }
        return others;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getOtherEditors()
     */
    public List<String> getOtherEditors() {
        List<String> others = null;
        if (this.fileSpec instanceof IExtendedFileSpec) {
            others = ((IExtendedFileSpec) this.fileSpec).getOtherOpenList();
        }
        if (others == null) {
            others = new ArrayList<String>();
        }
        return others;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedByOwner()
     */
    public boolean openedByOwner() {
        boolean owned = false;
        IP4Connection connection = getConnection();
        if (connection != null) {
            String owner = connection.getParameters().getUserNoNull();
            // Check username field as this will be set via an opened command
            // If that field is unset use the action owner field if present
            String username = getUserName();
            if (username != null) {
                if (isCaseSensitive()) {
                    owned = owner.equals(username);
                } else {
                    owned = owner.equalsIgnoreCase(username);
                }
            } else if (this.fileSpec instanceof IExtendedFileSpec) {
                String specOwner = ((IExtendedFileSpec) this.fileSpec)
                        .getActionOwner();
                if (isCaseSensitive()) {
                    owned = owner.equals(specOwner);
                } else {
                    owned = owner.equalsIgnoreCase(specOwner);
                }
            }
        }
        return owned;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#openedByOtherOwner()
     */
    public boolean openedByOtherOwner() {
        boolean otherOwned = false;
        IP4Connection connection = getConnection();
        if (connection != null) {
            String owner = connection.getParameters().getUserNoNull();
            // Check username field as this will be set via an opened command
            // If that field is unset use the action owner field if present
            String username = getUserName();
            if (username != null) {
                if (isCaseSensitive()) {
                    otherOwned = !owner.equals(username);
                } else {
                    otherOwned = !owner.equalsIgnoreCase(username);
                }
            } else if (this.fileSpec instanceof IExtendedFileSpec) {
                String specOwner = ((IExtendedFileSpec) this.fileSpec)
                        .getActionOwner();
                if (isCaseSensitive()) {
                    otherOwned = !owner.equals(specOwner);
                } else {
                    otherOwned = !owner.equalsIgnoreCase(specOwner);
                }
            }
        }
        return otherOwned;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isHeadActionAdd()
     */
    public boolean isHeadActionAdd() {
        return isActionAdd(getHeadAction());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isHeadActionDelete()
     */
    public boolean isHeadActionDelete() {
        return isActionDelete(getHeadAction());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#isHeadActionEdit()
     */
    public boolean isHeadActionEdit() {
        return isActionEdit(getHeadAction());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getCompleteHistory(boolean,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public IFileRevision[] getCompleteHistory(boolean includeBranches,
            IProgressMonitor monitor) {
        List<IFileRevision> revisions = new ArrayList<IFileRevision>();

        monitor.setTaskName(MessageFormat.format(Messages.P4File_3, getName()));
        IFileRevisionData[] data = getHistory(includeBranches);
        for (IFileRevisionData rev : data) {
            revisions.add(new P4Revision(getConnection(), rev));
        }
        monitor.worked(1);

        monitor.setTaskName(MessageFormat.format(Messages.P4File_4, getName()));
        IFile localFile = getLocalFileForLocation();
        if (localFile != null) {
            try {
                IFileState[] states = localFile.getHistory(monitor);
                revisions.add(new LocalRevision(localFile, null));
                for (IFileState state : states) {
                    revisions.add(new LocalRevision(localFile, state));
                }
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            } catch (OperationCanceledException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        monitor.worked(1);

        return revisions.toArray(new IFileRevision[revisions.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAnnotations(boolean)
     */
    public IFileAnnotation[] getAnnotations(boolean followBranches) {
        return getAnnotations(followBranches, null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAnnotations()
     */
    public IFileAnnotation[] getAnnotations() {
        return getAnnotations(false);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAnnotations(boolean,
     *      com.perforce.team.core.p4java.IP4File.WhitespaceIgnoreType)
     */
    public IFileAnnotation[] getAnnotations(boolean followBranches,
            WhitespaceIgnoreType ignoreType) {
        return getAnnotations(followBranches, ignoreType, true);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getAnnotations(boolean,
     *      com.perforce.team.core.p4java.IP4File.WhitespaceIgnoreType, boolean)
     */
    public IFileAnnotation[] getAnnotations(final boolean followBranches,
            final WhitespaceIgnoreType ignoreType,
            final boolean outputChangeNumbers) {
        final List<IFileAnnotation> annotations = new ArrayList<IFileAnnotation>();
        String path = getActionPath();
        if (path != null) {
            final List<IFileSpec> specs = P4FileSpecBuilder
                    .makeFileSpecList(new String[] { path });
            IP4ServerOperation operation = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    DiffType diffType = null;
                    if (ignoreType != null) {
                        switch (ignoreType) {
                        case ALL:
                            diffType = DiffType.IGNORE_WS;
                            break;
                        case LINE_ENDINGS:
                            diffType = DiffType.IGNORE_LINE_ENDINGS;
                            break;
                        case WHITESPACE:
                            diffType = DiffType.IGNORE_WS_CHANGES;
                            break;
                        default:
                            break;
                        }
                    }
                    annotations.addAll(server
                            .getFileAnnotations(specs, diffType, true,
                                    outputChangeNumbers, followBranches));
                }

            };
            runOperation(operation);
        }
        return annotations.toArray(new IFileAnnotation[annotations.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getShelvedVersions()
     */
    public IP4ShelveFile[] getShelvedVersions() {
        final List<IP4ShelveFile> shelved = new ArrayList<IP4ShelveFile>();
        String path = getActionPath();
        final IP4Connection connection = getConnection();
        if (path != null && connection != null) {
            final List<IFileSpec> specs = P4FileSpecBuilder
                    .makeFileSpecList(new String[] { path });
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    if (connection.isShelvingSupported()) {
                        IServer server = client.getServer();
                        List<IChangelistSummary> lists = server.getChangelists(
                                -1, specs, null, null, false,
                                IChangelist.Type.SHELVED, true);
                        for (IChangelistSummary list : lists) {
                            if (list.getId() > 0) {
                                IChangelist fullList = new Changelist(list,
                                        server, false);
                                shelved.add(new P4ShelveFile(fullList,
                                        P4File.this, true));
                            }
                        }
                    }
                }
            };
            runOperation(operation);
        }
        return shelved.toArray(new IP4ShelveFile[shelved.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#getMovedFile()
     */
    public String getMovedFile() {
        if (this.fileSpec instanceof IExtendedFileSpec) {
            return ((IExtendedFileSpec) this.fileSpec).getMovedFile();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#isFile()
     */
    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4File#toFile()
     */
    public File toFile() {
        String local = getLocalPath();
        File file = null;
        if (local != null) {
            file = new File(local);
        }
        return file;
    }

}
