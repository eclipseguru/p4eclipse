/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.ui.diff.DiffContentProvider;
import com.perforce.team.ui.diff.IFileDiffer;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.FileDiffElement;
import com.perforce.team.ui.folder.diff.model.FileEntry;

import java.io.InputStream;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileDiffContentProvider extends DiffContentProvider {

    /**
     * Diff listener
     */
    public static interface IDiffListener {

        /**
         * Diff file loaded
         * 
         * @param file
         */
        void load(IP4DiffFile file);

        /**
         * Diff file updated
         * 
         * @param file
         */
        void update(IP4DiffFile file);

    }

    private FileDiffContainer container = new FileDiffContainer();
    private ListenerList listeners = new ListenerList();

    /**
     * @param viewer
     * @param async
     */
    public FileDiffContentProvider(StructuredViewer viewer, boolean async) {
        super(viewer, async);
    }

    /**
     * Add diff listener
     * 
     * @param listener
     */
    public void addDiffListener(IDiffListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Remove diff listener
     * 
     * @param listener
     */
    public void removeDiffListener(IDiffListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Set file diff container
     * 
     * @param container
     */
    public void setContainer(FileDiffContainer container) {
        if (container != null) {
            clearDiffers();
            this.container = container;
        }
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#updateResource(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    protected void updateResource(IP4Resource resource) {
        super.updateResource(resource);
        if (resource instanceof IP4DiffFile) {
            IP4DiffFile diffFile = (IP4DiffFile) resource;
            for (Object listener : this.listeners.getListeners()) {
                ((IDiffListener) listener).update(diffFile);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        if (element instanceof IP4DiffFile) {
            FileEntry entry = this.container.getEntry((IP4DiffFile) element);
            if (entry != null) {
                return entry.getParent(element);
            }
        }
        return super.getParent(element);
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#canDiff(com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    public boolean canDiff(IP4Resource file) {
        if (file instanceof IP4DiffFile) {
            return Status.CONTENT == ((IP4DiffFile) file).getStatus();
        }
        return false;
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getLeftStorage(com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File)
     */
    @Override
    public IStorage getLeftStorage(IP4Resource resource, IP4File file) {
        if (resource instanceof IP4DiffFile) {
            final IP4DiffFile diffFile = (IP4DiffFile) resource;
            int revision = diffFile.getDiff().getRevision1();
            if (revision > 0) {
                return new P4Storage() {

                    public InputStream getContents() throws CoreException {
                        return diffFile.getFile1Contents();
                    }
                };
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getRightStorage(com.perforce.team.core.p4java.IP4Resource,
     *      com.perforce.team.core.p4java.IP4File)
     */
    @Override
    public IStorage getRightStorage(IP4Resource resource, IP4File file) {
        if (resource instanceof IP4DiffFile) {
            final IP4DiffFile diffFile = (IP4DiffFile) resource;
            int revision = diffFile.getDiff().getRevision2();
            if (revision > 0) {
                return new P4Storage() {

                    public InputStream getContents() throws CoreException {
                        return diffFile.getFile2Contents();
                    }
                };
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.diff.DiffContentProvider#getDiffs(com.perforce.team.ui.diff.IFileDiffer,
     *      com.perforce.team.core.p4java.IP4Resource)
     */
    @Override
    protected Object[] getDiffs(IFileDiffer differ, IP4Resource resource) {
        Object[] diffs = super.getDiffs(differ, resource);
        if (diffs.length > 0 && resource instanceof IP4DiffFile) {
            Object[] converted = new Object[diffs.length];
            int index = 0;
            IP4DiffFile file = (IP4DiffFile) resource;
            for (Object diff : diffs) {
                if (diff instanceof IDiffElement) {
                    diff = new FileDiffElement((IDiffElement) diff, file);
                }
                converted[index] = diff;
                index++;
            }
            diffs = converted;
        }
        return diffs;
    }

}
