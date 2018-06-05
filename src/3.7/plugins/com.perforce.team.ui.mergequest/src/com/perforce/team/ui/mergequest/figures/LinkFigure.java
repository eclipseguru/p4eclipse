/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import com.perforce.team.ui.P4UIUtils;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.SharedCursors;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LinkFigure extends Label {

    private IAction action;

    /**
     * Create a link
     */
    public LinkFigure() {
        this(""); //$NON-NLS-1$
    }

    /**
     * Create a link
     * 
     * @param text
     */
    public LinkFigure(String text) {
        super(text);
        setForegroundColor(P4UIUtils.getDisplay().getSystemColor(
                SWT.COLOR_DARK_BLUE));
        setCursor(SharedCursors.HAND);
        this.addMouseListener(new MouseListener() {

            public void mouseReleased(MouseEvent me) {

            }

            public void mousePressed(MouseEvent me) {
                runAction();
            }

            public void mouseDoubleClicked(MouseEvent me) {

            }
        });
    }

    /**
     * Set on click action to run
     * 
     * @param action
     */
    public void setAction(IAction action) {
        this.action = action;
    }

    private void runAction() {
        if (this.action != null) {
            this.action.run();
        }
    }

    /**
     * @see org.eclipse.draw2d.Label#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        Rectangle bounds = getBounds();
        graphics.translate(bounds.x, bounds.y);
        graphics.drawLine(getIconBounds().width, bounds.height - 1,
                bounds.width, bounds.height - 1);
        graphics.translate(-bounds.x, -bounds.y);
    }

}