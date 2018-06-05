/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ChangelistSelection;
import com.perforce.team.core.p4java.IP4File;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class Move91Manager extends MoveManager implements IMoveManager {

    /**
     * Create a move 2009.1 manager
     */
    public Move91Manager() {
        super(true, true);
    }

    /**
     * @see com.perforce.team.ui.refactor.MoveManager#moveFile(com.perforce.team.core.p4java.IP4File,
     *      com.perforce.team.core.p4java.IP4File,
     *      com.perforce.team.core.p4java.ChangelistSelection)
     */
    @Override
    protected boolean moveFile(IP4File fromFile, IP4File toFile,
            ChangelistSelection changelist) {
        return fromFile.move(toFile, true, changelist, false);
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
            if (session != null) {
                session.moved(source, destination);
            }
            IResource[] members = source.members();
            for (IResource resource : members) {
                if (resource instanceof IFile) {
                    IFile sourceFile = (IFile) resource;
                    IFile destinationFile = destination.getFile(resource
                            .getName());
                    internalMoveFile(sourceFile, destinationFile, files,
                            changelist, monitor, session);
                } else if (resource instanceof IFolder) {
                    moveFolder((IFolder) resource,
                            destination.getFolder(resource.getName()), files,
                            changelist, monitor, session);
                }
            }
        } catch (CoreException e) {
            PerforceProviderPlugin.logError(e);
        }
    }
}
