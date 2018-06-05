/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.java.diff;

import com.perforce.team.ui.diff.BaseFileDiffer;

import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.jdt.internal.ui.compare.PropertiesStructureCreator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class PropertiesDiffer extends BaseFileDiffer {

    private PropertiesStructureCreator creator;

    /**
     * @see com.perforce.team.ui.diff.BaseFileDiffer#formatDiffs(org.eclipse.compare.structuremergeviewer.IDiffContainer)
     */
    @Override
    protected Object[] formatDiffs(IDiffContainer root) {
        IDiffElement[] children = root.getChildren();
        if (children.length == 1 && children[0] instanceof IDiffContainer) {
            root = (IDiffContainer) children[0];
            if (root.hasChildren()) {
                children = root.getChildren();
            }
        }
        return children;
    }

    /**
     * @see com.perforce.team.ui.diff.BaseFileDiffer#getStructureCreator()
     */
    @Override
    protected IStructureCreator getStructureCreator() {
        if (creator == null) {
            creator = new PropertiesStructureCreator();
        }
        return creator;
    }

}
