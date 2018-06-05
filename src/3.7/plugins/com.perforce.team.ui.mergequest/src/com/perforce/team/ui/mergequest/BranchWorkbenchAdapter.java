/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.registry.BranchRegistry;
import com.perforce.team.ui.mergequest.descriptors.DescriptorRegistry;
import com.perforce.team.ui.mergequest.descriptors.ElementDescriptor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchWorkbenchAdapter implements IWorkbenchAdapter {

    private static DescriptorRegistry registry;

    /**
     * Get descriptor registry for branches
     * 
     * @return descriptor registry for branches
     */
    public static DescriptorRegistry getDescriptorRegistry() {
        if (registry == null) {
            registry = new DescriptorRegistry(
                    P4BranchGraphPlugin.ELEMENT_DESCRIPTORS_ID,
                    BranchRegistry.BRANCH_ELEMENT);
        }
        return registry;
    }

    /**
     * Get branch descriptors
     * 
     * @return non-null but possibly empty array of branch descriptors
     */
    public static ElementDescriptor[] getBranchDescriptors() {
        return getDescriptorRegistry().getDescriptors();
    }

    /**
     * Get type image descriptor
     * 
     * @param type
     * @return - image descriptor
     */
    public static ImageDescriptor getTypeDescriptor(String type) {
        ImageDescriptor descriptor = null;
        if (type != null) {
            ElementDescriptor elementDescriptor = getDescriptorRegistry()
                    .getDescriptor(type);
            if (elementDescriptor != null) {
                descriptor = elementDescriptor.getIcon();
            }
        }
        return descriptor;
    }

    /**
     * Is the type important?
     * 
     * @param type
     * @return true if important, false otherwise
     */
    public static boolean isImportant(String type) {
        boolean important = false;
        if (type != null) {
            ElementDescriptor elementDescriptor = getDescriptorRegistry()
                    .getDescriptor(type);
            if (elementDescriptor != null) {
                important = elementDescriptor.isImportant();
            }
        }
        return important;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return ((Branch) o).getAllMappings();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return getTypeDescriptor(((Branch) object).getType());
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return ((Branch) o).getName();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return ((Branch) o).getGraph();
    }

}
