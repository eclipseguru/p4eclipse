/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures.theme;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IThemeListener {

    /**
     * Set background color
     * 
     * @param color
     */
    void setBackgroundColor(Color color);

    /**
     * Set foreground color
     * 
     * @param color
     */
    void setForegroundColor(Color color);

    /**
     * Set font
     * 
     * @param font
     */
    void setFont(Font font);

    /**
     * Set outline color
     * 
     * @param color
     */
    void setOutlineColor(Color color);

}
