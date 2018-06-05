/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.pending;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredViewer;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.ui.diff.DiffContentProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class PendingFileContentProvider extends DiffContentProvider {

    /**
     * Creates a new content provider for a structured viewer
     * 
     * @param viewer
     */
    public PendingFileContentProvider(StructuredViewer viewer) {
        super(viewer);
    }

    /**
     * Creates a new content provider for a structured view
     * 
     * @param viewer
     * @param async
     */
    public PendingFileContentProvider(StructuredViewer viewer, boolean async) {
        super(viewer, async);
    }

    /**
     * Creates a new content provider for a structured view
     * 
     * @param viewer
     * @param context
     */
    public PendingFileContentProvider(StructuredViewer viewer, Object context) {
        super(viewer, context);
    }

    /**
     * Creates a new content provider for a structured view
     * 
     * @param viewer
     * @param async
     * @param context
     */
    public PendingFileContentProvider(StructuredViewer viewer, boolean async,
            Object context) {
        super(viewer, async, context);
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getLeftStorage(com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File)
     */
    @Override
    public IStorage getLeftStorage(IP4Resource resource, final IP4File file) {
        final File local = file.toFile();
        if (local != null && local.exists()) {
            return new P4Storage() {

                public InputStream getContents() throws CoreException {
                    try {
                        return new FileInputStream(local);
                    } catch (FileNotFoundException e) {
                        return new ByteArrayInputStream(new byte[0]);
                    }
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
        return new P4Storage() {

            public InputStream getContents() throws CoreException {
                return file.getHeadContents();
            }
        };
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#canDiff(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    public boolean canDiff(IP4Resource resource) {
        if (resource instanceof IP4File) {
            IP4File file = (IP4File) resource;
            File local = file.toFile();
            return file.openedForEdit() && local != null && local.exists();
        }
        return false;
    }

}
