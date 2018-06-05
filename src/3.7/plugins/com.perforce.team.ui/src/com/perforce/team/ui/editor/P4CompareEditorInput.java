/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.ui.P4UIUtils;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4CompareEditorInput extends CompareEditorInput {

    /**
     * P4 compare configuration
     */
    private static class P4CompareConfiguration extends CompareConfiguration {

        public P4CompareConfiguration(IP4CompareNode leftNode,
                IP4CompareNode rightNode, String type) {
            setLeftLabel(leftNode.getLabel());
            setRightLabel(rightNode.getLabel());
            ImageDescriptor desc = P4UIUtils.getImageDescriptor(type);
            setRightImage(desc.createImage());
            setLeftImage(desc.createImage());
            setLeftEditable(leftNode.isEditable());
            setRightEditable(rightNode.isEditable());
        }

        @Override
        public String getAncestorLabel(Object element) {
            return super.getAncestorLabel(element);
        }
    }

    private IP4CompareNode left;
    private IP4CompareNode right;

    /**
     * @param title
     * @param left
     * @param right
     */
    public P4CompareEditorInput(String title, IP4CompareNode left,
            IP4CompareNode right) {
        super(new P4CompareConfiguration(left, right, left.getType()));
        this.left = left;
        this.right = right;
        setTitle(title);
    }

    /**
     * @return the left compare node
     */
    public IP4CompareNode getLeft() {
        return this.left;
    }

    /**
     * @return the right compare node
     */
    public IP4CompareNode getRight() {
        return this.right;
    }

    /**
     * 
     * @see org.eclipse.compare.CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void saveChanges(IProgressMonitor pm) throws CoreException {
        super.saveChanges(pm);
        if (left.isEditable() && left instanceof P4ResourceNode) {
            try {
                ((P4ResourceNode) left).commit(pm);
            } finally {
                setDirty(false);
            }
        }
    }

    /**
     * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected Object prepareInput(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        return new Differencer().findDifferences(false, monitor, null, null,
                this.left, this.right);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof P4CompareEditorInput) {
            if (this.left != null && this.right != null) {
                P4CompareEditorInput other = (P4CompareEditorInput) obj;
                return this.left.equals(other.left)
                        && this.right.equals(other.right);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
    	int hash=0;
    	if (this.left != null)
    		hash+=this.left.hashCode();
    	if (this.right != null)
    		hash+=this.right.hashCode()*31;
    	if(hash>0)
    		return hash;
    	
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode();
    }

}
