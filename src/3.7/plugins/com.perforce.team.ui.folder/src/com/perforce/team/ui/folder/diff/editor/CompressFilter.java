/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.ui.folder.diff.model.GroupedDiffContainer.GroupFolder;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CompressFilter extends ViewerFilter {

    private ViewerFilter[] fileFilters = new ViewerFilter[0];

    /**
     * Set file filters
     * 
     * @param filters
     */
    public void setFileFilters(ViewerFilter[] filters) {
        if (filters != null) {
            this.fileFilters = filters;
        } else {
            this.fileFilters = new ViewerFilter[0];
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof GroupFolder && this.fileFilters.length > 0) {
            return hasUnfilteredChidlren(viewer, (GroupFolder) element);
        }
        return true;
    }

    private boolean fileDisplayed(Viewer viewer, Object file, Object parent) {
        for (ViewerFilter filter : this.fileFilters) {
            if (!filter.select(viewer, parent, file)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasUnfilteredChidlren(Viewer viewer, GroupFolder element) {
        if (element.containsFolders() || element.containsFiles()) {
            for (Object child : element.getChildren(element)) {
                if (child instanceof IP4DiffFile
                        && fileDisplayed(viewer, child, element)) {
                    return true;
                } else if (child instanceof GroupFolder
                        && hasUnfilteredChidlren(viewer, (GroupFolder) child)) {
                    return true;
                }
            }
        }
        return false;
    }
}
