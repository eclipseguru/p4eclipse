/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures.theme;

import com.perforce.team.ui.mergequest.figures.IOutlineFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CompositeFigureThemeHelper extends ThemeHelper {

    private IFigure[] figures;
    private IThemeListener figureListener = new ThemeListenerAdapter() {

        @Override
        public void setForegroundColor(Color color) {
            for (IFigure figure : figures) {
                figure.setForegroundColor(color);
            }
        }

        @Override
        public void setFont(Font font) {
            for (IFigure figure : figures) {
                figure.setFont(font);
                figure.invalidateTree();
            }
        }

        @Override
        public void setBackgroundColor(Color color) {
            for (IFigure figure : figures) {
                figure.setBackgroundColor(color);
            }
        }

        @Override
        public void setOutlineColor(Color color) {
            for (IFigure figure : figures) {
                if (figure instanceof IOutlineFigure) {
                    ((IOutlineFigure) figure).setOutlineColor(color);
                }
            }
        }

    };

    /**
     * Create a figure theme helper
     */
    public CompositeFigureThemeHelper() {
        this.figures = new IFigure[0];
        setListener(figureListener);
    }

    /**
     * @see com.perforce.team.ui.mergequest.figures.theme.ThemeHelper#setListener(com.perforce.team.ui.mergequest.figures.theme.IThemeListener)
     */
    @Override
    public void setListener(IThemeListener listener) {
        if (listener == figureListener) {
            super.setListener(listener);
        }
    }

    /**
     * Set the figure
     * 
     * @param figures
     */
    public void setFigures(IFigure[] figures) {
        if (figures == null) {
            figures = new IFigure[0];
        }
        this.figures = figures;
    }

}
