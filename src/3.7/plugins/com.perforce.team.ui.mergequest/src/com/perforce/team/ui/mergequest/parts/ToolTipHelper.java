/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.ui.mergequest.tooltip.IToolTipManager;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ToolTipHelper {

    private AbstractGraphicalEditPart part;

    /**
     * Mouse motion listener
     */
    protected MouseMotionListener mouseListener = new MouseMotionListener() {

        public void mouseMoved(MouseEvent me) {

        }

        public void mouseHover(MouseEvent me) {
            showToolTip(me);
        }

        public void mouseExited(MouseEvent me) {
            hideToolTip();
        }

        public void mouseEntered(MouseEvent me) {

        }

        public void mouseDragged(MouseEvent me) {

        }
    };

    /**
     * Tool tip helper
     * 
     * @param part
     */
    public ToolTipHelper(AbstractGraphicalEditPart part) {
        this.part = part;
    }

    /**
     * Show tool tip
     * 
     * @param event
     */
    protected void showToolTip(MouseEvent event) {
        IBranchGraphElement element = P4CoreUtils.convert(this.part,
                IBranchGraphElement.class);
        if (element != null) {
            IToolTipManager manager = P4CoreUtils.convert(part.getViewer(),
                    IToolTipManager.class);
            if (manager != null) {
                manager.showToolTip(element, event);
            }
        }
    }

    /**
     * Hide tool tip
     */
    protected void hideToolTip() {
        IBranchGraphElement element = P4CoreUtils.convert(this.part,
                IBranchGraphElement.class);
        if (element != null) {
            IToolTipManager manager = P4CoreUtils.convert(part.getViewer(),
                    IToolTipManager.class);
            if (manager != null) {
                manager.hideToolTip(element);
            }
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    public void activate() {
        IFigure figure = part.getFigure();
        if (figure != null) {
            figure.addMouseMotionListener(mouseListener);
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    public void deactivate() {
        IFigure figure = part.getFigure();
        if (figure != null) {
            figure.removeMouseMotionListener(mouseListener);
        }
    }

}
