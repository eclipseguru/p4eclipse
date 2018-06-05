/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4FileNode extends P4CompareNode {

    private File file;
    private String name = null;
    private String id = null;

    /**
     * @param filename
     * @param label
     * @param type
     * @param editable
     * @param name
     */
    public P4FileNode(String filename, String label, String type,
            boolean editable, String name) {
        this(new File(filename), label, type, editable, name);
    }

    /**
     * @param filename
     * @param label
     * @param type
     * @param name
     */
    public P4FileNode(String filename, String label, String type, String name) {
        this(new File(filename), label, type, false, name);
    }

    /**
     * @param file
     * @param label
     * @param type
     * @param name
     */
    public P4FileNode(File file, String label, String type, String name) {
        this(file, label, type, false, name);
    }

    /**
     * @param file
     * @param label
     * @param type
     * @param editable
     * @param name
     */
    public P4FileNode(File file, String label, String type, boolean editable,
            String name) {
        super(label, type);
        this.file = file;
        this.editable = editable;
        this.name = name;
    }

    /**
     * @see org.eclipse.compare.IStreamContentAccessor#getContents()
     */
    public InputStream getContents() throws CoreException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * @see com.perforce.team.ui.editor.P4CompareNode#commit(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void commit(IProgressMonitor monitor) throws CoreException {
        if (isEditable()) {
            // TODO support writing back to the file
        }
    }

    /**
     * @see org.eclipse.compare.ITypedElement#getName()
     */
    public String getName() {
        return this.name != null ? this.name : this.file.getName();
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
        if (other instanceof P4FileNode) {
            P4FileNode file2 = (P4FileNode) other;
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
