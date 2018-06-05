/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.team.ui.P4UIUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Resizer extends MouseAdapter implements MouseMoveListener {

    /**
     * SPLIT
     */
    public static final double SPLIT = 0.5;

    private class ResizerLayout extends Layout {

        /**
         * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
         *      int, int, boolean)
         */
        @Override
        public Point computeSize(Composite c, int w, int h, boolean force) {
            return new Point(100, 100);
        }

        /**
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
         *      boolean)
         */
        @Override
        public void layout(Composite composite, boolean force) {
            Rectangle space = composite.getClientArea();
            int width = space.width - (padding * 2);
            int height = space.height - padding;
            if (width <= 0 || height <= 0) {
                return;
            }
            int middleSize = ((GridData) middle.getLayoutData()).widthHint;

            int leftSize = (int) (width * split - middleSize / 2.0);
            left.setBounds(padding, padding, leftSize, height);
            middle.setBounds(leftSize + padding, padding, middleSize, height);

            int rightSize = (int) (width * (1 - split) - middleSize / 2.0);
            right.setBounds(leftSize + padding + middleSize, padding,
                    rightSize, height);
        }
    }

    private Composite left;
    private Composite middle;
    private Composite right;
    private boolean down = false;
    private double split = SPLIT;
    private int width1 = 0;
    private int width2 = 0;
    private int padding = 2;
    private int x = 0;

    /**
     * Create new resizer
     * 
     * @param left
     * @param middle
     * @param right
     */
    public Resizer(Composite left, Composite middle, Composite right) {
        Cursor cursor = new Cursor(middle.getDisplay(), SWT.CURSOR_SIZEWE);
        P4UIUtils.registerDisposal(middle, cursor);
        middle.setCursor(cursor);
        this.left = left;
        this.right = right;
        this.middle = middle;
        this.middle.addMouseListener(this);
        this.middle.addMouseMoveListener(this);
        this.middle.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                Resizer.this.middle = null;
            }
        });
        this.left.getParent().setLayout(new ResizerLayout());
    }

    /**
     * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseDoubleClick(MouseEvent e) {
        split = SPLIT;
        this.left.getParent().layout(true);
    }

    /**
     * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseDown(MouseEvent e) {
        width1 = left.getSize().x;
        width2 = right.getSize().x;
        x = e.x;
        down = true;
    }

    /**
     * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseUp(MouseEvent e) {
        down = false;
    }

    /**
     * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
     */
    public void mouseMove(MouseEvent e) {
        if (down) {
            resize(e);
        }
    }

    private void resize(MouseEvent e) {
        int dx = e.x - x;
        int centerWidth = middle.getSize().x;

        if (width1 + dx > centerWidth && width2 - dx > centerWidth) {
            width1 += dx;
            width2 -= dx;
            split = (double) width1 / (double) (width1 + width2);
            split = Math.min(split, .9);
            split = Math.max(split, .1);
        }

        this.left.getParent().layout(true);
        this.left.getParent().update();
    }

}
