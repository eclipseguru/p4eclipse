/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class ColorPainter implements IColorPainter {

    private Color[] colors = null;

    private boolean enabled = true;

    /**
     * @see com.perforce.team.ui.text.timelapse.IColorPainter#dispose()
     */
    public void dispose() {
        if (this.colors != null) {
            for (Color color : this.colors) {
                color.dispose();
            }
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.IColorPainter#getColor(int)
     */
    public Color getColor(int lineNumber) {
        if (enabled && this.colors != null) {
            lineNumber = Math.min(lineNumber, this.colors.length - 1);
            if (lineNumber > -1) {
                return this.colors[lineNumber];
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.IColorPainter#loadColors(java.lang.Object)
     */
    public void loadColors(Object data) {
        this.colors = internalLoadColors(data);
    }

    /**
     * Loads colors and return line array of colors
     * 
     * @param data
     * @return - color array
     */
    protected abstract Color[] internalLoadColors(Object data);

    /**
     * @see com.perforce.team.ui.IEnableDisplay#isEnabled()
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @see com.perforce.team.ui.IEnableDisplay#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
