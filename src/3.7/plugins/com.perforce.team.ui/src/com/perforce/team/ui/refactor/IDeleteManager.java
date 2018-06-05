/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IDeleteManager extends IRefactorManager {

    /**
     * Handle a file deletion
     * 
     * @param tree
     * @param file
     * @param updateFlags
     * @param monitor
     * @return - boolean
     */
    boolean delete(IResourceTree tree, IFile file, int updateFlags,
            IProgressMonitor monitor);

    /**
     * Handle a folder deletion
     * 
     * @param tree
     * @param folder
     * @param updateFlags
     * @param monitor
     * @return - boolean
     */
    boolean delete(IResourceTree tree, final IFolder folder, int updateFlags,
            IProgressMonitor monitor);

    /**
     * Handle a project deletion
     * 
     * @param tree
     * @param project
     * @param updateFlags
     * @param monitor
     * @return - boolean
     */
    boolean delete(IResourceTree tree, IProject project, int updateFlags,
            IProgressMonitor monitor);

}
