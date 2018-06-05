/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures.theme;

import com.perforce.team.ui.mergequest.figures.IOutlineFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FigureThemeHelper extends ThemeHelper implements
        IPropertyChangeListener {

    private IFigure figure;
    private IThemeListener figureListener = new ThemeListenerAdapter() {

        @Override
        public void setForegroundColor(Color color) {
            if (figure != null) {
                figure.setForegroundColor(color);
            }
        }

        @Override
        public void setFont(Font font) {
            if (figure != null) {
                figure.setFont(font);
                figure.invalidateTree();
            }
        }

        @Override
        public void setBackgroundColor(Color color) {
            if (figure != null) {
                figure.setBackgroundColor(color);
            }
        }

        @Override
        public void setOutlineColor(Color color) {
            if (figure instanceof IOutlineFigure) {
                ((IOutlineFigure) figure).setOutlineColor(color);
            }
        }

    };

    /**
     * Create a figure theme helper
     */
    public FigureThemeHelper() {
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
     * @param figure
     */
    public void setFigure(IFigure figure) {
        this.figure = figure;
    }

}
