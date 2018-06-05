/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.tooltip;

import com.perforce.team.core.mergequest.model.IBranchGraphElement;

import org.eclipse.draw2d.MouseEvent;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IToolTipManager {

    /**
     * Show the tooltip for the element
     * 
     * @param element
     * @param event
     */
    void showToolTip(IBranchGraphElement element, MouseEvent event);

    /**
     * Hide tooltip for the element
     * 
     * @param element
     */
    void hideToolTip(IBranchGraphElement element);

    /**
     * Dispose of the manager
     */
    void dispose();

}
