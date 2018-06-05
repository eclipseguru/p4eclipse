/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.BranchWorkbenchAdapter;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.handles.HandleBounds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchFigure extends RoundedRectangle implements IOutlineFigure,
        HandleBounds {

    private BranchWorkbenchAdapter adapter;
    private SharedResources resources;
    private Branch branch;
    private Label label;
    private Color outlineColor;
    private List<MappingConnectionAnchor> anchors = new ArrayList<MappingConnectionAnchor>();

    /**
     * 
     * @param branch
     * @param resources
     */
    public BranchFigure(Branch branch, SharedResources resources) {
        this.adapter = new BranchWorkbenchAdapter();
        this.resources = resources;
        this.branch = branch;
        this.setBorder(new BranchBorder());
        this.setOpaque(true);

        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        this.setLayoutManager(layout);

        label = new Label();
        label.setTextPlacement(PositionConstants.EAST);
        label.setTextAlignment(PositionConstants.CENTER);
        label.setIconAlignment(PositionConstants.CENTER);
        label.setBorder(null);
        this.add(label, new GridData(SWT.CENTER, SWT.CENTER, true, true));

        int count;
        for (count = 0; count < 3; count++) {
            anchors.add(new MappingConnectionAnchor(this, count, SWT.TOP));
        }
        for (int i = 0; i < 2; count++, i++) {
            anchors.add(new MappingConnectionAnchor(this, count, SWT.RIGHT));
        }
        for (int i = 0; i < 3; count++, i++) {
            anchors.add(new MappingConnectionAnchor(this, count, SWT.BOTTOM));
        }
        for (int i = 0; i < 2; count++, i++) {
            anchors.add(new MappingConnectionAnchor(this, count, SWT.LEFT));
        }

        refresh();
    }

    /**
     * Show anchor of branch figure
     * 
     * @param show
     */
    public void showAnchors(boolean show) {
        if (((BranchBorder) getBorder()).showAnchors(show)) {
            repaint();
        }
    }

    /**
     * @see org.eclipse.draw2d.Figure#validate()
     */
    @Override
    public void validate() {
        if (isValid()) {
            return;
        }
        refreshAnchors();
        super.validate();
    }

    /**
     * Get anchor at index
     * 
     * @param mapping
     * @return mapping connection anchor
     */
    public MappingConnectionAnchor getAnchor(Mapping mapping) {
        MappingConnectionAnchor anchor = null;
        if (branch.equals(mapping.getSource())) {
            anchor = anchors.get(mapping.getSourceAnchor());
        } else if (branch.equals(mapping.getTarget())) {
            anchor = anchors.get(mapping.getTargetAnchor());
        }
        return anchor;
    }

    /**
     * Get anchor closest to point
     * 
     * @param point
     * @return anchor
     */
    public MappingConnectionAnchor getAnchor(Point point) {
        MappingConnectionAnchor closest = null;
        long minDistance = Long.MAX_VALUE;

        for (MappingConnectionAnchor anchor : this.anchors) {
            Point location = anchor.getLocation(null);
            long distance = point.getDistance2(location);
            if (distance < minDistance) {
                minDistance = distance;
                closest = anchor;
            }
        }
        return closest;
    }

    private void refreshAnchors() {
        Dimension size = getSize();
        int xSpacing = size.width / 4;
        int ySpacing = size.height / 3;

        int count = 0;
        // Top
        for (int i = 1; i < 4; i++, count++) {
            anchors.get(count).setXOffset(i * xSpacing + 1).setYOffset(0);
        }

        // Right
        for (int i = 1; i < 3; i++, count++) {
            anchors.get(count).setYOffset(i * ySpacing + 1)
                    .setXOffset(size.width - 1);
        }

        // Bottom
        for (int i = 1; i < 4; i++, count++) {
            anchors.get(count).setXOffset(i * xSpacing + 1)
                    .setYOffset(size.height - 1);
        }

        // Left
        for (int i = 1; i < 3; i++, count++) {
            anchors.get(count).setYOffset(i * ySpacing + 1).setXOffset(0);
        }

    }

    /**
     * @see org.eclipse.draw2d.RoundedRectangle#outlineShape(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
    	// coverity: Avoid CALL_SUPER 
    	if(getBorder()==null)
    		super.outlineShape(graphics);
    	else{
    		// Do not outline shape, border is responsible for outline
    	}
    }

    /**
     * @see Shape#fillShape(Graphics)
     */
    @Override
    protected void fillShape(Graphics graphics) {
        Rectangle fillBounds = new Rectangle(getBounds()).crop(new Insets(2));
        graphics.fillRoundRectangle(fillBounds, corner.width, corner.height);
    }

    /**
     * Refresh the branch figure
     */
    public void refresh() {
        label.setText(adapter.getLabel(branch));
        label.setIcon(resources.getImage(adapter.getImageDescriptor(branch)));
        refreshAnchors();
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

    /**
     * @see org.eclipse.gef.handles.HandleBounds#getHandleBounds()
     */
    public Rectangle getHandleBounds() {
        return getBounds().getCropped(new Insets(1));
    }

}
