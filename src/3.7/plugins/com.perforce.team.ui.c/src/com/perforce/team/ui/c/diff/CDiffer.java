/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.c.diff;

import com.perforce.team.ui.diff.BaseFileDiffer;

import org.eclipse.cdt.internal.ui.compare.CStructureCreator;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CDiffer extends BaseFileDiffer {

    private CStructureCreator creator;

    /**
     * @see com.perforce.team.ui.diff.BaseFileDiffer#formatDiffs(org.eclipse.compare.structuremergeviewer.IDiffContainer)
     */
    @Override
    protected Object[] formatDiffs(IDiffContainer root) {
        IDiffElement[] children = root.getChildren();
        if (children.length == 1 && children[0] instanceof IDiffContainer) {
            root = (IDiffContainer) children[0];
            children = root.getChildren();
        }
        return children;
    }

    /**
     * @see com.perforce.team.ui.diff.BaseFileDiffer#getStructureCreator()
     */
    @Override
    protected IStructureCreator getStructureCreator() {
        if (creator == null) {
            creator = new CStructureCreator();
        }
        return creator;
    }

}
