/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DecorationLabel {

    private Composite area;
    private String leftText;
    private String rightText;
    private Label left;
    private Label middle;
    private Label right;

    /**
     * Create a decoration label wrapped with '(' and ')'
     * 
     * @param parent
     */
    public DecorationLabel(Composite parent) {
        this(parent, "(", ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Create a decoration label
     * 
     * @param parent
     * @param left
     * @param right
     */
    public DecorationLabel(Composite parent, String left, String right) {
        this.leftText = left;
        this.rightText = right;
        area = new Composite(parent, SWT.NONE);
        GridLayout aLayout = new GridLayout(3, false);
        aLayout.marginWidth = 0;
        aLayout.marginHeight = 0;
        aLayout.verticalSpacing = 0;
        aLayout.horizontalSpacing = 0;
        area.setLayout(aLayout);
        this.left = new Label(area, SWT.NONE);
        this.middle = new Label(area, SWT.NONE);
        Color decorationColor = JFaceResources.getColorRegistry().get(
                JFacePreferences.DECORATIONS_COLOR);
        this.middle.setForeground(decorationColor);
        this.right = new Label(area, SWT.NONE);
    }

    /**
     * Set foreground color of non-decorated text
     * 
     * @param color
     */
    public void setForeground(Color color) {
        left.setForeground(color);
        right.setForeground(color);
    }

    /**
     * Get main control
     * 
     * @return - composite
     */
    public Composite getControl() {
        return this.area;
    }

    /**
     * Set the label text
     * 
     * @param text
     */
    public void setText(String text) {
        if (text == null || text.length() == 0) {
            left.setText(""); //$NON-NLS-1$
            middle.setText(""); //$NON-NLS-1$
            right.setText(""); //$NON-NLS-1$
        } else {
            left.setText(this.leftText);
            middle.setText(text);
            right.setText(this.rightText);
        }
    }
}
