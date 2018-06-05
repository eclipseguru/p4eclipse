/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results.tree;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.search.results.FileMatch;
import com.perforce.team.ui.search.results.RevisionMatch;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchTreeResultsSorter extends ViewerSorter {

    /**
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof RevisionMatch && e2 instanceof RevisionMatch) {
            return ((RevisionMatch) e2).getRevision()
                    - ((RevisionMatch) e1).getRevision();
        } else if (e1 instanceof IP4File && e2 instanceof IP4File) {
            return ((IP4File) e1).getRemotePath().compareToIgnoreCase(
                    ((IP4File) e2).getRemotePath());
        } else if (e1 instanceof FileMatch && e2 instanceof FileMatch) {
            return ((FileMatch) e1).getLineNumber()
                    - ((FileMatch) e2).getLineNumber();
        }
        return super.compare(viewer, e1, e2);
    }

}
