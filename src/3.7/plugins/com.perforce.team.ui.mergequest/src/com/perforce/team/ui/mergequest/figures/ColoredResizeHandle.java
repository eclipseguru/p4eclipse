/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ColoredResizeHandle extends ResizeHandle {

    /**
     * Fills the part with handles on the corners
     * 
     * @param part
     * @param handles
     */
    public static void addCornerHandles(GraphicalEditPart part,
            List<Handle> handles) {
        handles.add(new ColoredResizeHandle(part, PositionConstants.SOUTH_EAST));
        handles.add(new ColoredResizeHandle(part, PositionConstants.SOUTH));
        handles.add(new ColoredResizeHandle(part, PositionConstants.SOUTH_WEST));
        handles.add(new ColoredResizeHandle(part, PositionConstants.WEST));
        handles.add(new ColoredResizeHandle(part, PositionConstants.NORTH_WEST));
        handles.add(new ColoredResizeHandle(part, PositionConstants.NORTH));
        handles.add(new ColoredResizeHandle(part, PositionConstants.NORTH_EAST));
        handles.add(new ColoredResizeHandle(part, PositionConstants.EAST));
    }

    /**
     * @param owner
     * @param direction
     */
    public ColoredResizeHandle(GraphicalEditPart owner, int direction) {
        super(owner, direction);
    }

    /**
     * @see org.eclipse.gef.handles.SquareHandle#getFillColor()
     */
    @Override
    protected Color getFillColor() {
        return getForegroundColor();
    }

}
