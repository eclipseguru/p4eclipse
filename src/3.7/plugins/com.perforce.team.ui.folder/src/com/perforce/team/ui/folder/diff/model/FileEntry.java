/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.model;

import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Resource;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileEntry extends PlatformObject implements IWorkbenchAdapter {

    private IGroupProvider provider = null;
    private IP4DiffFile file = null;
    private FileEntry pair = null;
    private String virtualPairPath;

    /**
     * Create a file entry
     * 
     * @param file
     * @param provider
     */
    public FileEntry(IP4DiffFile file, IGroupProvider provider) {
        this.file = file;
        this.provider = provider;
    }

    /**
     * Set virtual pair path
     * 
     * @param path
     */
    public void setVirtualPairPath(String path) {
        this.virtualPairPath = path;
    }

    /**
     * Get virtual pair path
     * 
     * @return pair path, may be null
     */
    public String getVirtualPairPath() {
        return this.virtualPairPath;
    }

    /**
     * Get group provider for this file entry
     * 
     * @return group provider
     */
    public IGroupProvider getProvider() {
        return this.provider;
    }

    /**
     * Set file entry pair
     * 
     * @param pair
     */
    public void setPair(FileEntry pair) {
        this.pair = pair;
    }

    /**
     * Get file entry pair
     * 
     * @return pair
     */
    public FileEntry getPair() {
        return this.pair;
    }

    /**
     * Get diff file
     * 
     * @return diff file
     */
    public IP4DiffFile getFile() {
        return this.file;
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IP4Connection.class == adapter) {
            return this.file.getConnection();
        }
        if (IP4Resource.class == adapter || IP4DiffFile.class == adapter) {
            return this.file;
        }
        if (IP4File.class == adapter) {
            return this.file.getFile();
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return P4Resource.EMPTY;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(this.file.getName());
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return this.file.getName();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return this.provider != null ? this.provider.getParent(this) : null;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof FileEntry) {
            return this.file.equals(((FileEntry) obj).file);
        }
        return false;
    }

    @Override
    public int hashCode() {
    	if(this.file!=null)
    		return this.file.hashCode();
    	return super.hashCode();
    }
}
