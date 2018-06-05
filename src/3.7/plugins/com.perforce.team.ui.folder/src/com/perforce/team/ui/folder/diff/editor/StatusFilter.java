/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.folder.IP4DiffFile;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class StatusFilter extends ViewerFilter {

    private Status status = null;

    /**
     * Create a status filter
     * 
     * @param status
     */
    public StatusFilter(Status status) {
        this.status = status;
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IP4DiffFile) {
            return status != ((IP4DiffFile) element).getDiff().getStatus();
        }
        return true;
    }

}
