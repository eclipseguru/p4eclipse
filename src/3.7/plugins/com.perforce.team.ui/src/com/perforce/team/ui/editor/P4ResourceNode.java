/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.ui.dialogs.BufferedResourceNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 *
 */
/**
 * A buffer for a workspace resource.
 */
public class P4ResourceNode extends ResourceNode implements IP4CompareNode {

    private boolean dirty = false;
    private IFile deleteFile;
    private String label = null;

    /**
     * 
     * @param resource
     *            the resource
     * @param label
     */
    public P4ResourceNode(IFile resource, String label) {
        super(resource);
        this.label = label;
    }

    /**
     * @see org.eclipse.compare.ResourceNode#createChild(org.eclipse.core.resources.IResource)
     */
    @Override
    protected IStructureComparator createChild(IResource child) {
        return new BufferedResourceNode(child);
    }

    /**
     * @see org.eclipse.compare.BufferedContent#setContent(byte[])
     */
    @Override
    public void setContent(byte[] contents) {
        dirty = true;
        super.setContent(contents);
    }

    /**
     * Commits buffered contents to resource.
     * 
     * @see com.perforce.team.ui.editor.IP4CompareNode#commit(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void commit(IProgressMonitor pm) throws CoreException {
        if (dirty) {

            if (deleteFile != null) {
                deleteFile.delete(true, true, pm);
                return;
            }

            IResource resource = getResource();
            if (resource instanceof IFile) {
                ByteArrayInputStream is = new ByteArrayInputStream(getContent());
                try {
                    IFile file = (IFile) resource;
                    if (file.exists()) {
                        file.setContents(is, false, true, pm);
                    } else {
                        file.create(is, false, pm);
                    }
                    dirty = false;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }
    }

    /**
     * @see org.eclipse.compare.ResourceNode#replace(org.eclipse.compare.ITypedElement,
     *      org.eclipse.compare.ITypedElement)
     */
    @Override
    public ITypedElement replace(ITypedElement child, ITypedElement other) {

        if (child == null) { // add resource
            // create a node without a resource behind it!
            IResource resource = getResource();
            if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;
                IFile file = folder.getFile(other.getName());
                child = new BufferedResourceNode(file);
            }
        }

        if (other == null && child!=null) { // delete resource
            IResource resource = getResource();
            if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;
                IFile file = folder.getFile(child.getName());
                if (file != null && file.exists()) {
                    deleteFile = file;
                    dirty = true;
                }
            }
            return null;
        }

        if (other instanceof IStreamContentAccessor
                && child instanceof IEditableContent) {
            IEditableContent dst = (IEditableContent) child;

            try {
                InputStream is = ((IStreamContentAccessor) other).getContents();
                byte[] bytes = readBytes(is);
                if (bytes != null) {
                    dst.setContent(bytes);
                }
            } catch (CoreException ex) {
            }
        }
        return child;
    }

    private static byte[] readBytes(InputStream in) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while (true) {
                int c = in.read();
                if (c == -1) {
                    break;
                }
                bos.write(c);
            }

        } catch (IOException ex) {
            return null;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException x) {
                }
            }
            try {
                bos.close();
            } catch (IOException x) {
            }
        }

        return bos.toByteArray();
    }

    /**
     * @see com.perforce.team.ui.editor.IP4CompareNode#getLabel()
     */
    public String getLabel() {
        return this.label;
    }


    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
    	return obj instanceof P4ResourceNode && super.equals(obj) && P4CoreUtils.equals(this.label, ((P4ResourceNode) obj).getLabel());
    }
    
    @Override
    public int hashCode() {
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return label==null?super.hashCode():super.hashCode()+label.hashCode()*31;
    }

}
