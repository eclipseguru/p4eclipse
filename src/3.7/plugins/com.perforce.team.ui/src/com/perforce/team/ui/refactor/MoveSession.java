/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

import com.perforce.team.core.PerforceProviderPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Move session class to be used by {@link MoveDeleteManager} when the p4 'move'
 * command is used during refactorings.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MoveSession {

    /**
     * Simple pair class
     */
    private static class Pair {

        IResource source;
        IResource destination;

        Pair(IResource source, IResource destination) {
            this.source = source;
            this.destination = destination;
        }

    }

    private List<Pair> folders;
    private List<Pair> unmoved;
    private List<Pair> moved;

    private IResourceTree tree;
    private int flags;
    private IProgressMonitor monitor;

    /**
     * Create a new move session
     * 
     * @param tree
     * @param flags
     * @param monitor
     */
    public MoveSession(IResourceTree tree, int flags, IProgressMonitor monitor) {
        this.tree = tree;
        this.flags = flags;
        this.monitor = monitor;
        folders = new ArrayList<Pair>();
        unmoved = new ArrayList<Pair>();
        moved = new ArrayList<Pair>();
    }

    /**
     * Move was attempted
     * 
     * @param source
     * @param destination
     * @param success
     */
    public void moved(IFile source, IFile destination, boolean success) {
        if (success) {
            this.moved.add(new Pair(source, destination));
        } else {
            this.unmoved.add(new Pair(source, destination));
        }
    }

    /**
     * Folder was moved
     * 
     * @param source
     * @param destination
     */
    public void moved(IFolder source, IFolder destination) {
        this.folders.add(new Pair(source, destination));
    }

    private boolean exists(IResource resource) {
        IPath location = resource.getLocation();
        return location != null ? location.toFile().exists() : false;
    }

    /**
     * Update the resource tree with any remaining changes need to be made
     */
    public void finish() {
        for (Pair pair : this.folders) {
            if (exists(pair.destination)) {
                tree.movedFolderSubtree((IFolder) pair.source,
                        (IFolder) pair.destination);
            } else {
                try {
                    pair.source.refreshLocal(IResource.DEPTH_ONE,
                            new NullProgressMonitor());
                    pair.destination.getParent().refreshLocal(
                            IResource.DEPTH_ONE, new NullProgressMonitor());
                    tree.standardMoveFolder((IFolder) pair.source,
                            (IFolder) pair.destination, flags, monitor);
                } catch (CoreException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
        this.folders.clear();
        for (Pair pair : this.moved) {
            // Only move files not currently moved since the resource tree
            // may have been updated in folder loop
            if (!pair.destination.exists()) {
                tree.movedFile((IFile) pair.source, (IFile) pair.destination);
            }
        }
        this.moved.clear();
        for (Pair pair : this.unmoved) {
            if (!exists(pair.destination)) {
                try {
                    pair.source.refreshLocal(IResource.DEPTH_ONE, monitor);
                    pair.destination.refreshLocal(IResource.DEPTH_ONE, monitor);
                    tree.standardMoveFile((IFile) pair.source,
                            (IFile) pair.destination, flags, monitor);
                } catch (CoreException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }

        }
        this.unmoved.clear();
    }
}
