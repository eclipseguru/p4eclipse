/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Diff configuration
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IDiffConfiguration extends IWorkbenchAdapter, IAdaptable {

    /**
     * Get filter option
     * 
     * @return non-null filter options
     */
    IFilterOptions getOptions();

    /**
     * Set label
     * 
     * @param label
     */
    void setLabel(String label);

    /**
     * Set image descriptor
     * 
     * @param descriptor
     */
    void setImageDescriptor(ImageDescriptor descriptor);

}