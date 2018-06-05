/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.diff;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DiffSorter extends ViewerSorter {

    /**
     * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
     */
    @Override
    public int category(Object element) {
        if (element instanceof DiffNode) {
            Object node = ((DiffNode) element).getId();
            if (node instanceof DocumentRangeNode) {
                return ((DocumentRangeNode) node).getTypeCode();
            }
        }
        return super.category(element);
    }

}
