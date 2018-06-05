/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results.tree;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.search.results.P4SearchResult;
import com.perforce.team.ui.search.results.RevisionMatch;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchTreeContentProvider extends PerforceContentProvider {

    /**
     * Search results
     */
    protected P4SearchResult results = null;

    /**
     * Display type for results
     */
    protected Type type = Type.FLAT;

    /**
     * @param viewer
     */
    public SearchTreeContentProvider(StructuredViewer viewer) {
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
        if (results.getMatchCount() > 0) {
            switch (this.type) {
            case FLAT:
            default:
                return results.getFiles();
            case TREE:
                return results.getFolders();
            case COMPRESSED:
                return results.getCompressed();
            }
        } else {
            return EMPTY;
        }
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IP4File) {
            return results.getRevisions((IP4File) parentElement);
        } else if (parentElement instanceof RevisionMatch) {
            return results.getMatches(parentElement);
        }
        return super.getChildren(parentElement);
    }

    /**
     * Set display type
     * 
     * @param type
     */
    public void setDisplayType(Type type) {
        if (type != null) {
            this.type = type;
        }
    }

    /**
     * Refresh the underlying viewer
     */
    public void refresh() {
        this.viewer.refresh();
    }
}
