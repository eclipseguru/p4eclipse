/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.DeleteAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DeleteManager extends RefactorManager implements IDeleteManager {

    private void delete(IP4Resource resource, IProgressMonitor monitor) {
        P4Collection collection = new P4Collection(
                new IP4Resource[] { resource });
        collection.revert();

        // Run delete part in action so prompts are displayed
        DeleteAction action = new DeleteAction();
        action.setCollection(collection);
        action.setMonitor(monitor);
        action.setAsync(false);
        action.run(null);
        if (action.wasDialogCancelled()) {
            throw new OperationCanceledException(
                    "Changelist selection dialog was cancelled.");
        }
    }

    private boolean isIgnored(IResource resource) {
        return PerforceProviderPlugin.isIgnoredHint(resource);
    }

    private boolean isLinked(IResource resource) {
        return resource.isLinked();
    }

    private boolean checkLink(IResource resource) {
        return isEnabled(IPerforceUIConstants.PREF_DELETE_LINKED_RESOURCES)
                || !isLinked(resource);
    }

    /**
     * @see com.perforce.team.ui.refactor.IDeleteManager#delete(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFile, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean delete(IResourceTree tree, IFile file, int updateFlags,
            IProgressMonitor monitor) {
        boolean standard = true;
        if (isEnabled() && !isIgnored(file) && checkLink(file)) {
            IP4Resource resource = P4ConnectionManager.getManager()
                    .getResource(file);
            if (resource instanceof IP4File) {
                delete(resource, monitor);

                // If command has deleted file then update the tree
                if (!exists(file)) {
                    tree.deletedFile(file);
                    standard = false;
                }
            }
        }
        if (standard) {
            tree.standardDeleteFile(file, updateFlags, monitor);
        }
        return false;
    }

    /**
     * @see com.perforce.team.ui.refactor.IDeleteManager#delete(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFolder, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean delete(IResourceTree tree, IFolder folder, int updateFlags,
            IProgressMonitor monitor) {
        boolean standard = true;
        if (isEnabled() && !isIgnored(folder) && checkLink(folder)) {
            IP4Resource resource = P4ConnectionManager.getManager()
                    .getResource(folder);
            if (resource instanceof IP4Container) {
                delete(resource, monitor);

                // If command has deleted the folder (rmdir client) then update
                // the tree
                if (!exists(folder)) {
                    tree.deletedFolder(folder);
                    standard = false;
                }
            }
        }
        if (standard) {
            tree.standardDeleteFolder(folder, updateFlags, monitor);
        }
        return true;
    }

    /**
     * @see com.perforce.team.ui.refactor.IDeleteManager#delete(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IProject, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean delete(IResourceTree tree, IProject project,
            int updateFlags, IProgressMonitor monitor) {
        boolean standard = true;
        if (isEnabled(IPerforceUIConstants.PREF_DELETE_PROJECT_FILES)) {
            // Fix for job012868
            // Need Perforce to delete project first otherwise Eclipse will
            // unmanage the project
            IP4Resource resource = P4ConnectionManager.getManager()
                    .getResource(project);
            if (resource instanceof IP4Container) {
                delete(resource, monitor);

                // If command has deleted the project folder (rmdir client) then
                // update the tree
                if (!exists(project)) {
                    tree.deletedProject(project);
                    standard = false;
                }
            }
        }
        if (standard) {
            tree.standardDeleteProject(project, updateFlags, monitor);
        }
        return false;
    }

}
