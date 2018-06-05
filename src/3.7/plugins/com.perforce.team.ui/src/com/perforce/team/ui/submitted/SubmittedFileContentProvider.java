/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.ui.diff.DiffContentProvider;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class SubmittedFileContentProvider extends DiffContentProvider {

    /**
     * Creates a new content provider for a structured viewer
     * 
     * @param viewer
     */
    public SubmittedFileContentProvider(StructuredViewer viewer) {
        super(viewer);
    }

    /**
     * Creates a new content provider for a structured view
     * 
     * @param viewer
     * @param async
     */
    public SubmittedFileContentProvider(StructuredViewer viewer, boolean async) {
        super(viewer, async);
    }

    /**
     * Creates a new content provider for a structured view
     * 
     * @param viewer
     * @param context
     */
    public SubmittedFileContentProvider(StructuredViewer viewer, Object context) {
        super(viewer, context);
    }

    /**
     * Creates a new content provider for a structured view
     * 
     * @param viewer
     * @param async
     * @param context
     */
    public SubmittedFileContentProvider(StructuredViewer viewer, boolean async,
            Object context) {
        super(viewer, async, context);
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getLeftStorage(com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File)
     */
    @Override
    public IStorage getLeftStorage(IP4Resource resource, final IP4File file) {
        final int change = file.getChangelistId();
        if (change > 0) {
            return new P4Storage() {

                public InputStream getContents() throws CoreException {
                    return file.getRemoteContents("@" + change); //$NON-NLS-1$
                }
            };
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getRightStorage(com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File)
     */
    @Override
    public IStorage getRightStorage(IP4Resource resource, final IP4File file) {
        final int change = file.getChangelistId() - 1;
        if (change > 0) {
            return new P4Storage() {

                public InputStream getContents() throws CoreException {
                    return file.getRemoteContents("@" + change); //$NON-NLS-1$
                }
            };
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#canDiff(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    public boolean canDiff(IP4Resource resource) {
        if (resource instanceof IP4SubmittedFile) {
            IP4SubmittedFile file = (IP4SubmittedFile) resource;
            return file.getChangelist().getId() > 0
                    && P4File.isActionEdit(file.getAction());
        }
        return false;
    }

}
