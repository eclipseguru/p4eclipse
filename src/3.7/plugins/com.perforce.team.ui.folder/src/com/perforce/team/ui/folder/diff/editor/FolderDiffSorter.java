/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.ui.changelists.Folder;
import com.perforce.team.ui.diff.DiffSorter;
import com.perforce.team.ui.folder.diff.model.FileDiffElement;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FolderDiffSorter extends DiffSorter {

    /**
     * @see com.perforce.team.ui.diff.DiffSorter#category(java.lang.Object)
     */
    @Override
    public int category(Object element) {
        if (element instanceof FileDiffElement) {
            element = P4CoreUtils.convert(element, DiffNode.class);
        }
        return super.category(element);
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof IP4DiffFile && e2 instanceof Folder) {
            return 1;
        }
        if (e1 instanceof Folder && e2 instanceof IP4DiffFile) {
            return -1;
        }
        return super.compare(viewer, e1, e2);
    }

}
