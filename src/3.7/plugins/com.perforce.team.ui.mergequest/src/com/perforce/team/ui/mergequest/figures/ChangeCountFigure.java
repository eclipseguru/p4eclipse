/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangeCountFigure extends RoundedRectangle implements
        IOutlineFigure {

    private Label countLabel;
    private Color outlineColor;
    private SharedResources resources;

    /**
     * Create a change count figure
     * 
     * @param resources
     * 
     */
    public ChangeCountFigure(SharedResources resources) {
        this.resources = resources;
        this.outlineColor = ColorConstants.white;
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 1;
        layout.marginWidth = 1;
        layout.horizontalSpacing = 1;
        setLayoutManager(layout);
        setBorder(new MarginBorder(1));
        setOpaque(true);

        this.countLabel = new Label();
        this.countLabel.setIconAlignment(PositionConstants.EAST);
        this.countLabel.setIconTextGap(1);
        add(countLabel, new GridData(SWT.CENTER, SWT.CENTER, true, true));
    }

    /**
     * Get count label that displays the text and icon containing the count
     * information
     * 
     * @return count label
     */
    public Label getCountLabel() {
        return this.countLabel;
    }

    /**
     * Update count figure with specified change type and count
     * 
     * @param type
     * @param count
     */
    public void updateChanges(ChangeType type, int count) {
        if (type == null) {
            type = ChangeType.UNKNOWN;
        }

        switch (type) {
        case NO_CHANGES:
            this.countLabel.setIcon(this.resources
                    .getImage(IP4BranchGraphConstants.TASKS_NONE));
            setCount(0);
            break;
        case NO_PERMISSION:
            this.countLabel.setIcon(this.resources
                    .getImage(IP4BranchGraphConstants.TASKS_NO_PERMISSION));
            if (count > 0) {
                setCount(count);
            } else {
                setCount(Messages.ChangeCountFigure_Unknown);
            }
            break;
        case VISIBLE_CHANGES:
            this.countLabel.setIcon(this.resources
                    .getImage(IP4BranchGraphConstants.TASKS_VISIBLE));
            if (count > 0) {
                setCount(count);
            } else {
                setCount(Messages.ChangeCountFigure_OneOrMore);
            }
            break;
        case UNKNOWN:
            setVisible(false);
            break;
        default:
            break;
        }
    }

    /**
     * Set count label icon
     * 
     * @param image
     * 
     */
    public void setLabelIcon(Image image) {
        this.countLabel.setIcon(image);
    }

    /**
     * Set count
     * 
     * @param count
     */
    public void setCount(int count) {
        setCount(Integer.toString(count));
    }

    /**
     * Set count
     * 
     * @param count
     */
    public void setCount(String count) {
        this.countLabel.setText(count);
    }

    /**
     * @see org.eclipse.draw2d.RoundedRectangle#outlineShape(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
        if (this.outlineColor != null) {
            graphics.setForegroundColor(this.outlineColor);
        }
        super.outlineShape(graphics);
    }

    /**
     * @see com.perforce.team.ui.mergequest.figures.IOutlineFigure#setOutlineColor(org.eclipse.swt.graphics.Color)
     */
    public void setOutlineColor(Color color) {
        this.outlineColor = color;
        repaint();
    }

    /**
     * @see com.perforce.team.ui.mergequest.figures.IOutlineFigure#getOutlineColor()
     */
    public Color getOutlineColor() {
        return this.outlineColor;
    }

}
