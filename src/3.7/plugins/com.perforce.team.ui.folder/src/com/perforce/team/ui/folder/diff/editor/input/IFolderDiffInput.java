/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IFolderDiffInput extends IEditorInput {

    /**
     * LEFT_OPTIONS
     */
    String LEFT_OPTIONS = "leftOptions"; //$NON-NLS-1$

    /**
     * RIGHT_OPTIONS
     */
    String RIGHT_OPTIONS = "rightOptions"; //$NON-NLS-1$

    /**
     * Get header configuration
     * 
     * @return non-null configuration
     */
    IDiffConfiguration getHeaderConfiguration();

    /**
     * Get left-side diff configuration
     * 
     * @return non-null configuration
     */
    IDiffConfiguration getLeftConfiguration();

    /**
     * Get right-side diff configuration
     * 
     * @return non-null configuration
     */
    IDiffConfiguration getRightConfiguration();

    /**
     * Generate a diff container for this input
     * 
     * @param leftFilter
     * @param rightFilter
     * @param monitor
     * @return non-null file diff container
     */
    FileDiffContainer generateDiffs(String leftFilter, String rightFilter,
            IProgressMonitor monitor);

    /**
     * Refresh the input. This method should not be called from the UI-thread
     * since it may involve loading server objects. The editor using this input
     * should react to any left, right, or header configuration changes that
     * occur because of this refresh.
     * 
     * @param monitor
     */
    void refreshInput(IProgressMonitor monitor);

    /**
     * Get input connection
     * 
     * @return p4 connection
     */
    IP4Connection getConnection();

}
