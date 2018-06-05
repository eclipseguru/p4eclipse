/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.tooltip;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseToolTip extends ToolTip {

    private Control control;
    private DisposeListener disposeListener = new DisposeListener() {

        public void widgetDisposed(DisposeEvent e) {
            hide();
        }
    };

    /**
     * Create tooltip
     * 
     * @param control
     */
    public BaseToolTip(Control control) {
        super(control, NO_RECREATE, true);
        this.control = control;
        this.control.addDisposeListener(disposeListener);
    }

    /**
     * Create inner content
     * 
     * @param parent
     */
    protected abstract void createInnerContent(Composite parent);

    /**
     * @see org.eclipse.jface.window.ToolTip#createToolTipContentArea(org.eclipse.swt.widgets.Event,
     *      org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Composite createToolTipContentArea(Event event, Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(GridLayoutFactory.swtDefaults().create());
        displayArea.setForeground(ColorConstants.tooltipForeground);
        displayArea.setBackground(ColorConstants.tooltipBackground);
        displayArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
        displayArea.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                control.removeDisposeListener(disposeListener);
            }
        });

        createInnerContent(displayArea);

        return displayArea;
    }
}
