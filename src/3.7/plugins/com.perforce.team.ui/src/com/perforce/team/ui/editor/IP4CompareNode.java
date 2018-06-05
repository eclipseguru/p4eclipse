/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IP4CompareNode extends IStructureComparator,
        IStreamContentAccessor, ITypedElement {

    /**
     * Get label of node
     * 
     * @return - node label
     */
    String getLabel();

    /**
     * Is the node editable?
     * 
     * @return - true if editable
     */
    boolean isEditable();

    /**
     * Commit changes
     * 
     * @param monitor
     * @throws CoreException
     */
    void commit(IProgressMonitor monitor) throws CoreException;
}
