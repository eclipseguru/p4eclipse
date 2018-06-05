/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.zip;

import com.perforce.team.ui.diff.BaseFileDiffer;

import org.eclipse.compare.ZipFileStructureCreator;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ZipDiffer extends BaseFileDiffer {

    private ZipFileStructureCreator creator;

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
            creator = new ZipFileStructureCreator();
        }
        return creator;
    }

}
