/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingConnectionAnchor extends AbstractConnectionAnchor {

    private int index = 0;
    private int style = 0;
    private int xOffset = 0;
    private int yOffset = 0;

    /**
     * Create mapping connection anchor
     * 
     * @param owner
     * @param index
     * @param style
     */
    public MappingConnectionAnchor(IFigure owner, int index, int style) {
        super(owner);
        this.style = style;
        this.index = index;
    }

    /**
     * Is top anchor
     * 
     * @return true if on top
     */
    public boolean isTop() {
        return (SWT.TOP & style) != 0;
    }

    /**
     * Is bottom anchor
     * 
     * @return true if on bottom
     */
    public boolean isBottom() {
        return (SWT.BOTTOM & style) != 0;
    }

    /**
     * Is left anchor
     * 
     * @return true if on left
     */
    public boolean isLeft() {
        return (SWT.LEFT & style) != 0;
    }

    /**
     * Is right anchor
     * 
     * @return true if on right
     */
    public boolean isRight() {
        return (SWT.RIGHT & style) != 0;
    }

    /**
     * Get index of anchor
     * 
     * @return index
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Set x offset
     * 
     * @param x
     * @return this
     */
    public MappingConnectionAnchor setXOffset(int x) {
        this.xOffset = x;
        fireAnchorMoved();
        return this;
    }

    /**
     * Set y offset
     * 
     * @param y
     * @return this
     */
    public MappingConnectionAnchor setYOffset(int y) {
        this.yOffset = y;
        fireAnchorMoved();
        return this;
    }

    /**
     * @see org.eclipse.draw2d.AbstractConnectionAnchor#getReferencePoint()
     */
    @Override
    public Point getReferencePoint() {
        return getLocation(null);
    }

    /**
     * @see org.eclipse.draw2d.ConnectionAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
     */
    public Point getLocation(Point reference) {
        IFigure owner = getOwner();
        Rectangle r = owner.getBounds();
        int y = r.y + yOffset;
        int x = r.x + xOffset;
        Point p = new PrecisionPoint(x, y);
        owner.translateToAbsolute(p);
        return p;
    }

}
