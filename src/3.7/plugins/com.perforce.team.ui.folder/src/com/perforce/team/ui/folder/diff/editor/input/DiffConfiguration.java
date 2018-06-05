/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffConfiguration extends WorkbenchAdapter implements
        IDiffConfiguration {

    private String label;
    private ImageDescriptor descriptor;
    private IFilterOptions options;

    /**
     * Create diff configuration
     */
    public DiffConfiguration() {
        this(null, null);
    }

    /**
     * Create diff configuration
     * 
     * @param label
     */
    public DiffConfiguration(String label) {
        this(label, null);
    }

    /**
     * Create diff configuration
     * 
     * @param label
     * @param descriptor
     */
    public DiffConfiguration(String label, ImageDescriptor descriptor) {
        this.label = label;
        if (this.label == null) {
            this.label = ""; //$NON-NLS-1$
        }
        this.descriptor = descriptor;
        this.options = new FilterOptions();
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        return this.descriptor;
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object object) {
        return this.label;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IDiffConfiguration#getOptions()
     */
    public IFilterOptions getOptions() {
        return this.options;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IDiffConfiguration#setLabel(java.lang.String)
     */
    public void setLabel(String label) {
        if (label == null) {
            label = ""; //$NON-NLS-1$
        }
        this.label = label;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.IDiffConfiguration#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
     */
    public void setImageDescriptor(ImageDescriptor descriptor) {
        this.descriptor = descriptor;
    }

}
