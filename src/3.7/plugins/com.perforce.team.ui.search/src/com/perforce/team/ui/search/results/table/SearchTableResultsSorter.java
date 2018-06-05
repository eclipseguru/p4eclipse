/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results.table;

import com.perforce.team.ui.search.results.RevisionMatch;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchTableResultsSorter extends ViewerSorter {

    /**
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof RevisionMatch && e2 instanceof RevisionMatch) {
            return ((RevisionMatch) e1).getDepotPath().compareToIgnoreCase(
                    ((RevisionMatch) e2).getDepotPath());
        }
        return super.compare(viewer, e1, e2);
    }
}