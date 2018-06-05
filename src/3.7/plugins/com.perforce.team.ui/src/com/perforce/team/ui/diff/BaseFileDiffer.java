/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.diff;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.editor.P4StorageNode;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class BaseFileDiffer implements IFileDiffer {

    private Map<IP4Resource, Object[]> diffs;

    /**
     * Base file differ
     */
    public BaseFileDiffer() {
        diffs = new HashMap<IP4Resource, Object[]>();
    }

    /**
     * Get structure creator
     * 
     * @return - structure create
     */
    protected abstract IStructureCreator getStructureCreator();

    /**
     * Format the diffs to display
     * 
     * @param root
     * @return - formatted diffs
     */
    protected abstract Object[] formatDiffs(IDiffContainer root);

    /**
     * @see com.perforce.team.ui.diff.IFileDiffer#diffGenerated(com.perforce.team.core.p4java.IP4Resource)
     */
    public boolean diffGenerated(IP4Resource file) {
        return diffs.containsKey(file);
    }

    /**
     * @see com.perforce.team.ui.diff.IFileDiffer#dispose()
     */
    public void dispose() {
        this.diffs.clear();
    }

    /**
     * @see com.perforce.team.ui.diff.IFileDiffer#generateDiff(com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File,
     *      org.eclipse.core.resources.IStorage,
     *      org.eclipse.core.resources.IStorage)
     */
    public void generateDiff(IP4Resource resource, IP4File file,
            IStorage storage1, IStorage storage2) {
        final String ext = new Path(file.getName()).getFileExtension();

        IStructureComparator prev = new P4StorageNode(storage1,
                Messages.BaseFileDiffer_Left, ext);
        IStructureComparator curr = new P4StorageNode(storage2,
                Messages.BaseFileDiffer_Right, ext);

        IStructureCreator creator = getStructureCreator();
        prev = creator.getStructure(prev);
        curr = creator.getStructure(curr);

        Differencer differ = new Differencer();
        IDiffContainer allDiffs = (IDiffContainer) differ.findDifferences(
                false, new NullProgressMonitor(), null, null, prev, curr);
        if (allDiffs != null) {
            Object[] formatted = formatDiffs(allDiffs);
            diffs.put(resource, formatted);
            diffs.put(file, formatted);
        } else {
            diffs.put(resource, PerforceContentProvider.EMPTY);
            diffs.put(file, PerforceContentProvider.EMPTY);
        }
    }

    /**
     * @see com.perforce.team.ui.diff.IFileDiffer#getDiff(com.perforce.team.core.p4java.IP4Resource)
     */
    public Object[] getDiff(IP4Resource file) {
        Object[] diffs = null;
        if (file != null) {
            diffs = this.diffs.get(file);
        }
        if (diffs == null) {
            diffs = PerforceContentProvider.EMPTY;
        }
        return diffs;
    }

}
