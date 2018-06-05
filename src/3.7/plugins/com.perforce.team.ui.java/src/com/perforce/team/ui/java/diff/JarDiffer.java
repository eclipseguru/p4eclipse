/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.java.diff;

import com.perforce.team.ui.diff.BaseFileDiffer;

import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.jdt.internal.ui.compare.JarStructureCreator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JarDiffer extends BaseFileDiffer {

    private JarStructureCreator creator = null;

    /**
     * @see com.perforce.team.ui.diff.BaseFileDiffer#formatDiffs(org.eclipse.compare.structuremergeviewer.IDiffContainer)
     */
    @Override
    protected Object[] formatDiffs(IDiffContainer root) {
        return root.getChildren();
    }

    /**
     * @see com.perforce.team.ui.diff.BaseFileDiffer#getStructureCreator()
     */
    @Override
    protected IStructureCreator getStructureCreator() {
        if (creator == null) {
            creator = new JarStructureCreator();
        }
        return creator;
    }

}
