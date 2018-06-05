/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.FileStatOutputOptions;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.option.client.SyncOptions;
import com.perforce.p4java.server.CmdSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;

/**
 * Base class for a p4 container that may be a depot or a folder or another type
 * of collection.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4Container extends P4Resource implements IP4Container {

    /**
     * Currently cached members
     */
    protected IP4Resource[] cachedMembers = null;

    /**
     * Convert a list of specs to end with the standard ellipsis (/...)
     * 
     * @param specs
     * @return - list of file specs that end with ellipsis
     */
    protected List<IFileSpec> convertToEllipsisSpecs(List<IFileSpec> specs) {
        List<IFileSpec> converted = new ArrayList<IFileSpec>();
        for (IFileSpec spec : specs) {
            String path = spec.getPreferredPathString();
            if (path != null) {
                path += IP4Container.REMOTE_ELLIPSIS;
                converted.addAll(P4FileSpecBuilder
                        .makeFileSpecList(new String[] { path }));
            }
        }
        return converted;
    }

    /**
     * Remove the ellipsis from the specified path
     * 
     * @param path
     * @return - path without ellipsis
     */
    protected String convertFromEllipsis(String path) {
        String converted = path;
        if (path != null) {
            if (path.endsWith(IP4Container.REMOTE_ELLIPSIS)) {
                converted = path.substring(0,
                        path.lastIndexOf(IP4Container.REMOTE_ELLIPSIS));
            } else if (path.endsWith(IP4Container.DIR_ELLIPSIS)) {
                converted = path.substring(0,
                        path.lastIndexOf(IP4Container.DIR_ELLIPSIS));
            }
        }
        return converted;
    }

    /**
     * Get a list of mapped file specs
     * 
     * @param specs
     * @return - list with unmapped file specs removed
     */
    protected List<IFileSpec> filterUnmapped(List<IFileSpec> specs) {
        List<IFileSpec> mapped = new ArrayList<IFileSpec>();
        for (IFileSpec spec : specs) {
            if (!spec.isUnmap()) {
                mapped.add(spec);
            }
        }
        return mapped;
    }

    /**
     * Gets the child directories of this p4 container. This method does a dirs.
     * 
     * @param client
     * 
     * @param all
     * 
     * @return - array of p4 containers
     * @throws P4JavaException
     */
    protected List<IP4Container> getDirectories(IClient client, boolean all)
            throws P4JavaException {
        List<IP4Container> newResources = new ArrayList<IP4Container>();
        IP4Connection connection = getConnection();
        String path = getRemotePath();
        if (path != null) {
            boolean clientOnly = connection != null
                    && connection.showClientOnly();
            boolean includeDeletes = connection != null
                    && connection.showFoldersWithOnlyDeletedFiles();
            List<IFileSpec> dirs = client
                    .getServer()
                    .getDirectories(
                            P4FileSpecBuilder.makeFileSpecList(new String[] { path
                                    + "/*", }), //$NON-NLS-1$
                            clientOnly, includeDeletes, false);
            dirs = P4FileSpecBuilder.getValidFileSpecs(dirs);
            if (dirs.size() > 0) {
                for (int i = 0; i < dirs.size(); i++) {
                    IFileSpec dir = dirs.get(i);
                    String dirName = dir.getOriginalPathString();
                    if (dirName != null) {
                        dirName = dirName
                                .substring(dirName.lastIndexOf('/') + 1);
                        IP4Folder folder = new P4Folder(this, dirName);
                        if (connection != null) {
                            connection.updateResource(folder);
                        }
                        if (all) {
                            folder.refresh(IResource.DEPTH_INFINITE);
                        }
                        newResources.add(folder);
                    }
                }
            }
        }
        return newResources;
    }

    /**
     * Gets the child files of this p4 container. This method does a stat.
     * 
     * @param client
     * 
     * @return - array of p4 files
     * @throws P4JavaException
     */
    protected List<IP4File> getFiles(IClient client) throws P4JavaException {
        List<IP4File> newResources = new ArrayList<IP4File>();
        IP4Connection connection = getConnection();
        if (connection != null) {
            String path = getRemotePath();
            if (path == null) {
                path = getLocalPath();
            }
            if (path != null) {
                path += "/*"; //$NON-NLS-1$
//TODO change to p4 fstat -Rc //depot/dev/*                
                FileStatOutputOptions outputOptions = null;
                if (connection.showClientOnly()) {
                    outputOptions = new FileStatOutputOptions();
                    outputOptions.setMappedFiles(true);
                }

                List<IExtendedFileSpec> files = client
                        .getServer()
                        .getExtendedFiles(
                                P4FileSpecBuilder
                                        .makeFileSpecList(new String[] { path }),
                                0, -1, -1, outputOptions, null);

                List<IExtendedFileSpec> validFileSpecs=new ArrayList<IExtendedFileSpec>();
                for (IExtendedFileSpec file : files) {
                    if (FileSpecOpStatus.VALID.equals(file.getOpStatus())) {
                        if (file instanceof FileSpec) {
                            ((FileSpec) file).setClient(getClient());
                        }
                        validFileSpecs.add(file);
                    }
                }
                IP4File[] validFiles = connection.getFiles(validFileSpecs.toArray(new IFileSpec[0]));
                newResources.addAll(Arrays.asList(validFiles));

//                for (IExtendedFileSpec file : files) {
//                    if (FileSpecOpStatus.VALID.equals(file.getOpStatus())) {
//                        if (file instanceof FileSpec) {
//                            ((FileSpec) file).setClient(getClient());
//                        }
//                        IP4File p4File = connection.getFile(file); //TODO: batch update the files 
//                        if (p4File == null) {
//                            p4File = new P4File(file, this);
//                        }
//                        p4File.setFileSpec(file);
//                        newResources.add(p4File);
//                    }
//                }
            }
        }
        return newResources;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#getAllLocalFiles()
     */
    public IP4File[] getAllLocalFiles() {
        IP4File[] resources = new IP4File[0];
        IClient client = getClient();
        if (client != null) {
            String path = getRemotePath();
            if (path == null) {
                path = getLocalPath();
            }
            if (path != null) {
                path += "/..."; //$NON-NLS-1$
                List<IP4Resource> newResources = new ArrayList<IP4Resource>();
                try {
                    List<IExtendedFileSpec> files = client
                            .getServer()
                            .getExtendedFiles(
                                    P4FileSpecBuilder
                                            .makeFileSpecList(new String[] { path }),
                                    0, -1, -1, null, null);
                    for (IExtendedFileSpec file : files) {
                        if (FileSpecOpStatus.VALID.equals(file.getOpStatus())) {
                            if (file instanceof FileSpec) {
                                ((FileSpec) file).setClient(getClient());
                            }
                            IP4File p4File = null;
                            String local = P4Resource.normalizeLocalPath(file);
                            if (local != null) {
                                IP4Resource p4Resource = getConnection()
                                        .getResource(local);
                                if (p4Resource instanceof IP4File) {
                                    p4File = (IP4File) p4Resource;
                                    p4File.setFileSpec(file);
                                    newResources.add(p4File);
                                } else {
                                    p4File = new P4File(getConnection(), local);
                                    p4File.setFileSpec(file);
                                    newResources.add(p4File);
                                    getConnection().updateResource(p4File);
                                }
                            }
                        }
                    }
                    resources = newResources.toArray(new IP4File[0]);
                } catch (P4JavaException e1) {
                    PerforceProviderPlugin.logError(e1);
                }

            }
        }
        return resources;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#members()
     */
    public IP4Resource[] members() {
        if (this.needsRefresh && this.cachedMembers == null) {
            refresh();
        }
        return this.cachedMembers != null
                ? this.cachedMembers
                : new IP4Resource[0];
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#size()
     */
    public int size() {
        return this.cachedMembers != null ? this.cachedMembers.length : 0;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath()
     */
    public String getActionPath() {
        String path = getRemoteActionPath();
        if (path == null) {
            path = getLocalActionPath();
        }
        return path;
    }

    private String getRemoteActionPath() {
        String path = getRemotePath();
        if (path != null) {
            path += "/..."; //$NON-NLS-1$
        }
        return path;
    }

    private String getLocalActionPath() {
        String path = getLocalPath();
        if (path != null) {
            path += IP4Container.DIR_ELLIPSIS;
        }
        return path;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath(com.perforce.team.core.p4java.IP4Resource.Type)
     */
    public String getActionPath(Type type) {
        String path = null;
        if (type == Type.REMOTE) {
            path = getRemoteActionPath();
        } else if (type == Type.LOCAL) {
            path = getLocalActionPath();
        }
        if (path == null) {
            path = getActionPath();
        }
        return path;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#sync()
     */
    @Override
    public void sync(final IProgressMonitor monitor) {
        IP4ClientOperation operation = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
            	P4ProgressListener handler = P4CoreUtils.createStreamCallback(getConnection(), CmdSpec.SYNC, monitor);
            	SyncOptions syncOpts = new SyncOptions();
            	syncOpts.setForceUpdate(false);
            	syncOpts.setNoUpdate(false);
            	syncOpts.setClientBypass(false);
            	syncOpts.setServerBypass(false);
            	client.sync(P4FileSpecBuilder
                        .makeFileSpecList(new String[] { getActionPath() }), syncOpts, handler, P4CoreUtils.getRandomInt());
//                client.sync(P4FileSpecBuilder
//                        .makeFileSpecList(new String[] { getActionPath() }),
//                        false, false, false, false);
            }
        };
        runOperation(operation);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isContainer()
     */
    public boolean isContainer() {
        return true;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {
        refresh(IResource.DEPTH_ONE);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh(int)
     */
    public void refresh(final int depth) {
        final List<IP4Container> dirs = new ArrayList<IP4Container>();

        IP4ClientOperation dirsOp = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                if (depth == IResource.DEPTH_ONE) {
                    dirs.addAll(getDirectories(client, false));
                } else if (depth == IResource.DEPTH_INFINITE) {
                    dirs.addAll(getDirectories(client, true));
                }
            }
        };
        runOperation(dirsOp);

        final List<IP4File> files = new ArrayList<IP4File>();
        IP4ClientOperation filesOp = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                files.addAll(getFiles(client));
            }
        };
        runOperation(filesOp);

        if (files.size() > 0) {
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.REFRESHED, new P4Collection(files
                            .toArray(new IP4File[0]))));
        }

        List<IP4Resource> resources = new ArrayList<IP4Resource>();
        resources.addAll(dirs);
        resources.addAll(files);
        this.cachedMembers = resources.toArray(new IP4Resource[0]);
        needsRefresh = false;
    }

}
