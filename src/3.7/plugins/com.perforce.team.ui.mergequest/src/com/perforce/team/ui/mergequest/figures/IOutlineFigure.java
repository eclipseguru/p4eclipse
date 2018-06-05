/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IOutlineFigure extends IFigure {

    /**
     * Set outline color
     * 
     * @param color
     */
    void setOutlineColor(Color color);

    /**
     * Get outline color which may be null
     * 
     * @return color
     */
    Color getOutlineColor();

}
