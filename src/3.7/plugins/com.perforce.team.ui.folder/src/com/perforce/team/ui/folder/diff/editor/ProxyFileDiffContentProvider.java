/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.diff.IFileDiffer;
import com.perforce.team.ui.folder.diff.editor.FileDiffContentProvider.IDiffListener;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ProxyFileDiffContentProvider extends FileDiffContentProvider
        implements IDiffListener {

    private FileDiffContentProvider contentProvider;

    /**
     * @param viewer
     * @param async
     */
    public ProxyFileDiffContentProvider(StructuredViewer viewer, boolean async) {
        super(viewer, async);
    }

    /**
     * Set content provider
     * 
     * @param contentProvider
     */
    public void setContentProvider(FileDiffContentProvider contentProvider) {
        this.contentProvider = contentProvider;
        this.contentProvider.addDiffListener(this);
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#generateDiff(com.perforce.team.ui.diff.IFileDiffer,
     *      com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected void generateDiff(IFileDiffer differ, IP4Resource resource,
            IP4File file) {
        // Do not generate diffs since the other content provider is responsible
        // for that
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#canDiff(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    public boolean canDiff(IP4Resource file) {
        return this.contentProvider.canDiff(file);
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getLeftStorage(com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File)
     */
    @Override
    public IStorage getLeftStorage(IP4Resource resource, IP4File file) {
        return this.contentProvider.getLeftStorage(resource, file);
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getRightStorage(com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File)
     */
    @Override
    public IStorage getRightStorage(IP4Resource resource, IP4File file) {
        return this.contentProvider.getRightStorage(resource, file);
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getDiffer(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    public IFileDiffer getDiffer(IP4Resource resource) {
        return this.contentProvider.getDiffer(resource);
    }

    /**
     * @see com.perforce.team.ui.PerforceContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IP4DiffFile) {
            IP4DiffFile pair = ((IP4DiffFile) parentElement).getPair();
            if (pair != null) {
                parentElement = pair;
            }
        }
        return super.getChildren(parentElement);
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.FileDiffContentProvider#updateResource(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    protected void updateResource(IP4Resource resource) {
        super.updateResource(resource);
        if (resource instanceof IP4DiffFile) {
            resource = ((IP4DiffFile) resource).getPair();
            if (resource != null) {
                super.updateResource(resource);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.FileDiffContentProvider.IDiffListener#load(com.perforce.team.core.folder.IP4DiffFile)
     */
    public void load(IP4DiffFile file) {

    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.FileDiffContentProvider.IDiffListener#update(com.perforce.team.core.folder.IP4DiffFile)
     */
    public void update(IP4DiffFile file) {
        file = file.getPair();
        if (file != null) {
            updateResource(file);
        }
    }

}
