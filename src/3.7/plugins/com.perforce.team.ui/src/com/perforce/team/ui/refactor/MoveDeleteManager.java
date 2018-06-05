package com.perforce.team.ui.refactor;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Move delete hook manager
 * 
 */
public class MoveDeleteManager implements IMoveDeleteHook {

    private static boolean prefMove() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_REFACTOR_USE_MOVE);
    }

    private IDeleteManager deleteManager = new DeleteManager();
    private IMoveManager integManager = new IntegManager();
    private IMoveManager move92Manager = new Move92Manager();
    private IMoveManager move91Manager = new Move91Manager();

    /**
     * @see org.eclipse.core.resources.team.IMoveDeleteHook#deleteFile(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFile, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags,
            IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        return deleteManager.delete(tree, file, updateFlags, monitor);
    }

    /**
     * @see org.eclipse.core.resources.team.IMoveDeleteHook#deleteFolder(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFolder, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean deleteFolder(IResourceTree tree, IFolder folder,
            int updateFlags, IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        return deleteManager.delete(tree, folder, updateFlags, monitor);
    }

    /**
     * @see org.eclipse.core.resources.team.IMoveDeleteHook#deleteProject(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IProject, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean deleteProject(IResourceTree tree, IProject project,
            int updateFlags, IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        return deleteManager.delete(tree, project, updateFlags, monitor);
    }

    private IMoveManager getMoveManager(IResource resource) {
        IMoveManager manager = integManager;
        if (prefMove()) {
            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(resource.getProject());
            if (connection != null) {
                IServer server = connection.getServer();
                if (server != null) {
                    try {
                        if (server.supportsSmartMove()) {
                            if (connection.isMoveServerOnlySupported()) {
                                manager = move92Manager;
                            } else {
                                manager = move91Manager;
                            }
                        }
                    } catch (P4JavaException e) {
                        // Ignore and use default
                        manager = integManager;
                    }
                }
            }
        }
        return manager;
    }

    /**
     * @see org.eclipse.core.resources.team.IMoveDeleteHook#moveFile(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean moveFile(final IResourceTree tree, final IFile source,
            final IFile destination, final int updateFlags,
            IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        IMoveManager manager = getMoveManager(source);
        return manager.move(tree, source, destination, updateFlags, monitor);
    }

    /**
     * @see org.eclipse.core.resources.team.IMoveDeleteHook#moveFolder(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFolder,
     *      org.eclipse.core.resources.IFolder, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean moveFolder(final IResourceTree tree, final IFolder source,
            final IFolder destination, final int updateFlags,
            IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        IMoveManager manager = getMoveManager(source);
        return manager.move(tree, source, destination, updateFlags, monitor);
    }

    /**
     * @see org.eclipse.core.resources.team.IMoveDeleteHook#moveProject(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IProject,
     *      org.eclipse.core.resources.IProjectDescription, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean moveProject(IResourceTree tree, IProject source,
            IProjectDescription description, int updateFlags,
            IProgressMonitor monitor) {
        IMoveManager manager = getMoveManager(source);
        return manager.move(tree, source, description, updateFlags, monitor);
    }
}
