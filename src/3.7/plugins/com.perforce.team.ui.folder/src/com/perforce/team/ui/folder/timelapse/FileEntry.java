/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.PerforceContentProvider;

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FileEntry implements IWorkbenchAdapter,
        Comparable<IWorkbenchAdapter>, IEntry {

    private SortedMap<Integer, IFileRevisionData> data;
    private int haveRevision = 0;
    private RootEntry root;
    private FolderEntry parent;
    private String path;
    private String name;

    /**
     * Create an empty file entry
     * 
     * @param data
     * @param parent
     * @param root
     */
    public FileEntry(IFileRevisionData data, FolderEntry parent, RootEntry root) {
        this.root = root;
        this.data = new TreeMap<Integer, IFileRevisionData>();
        this.parent = parent;
        this.path = data.getDepotFileName();
        int index = path.lastIndexOf('/');
        if (index >= 0 && index + 1 < path.length()) {
            this.name = path.substring(index + 1);
        }
        add(data);
        this.parent.add(this);
    }

    /**
     * Set have revision of this entry
     * 
     * @param revision
     */
    public void setHaveRevision(int revision) {
        this.haveRevision = revision;
    }

    /**
     * Get have revision
     * 
     * @return - have revision of entry
     */
    public int getHaveRevision() {
        return this.haveRevision;
    }

    /**
     * @see com.perforce.team.ui.folder.timelapse.IEntry#getFirst()
     */
    public int getFirst() {
        return this.data.get(this.data.firstKey()).getChangelistId();
    }

    /**
     * Get latest file revision data
     * 
     * @return - file revision data
     */
    public IFileRevisionData getLast() {
        return this.data.get(this.data.lastKey());
    }

    /**
     * Get the data for a changelist id
     * 
     * @param changelist
     * @return - file revision data
     */
    public IFileRevisionData getData(int changelist) {
        return this.data.get(changelist);
    }

    /**
     * Is the file synced at the specified changelist
     * 
     * @param changelist
     * @return - true if synced, false otherwise
     */
    public boolean isSynced(int changelist) {
        boolean synced = true;
        IFileRevisionData data = this.data.get(changelist);
        if (data != null) {
            synced = this.haveRevision >= data.getRevision();
        }
        return synced;
    }

    /**
     * Add a file revision to this entry
     * 
     * @param revision
     */
    public void add(IFileRevisionData revision) {
        if (revision != null && this.path.equals(revision.getDepotFileName())) {
            this.parent.updateMoveStatus(this, revision);
            int id = revision.getChangelistId();
            IP4Revision[] revisions = getRevisions();
            for (int i = revisions.length - 1; i >= 0; i--) {
                int revId = revisions[i].getChangelist();
                if (id <= revId) {
                    IFileRevisionData current = this.data.get(revId);
                    if (current == null) {
                        this.data.put(revId, revision);
                    } else if (id > current.getChangelistId()) {
                        this.data.put(revId, revision);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FileEntry) {
            FileEntry other = (FileEntry) obj;
            return this.path.equals(other.path);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return PerforceContentProvider.EMPTY;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(this.name);
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return this.name;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return this.parent;
    }

    /**
     * @param o
     * @return - comparison
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IWorkbenchAdapter o) {
        return getLabel(this).compareToIgnoreCase(o.getLabel(o));
    }

    /**
     * @see com.perforce.team.ui.folder.timelapse.IEntry#complete()
     */
    public void complete() {

    }

    /**
     * @see com.perforce.team.ui.folder.timelapse.IEntry#getRevisions()
     */
    public IP4Revision[] getRevisions() {
        return this.root.getRevisions();
    }

    /**
     * Get file path
     * 
     * @return - string path
     */
    public String getPath() {
        return this.path;
    }

}
