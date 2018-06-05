/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchBorder extends AbstractBorder {

    private static Insets insets = new Insets(10);

    /**
     * ARC
     */
    public static final int ARC = 8;

    /**
     * ANCHOR_SIZE
     */
    public static final int ANCHOR_SIZE = 4;

    /**
     * PADDING
     */
    public static final int PADDING = ANCHOR_SIZE / 2;

    private boolean drawAnchors = false;

    private void drawConnectors(Graphics g, Rectangle rec) {
        int y1 = rec.y;
        int x1 = 0;
        int y2 = y1 + rec.height - ANCHOR_SIZE - 1;

        Insets insets = new Insets(PADDING, PADDING, PADDING + 1, PADDING + 1);
        g.drawRoundRectangle(new Rectangle(rec).crop(insets), ARC, ARC);
        Color foreground = g.getForegroundColor();
        g.setForegroundColor(ColorConstants.white);
        insets.add(new Insets(1));
        g.drawRoundRectangle(new Rectangle(rec).crop(insets), ARC, ARC);
        if (drawAnchors) {
            g.setBackgroundColor(foreground);
            Rectangle r1 = new Rectangle(x1, y1, ANCHOR_SIZE, ANCHOR_SIZE);
            Rectangle r2 = new Rectangle(x1, y2, ANCHOR_SIZE, ANCHOR_SIZE);
            for (int i = 1; i < 4; i++) {
                x1 = rec.x + i * rec.width / 4 - PADDING;
                r1.x = x1;
                r2.x = x1;
                g.fillRectangle(r1);
                g.fillRectangle(r2);
                g.drawRectangle(r1);
                g.drawRectangle(r2);
            }
            x1 = rec.x;
            int x2 = x1 + rec.width - ANCHOR_SIZE - 1;
            r1 = new Rectangle(x1, y1, ANCHOR_SIZE, ANCHOR_SIZE);
            r2 = new Rectangle(x2, y1, ANCHOR_SIZE, ANCHOR_SIZE);
            for (int i = 1; i < 3; i++) {
                y1 = rec.y + i * rec.height / 3 - PADDING;
                r1.y = y1;
                r2.y = y1;
                g.fillRectangle(r1);
                g.fillRectangle(r2);
                g.drawRectangle(r1);
                g.drawRectangle(r2);
            }
        }
    }

    /**
     * 
     * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
     */
    public Insets getInsets(IFigure figure) {
        return insets;
    }

    /**
     * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
     *      org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
     */
    public void paint(IFigure figure, Graphics g, Insets in) {
        Rectangle r = figure.getBounds().getCropped(in);
        Color outlineColor = ((IOutlineFigure) figure).getOutlineColor();
        if (outlineColor == null) {
            outlineColor = ColorConstants.gray;
        }
        g.setForegroundColor(outlineColor);
        g.setBackgroundColor(figure.getBackgroundColor());
        drawConnectors(g, r);
    }

    /**
     * @param show
     * @return true if change, false if unchanged
     */
    public boolean showAnchors(boolean show) {
        if (drawAnchors != show) {
            this.drawAnchors = show;
            return true;
        } else {
            return false;
        }
    }
}