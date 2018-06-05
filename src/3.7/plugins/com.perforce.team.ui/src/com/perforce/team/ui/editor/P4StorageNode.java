/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4StorageNode extends P4CompareNode {

    private IStorage storage;
    private String id;

    /**
     * @param storage
     * @param label
     * @param filename
     */
    public P4StorageNode(IStorage storage, String label, String filename) {
        super(label, filename);
        this.storage = storage;
    }

    /**
     * @see org.eclipse.compare.IStreamContentAccessor#getContents()
     */
    public InputStream getContents() throws CoreException {
        return this.storage.getContents();
    }

    /**
     * @see org.eclipse.compare.ITypedElement#getName()
     */
    public String getName() {
        return this.storage.getName();
    }

    /**
     * Set content identifier
     * 
     * @param id
     */
    public void setContentIdentifier(String id) {
        this.id = id;
    }

    /**
     * @see com.perforce.team.ui.editor.P4CompareNode#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof P4StorageNode) {
            P4StorageNode file2 = (P4StorageNode) other;
            if (this.id != null && file2.id != null) {
                return this.id.equals(file2.id);
            } else {
                return super.equals(other);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
    	if(this.id!=null)
    		return this.id.hashCode();
    	return super.hashCode();
    }
    
}
