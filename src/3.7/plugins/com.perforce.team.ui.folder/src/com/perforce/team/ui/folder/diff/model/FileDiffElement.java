/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.model;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Revision;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileDiffElement extends WorkbenchAdapter implements IAdaptable {

    private IWorkbenchAdapter workbenchAdapter;
    private IDiffElement element;
    private IP4DiffFile parent;
    private Object[] children = null;

    /**
     * Create new file diff element
     * 
     * @param element
     * @param parent
     */
    public FileDiffElement(IDiffElement element, IP4DiffFile parent) {
        Assert.isNotNull(element, "Element cannot be null"); //$NON-NLS-1$
        Assert.isNotNull(parent, "Parent cannot be null"); //$NON-NLS-1$
        this.element = element;
        this.parent = parent;
        this.workbenchAdapter = P4CoreUtils.convert(this.element,
                IWorkbenchAdapter.class);
        if (this.workbenchAdapter == null) {
            Object platformAdapter = Platform.getAdapterManager().getAdapter(
                    this.element, IWorkbenchAdapter.class);
            if (platformAdapter instanceof IWorkbenchAdapter) {
                this.workbenchAdapter = (IWorkbenchAdapter) platformAdapter;
            }
        }
    }

    /**
     * Get diff element
     * 
     * @return diff element
     */
    public IDiffElement getElement() {
        return this.element;
    }

    private void loadChildren() {
        if (this.workbenchAdapter != null) {
            this.children = this.workbenchAdapter.getChildren(this.element);
            if (this.children.length > 0) {
                Object[] converted = new Object[this.children.length];
                int index = 0;
                for (Object child : this.children) {
                    if (child instanceof IDiffElement) {
                        child = new FileDiffElement((IDiffElement) child,
                                this.parent);
                    }
                    converted[index] = child;
                    index++;
                }
                this.children = converted;
            }
        } else {
            this.children = NO_CHILDREN;
        }
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object object) {
        if (this.children == null) {
            loadChildren();
        }
        return this.children;
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object object) {
        return this.workbenchAdapter != null ? this.workbenchAdapter
                .getLabel(this.element) : super.getLabel(this.element);
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        return this.workbenchAdapter != null ? this.workbenchAdapter
                .getImageDescriptor(this.element) : null;
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object object) {
        return this.parent;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (IDiffElement.class == adapter) {
            return this.element;
        }
        if (IP4DiffFile.class == adapter || IP4Resource.class == adapter) {
            return this.parent;
        }
        if (ICompareInput.class == adapter
                && this.element instanceof ICompareInput) {
            return this.element;
        }
        if (DiffNode.class == adapter && this.element instanceof DiffNode) {
            return this.element;
        }
        if (IP4Revision.class == adapter) {
            return this.parent.getAdapter(IP4Revision.class);
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof FileDiffElement) {
            FileDiffElement other = (FileDiffElement) obj;
            return this.parent.equals(other.parent)
                    && this.element.equals(other.element);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.element.hashCode();
    }

}
