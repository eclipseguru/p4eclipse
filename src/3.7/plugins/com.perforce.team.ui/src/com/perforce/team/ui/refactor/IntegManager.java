/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ChangelistSelection;
import com.perforce.team.core.p4java.IP4File;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class IntegManager extends MoveManager implements IMoveManager {

    /**
     * Create an integ-based (pre-2009.1) move manager
     */
    public IntegManager() {
        super(false, false);
    }

    /**
     * @see com.perforce.team.ui.refactor.MoveManager#moveFile(com.perforce.team.core.p4java.IP4File,
     *      com.perforce.team.core.p4java.IP4File,
     *      com.perforce.team.core.p4java.ChangelistSelection)
     */
    @Override
    protected boolean moveFile(IP4File fromFile, IP4File toFile,
            ChangelistSelection changelist) {
        return fromFile.move(toFile, false, changelist, true);
    }

    /**
     * @see com.perforce.team.ui.refactor.MoveManager#preUpdateTree(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFolder,
     *      org.eclipse.core.resources.IFolder, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void preUpdateTree(IResourceTree tree, IFolder source,
            IFolder destination, int flags, IProgressMonitor monitor) {
        tree.standardMoveFolder(source, destination, flags, monitor);
    }

    /**
     * @see com.perforce.team.ui.refactor.MoveManager#preUpdateTree(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void preUpdateTree(IResourceTree tree, IFile source,
            IFile destination, int flags, IProgressMonitor monitor) {

        // Fix for job037198, resource attributes should set source file to be
        // not read-only since if java.io.File.renameTo fails then a copy/delete
        // will be done by the Eclipse filesystem and that will check this
        // attribute and delete of source file will fails if the file is
        // read-only
        if (P4CoreUtils.isMac()) {
            ResourceAttributes attributes = source.getResourceAttributes();
            if (attributes != null && attributes.isReadOnly()) {
                attributes.setReadOnly(false);
                try {
                    source.setResourceAttributes(attributes);
                } catch (CoreException e) {
                    // Ignore error since refactoring will fail when delete is
                    // attempted of source file
                }
            }
        }

        tree.standardMoveFile(source, destination, flags, monitor);
    }

    /**
     * @see com.perforce.team.ui.refactor.MoveManager#moveFolder(org.eclipse.core.resources.IFolder,
     *      org.eclipse.core.resources.IFolder, java.util.List,
     *      com.perforce.team.core.p4java.ChangelistSelection,
     *      org.eclipse.core.runtime.IProgressMonitor,
     *      com.perforce.team.ui.refactor.MoveSession)
     */
    @Override
    protected void moveFolder(IFolder source, IFolder destination,
            List<IP4File> files, ChangelistSelection changelist,
            IProgressMonitor monitor, MoveSession session) {
        try {
            // Files already moved so source files no longer exist
            IResource[] members = destination.members();
            for (IResource resource : members) {
                if (resource instanceof IFile) {
                    IFile destinationFile = (IFile) resource;
                    IFile sourceFile = source.getFile(resource.getName());
                    internalMoveFile(sourceFile, destinationFile, files,
                            changelist, monitor, session);
                } else if (resource instanceof IFolder) {
                    moveFolder(source.getFolder(resource.getName()),
                            (IFolder) resource, files, changelist, monitor,
                            session);
                }
            }
        } catch (CoreException e) {
            PerforceProviderPlugin.logError(e);
        }
    }

}
