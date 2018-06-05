/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.ui.P4UIUtils;

import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class P4CompareNode implements IP4CompareNode {

    /**
     * Node label
     */
    protected String label;

    /**
     * Node type
     */
    protected String type;

    /**
     * Editable node?
     */
    protected boolean editable = false;

    /**
     * @param label
     * @param type
     * @param editable
     */
    public P4CompareNode(String label, String type, boolean editable) {
        this.label = label;
        if (type != null) {
            this.type = type;
        } else {
            this.type = ITypedElement.UNKNOWN_TYPE;
        }
        this.editable = editable;
    }

    /**
     * @param label
     * @param type
     */
    public P4CompareNode(String label, String type) {
        this(label, type, false);
    }

    /**
     * @see com.perforce.team.ui.editor.IP4CompareNode#getLabel()
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @see org.eclipse.compare.structuremergeviewer.IStructureComparator#getChildren()
     */
    public Object[] getChildren() {
        return new Object[0];
    }

    /**
     * @see org.eclipse.compare.ITypedElement#getImage()
     */
    public Image getImage() {
        return P4UIUtils.getImageDescriptor(type).createImage();
    }

    /**
     * @see org.eclipse.compare.ITypedElement#getType()
     */
    public String getType() {
        return this.type;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ITypedElement) {
            String otherName = ((ITypedElement) other).getName();
            return getName().equals(otherName);
        }
        return false;
    }

    @Override
    public int hashCode() {
    	String name = getName();
    	if(name!=null)
    		return name.hashCode();
    	return super.hashCode();
    }
    
    /**
     * @see com.perforce.team.ui.editor.IP4CompareNode#isEditable()
     */
    public boolean isEditable() {
        return this.editable;
    }

    /**
     * @see com.perforce.team.ui.editor.IP4CompareNode#commit(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void commit(IProgressMonitor monitor) throws CoreException {
        // Does nothing by default, subclasses should override if supported
    }

}
