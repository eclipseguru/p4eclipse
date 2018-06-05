/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results.table;

import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.search.results.P4SearchResult;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchTableContentProvider extends PerforceContentProvider {

    /**
     * Search results
     */
    protected P4SearchResult results = null;

    /**
     * @param viewer
     */
    public SearchTableContentProvider(StructuredViewer viewer) {
        super(viewer);
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        super.inputChanged(viewer, oldInput, newInput);
        results = (P4SearchResult) newInput;
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        int count = results.getMatchCount();
        if (count > 0) {
            return results.getElements();
        } else {
            return EMPTY;
        }
    }

}
