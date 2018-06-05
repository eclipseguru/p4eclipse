/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.handles.ConnectionEndpointHandle;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingEndpointHandle extends ConnectionEndpointHandle {

    private int anchor = 0;

    /**
     * @param owner
     * @param fixed
     * @param endPoint
     * @param anchor
     */
    public MappingEndpointHandle(ConnectionEditPart owner, boolean fixed,
            int endPoint, int anchor) {
        super(owner, fixed, endPoint);
        this.anchor = anchor;
    }

    /**
     * 
     * @param owner
     * @param endPoint
     * @param anchor
     */
    public MappingEndpointHandle(ConnectionEditPart owner, int endPoint,
            int anchor) {
        super(owner, endPoint);
        this.anchor = anchor;
    }

    /**
     * 
     * @param endPoint
     */
    public MappingEndpointHandle(int endPoint) {
        super(endPoint);
    }

    /**
     * @see org.eclipse.gef.handles.SquareHandle#getFillColor()
     */
    @Override
    protected Color getFillColor() {
        return getForegroundColor();
    }

    private Rectangle getHandleBounds(Rectangle bounds) {
        Rectangle handleBounds = new Rectangle(bounds);
        if (anchor >= 0 && anchor <= 2) {
            // Top
            handleBounds.translate(-1, 1);
        } else if (anchor >= 3 && anchor <= 4) {
            // Right
            handleBounds.translate(-3, -1);
        } else if (anchor >= 5 && anchor <= 7) {
            // Bottom
            handleBounds.translate(-1, -3);
        } else if (anchor >= 8 && anchor <= 9) {
            // Left
            handleBounds.translate(1, -1);
        }
        return handleBounds;
    }

    /**
     * @see org.eclipse.draw2d.Figure#getBounds()
     */
    @Override
    public Rectangle getBounds() {
        return getHandleBounds(bounds);
    }

}
