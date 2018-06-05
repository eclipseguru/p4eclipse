/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.ui.IEnableDisplay;

import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IColorPainter extends IEnableDisplay {

    /**
     * Dispose of the color painter
     */
    void dispose();

    /**
     * Get the color for a line number
     * 
     * @param lineNumber
     * @return - color or null
     */
    Color getColor(int lineNumber);

    /**
     * Load the colors for the specified data object.
     * 
     * @param data
     */
    void loadColors(Object data);

}
