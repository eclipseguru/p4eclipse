/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IMoveManager extends IRefactorManager {

    /**
     * Handle a folder move
     * 
     * @param tree
     * @param source
     * @param destination
     * @param flags
     * @param monitor
     * @return - boolean
     */
    boolean move(IResourceTree tree, IFolder source, IFolder destination,
            int flags, IProgressMonitor monitor);

    /**
     * Handle a file move
     * 
     * @param tree
     * @param source
     * @param destination
     * @param flags
     * @param monitor
     * @return - boolean
     */
    boolean move(IResourceTree tree, IFile source, IFile destination,
            int flags, IProgressMonitor monitor);

    /**
     * Handle a project move
     * 
     * @param tree
     * @param source
     * @param description
     * @param flags
     * @param monitor
     * @return - boolean
     */
    boolean move(IResourceTree tree, IProject source,
            IProjectDescription description, int flags, IProgressMonitor monitor);

}
