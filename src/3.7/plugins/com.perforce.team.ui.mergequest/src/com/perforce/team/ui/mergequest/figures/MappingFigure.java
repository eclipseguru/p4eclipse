/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.figures;

import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.RoutingAnimator;
import org.eclipse.gef.SharedCursors;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingFigure extends PolylineConnection {

    /**
     * INDICATOR_OFFSET
     */
    public static final int INDICATOR_OFFSET = 25;

    /**
     * COUNT_OFFSET
     */
    public static final int COUNT_OFFSET = 40;

    /**
     * INACTIVE_LINE_WIDTH
     */
    public static final int INACTIVE_LINE_WIDTH = 2;

    /**
     * ACTIVE_LINE_WIDTH
     */
    public static final int ACTIVE_LINE_WIDTH = 4;

    private SharedResources resources;
    private ChangeCountFigure sourceCountLabel;
    private RotatableDecoration sourceDecoration;
    private ChangeCountFigure targetCountLabel;
    private RotatableDecoration targetDecoration;
    private RoundedRectangle mappingLabel;
    private IColorProvider colorProvider;

    /**
     * Create mapping figure
     * 
     * @param mapping
     * @param resources
     */
    public MappingFigure(Mapping mapping, SharedResources resources) {
        this.resources = resources;
        this.setLineWidth(INACTIVE_LINE_WIDTH);
        this.setAntialias(SWT.ON);
        this.setCursor(SharedCursors.HAND);
        this.setOpaque(true);
        this.setBackgroundColor(ColorConstants.white);
        this.addRoutingListener(RoutingAnimator.getDefault());

        this.sourceCountLabel = new ChangeCountFigure(this.resources);
        this.add(this.sourceCountLabel,
                createEndpointLocator(COUNT_OFFSET, ConnectionLocator.SOURCE));

        this.targetCountLabel = new ChangeCountFigure(this.resources);
        this.add(this.targetCountLabel,
                createEndpointLocator(COUNT_OFFSET, ConnectionLocator.TARGET));

        if (mapping instanceof DepotPathMapping) {
            addMappingTypeLabel(resources
                    .getImage(IP4BranchGraphConstants.DEPOT_PATH_MAPPING));
        } else if (mapping instanceof BranchSpecMapping) {
            addMappingTypeLabel(resources.getImage(PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_BRANCH)));
        }

        sourceDecoration = createDecoration();
        targetDecoration = createDecoration();

        this.setSourceDecoration(sourceDecoration);
        this.setTargetDecoration(targetDecoration);
    }

    /**
     * Select the mapping
     */
    public void select() {
        setLineWidth(ACTIVE_LINE_WIDTH);
    }

    /**
     * Deselect the mapping
     */
    public void deselect() {
        setLineWidth(INACTIVE_LINE_WIDTH);
    }

    private Locator createEndpointLocator(int offset, int alignment) {
        return createEndpointLocator(offset, offset, alignment);
    }

    private Locator createEndpointLocator(int x, int y, int alignment) {
        MappingEndpointLocator locator = new MappingEndpointLocator(this,
                alignment);
        locator.setOffsets(x, y);
        return locator;
    }

    /**
     * Set color provider
     * 
     * @param provider
     */
    public void setColorProvider(IColorProvider provider) {
        this.colorProvider = provider;
    }

    /**
     * Set whether or not to outline the mapping label
     * 
     * @param outline
     */
    public void setOutlineMappingLabel(boolean outline) {
        this.mappingLabel.setOutline(outline);
    }

    private void addMappingTypeLabel(Image image) {
        this.mappingLabel = new RoundedRectangle();
        this.mappingLabel.setOpaque(true);
        Label label = new Label(image);
        this.mappingLabel.setBorder(new MarginBorder(1));
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        this.mappingLabel.setLayoutManager(layout);
        this.mappingLabel.add(label);
        this.add(this.mappingLabel, new ConnectionLocator(this,
                ConnectionLocator.MIDDLE));
    }

    private RotatableDecoration createDecoration() {
        PolygonDecoration decoration = new PolygonDecoration();
        decoration.setLineWidth(getLineWidth());
        return decoration;
    }

    /**
     * Update mapping status displayed on figure
     * 
     * @param mapping
     */
    public void updateStatus(Mapping mapping) {
        ChangeType sourceType = mapping.getSourceChange();
        ChangeType targetType = mapping.getTargetChange();

        int lineStyle = SWT.LINE_SOLID;

        Direction direction = mapping.getDirection();
        boolean source = direction == Direction.BOTH
                || direction == Direction.SOURCE;
        boolean target = direction == Direction.BOTH
                || direction == Direction.TARGET;

        ChangeType type = ChangeType.NO_CHANGES;

        if ((source && sourceType == ChangeType.NO_PERMISSION)
                || (target && targetType == ChangeType.NO_PERMISSION)) {
            lineStyle = SWT.LINE_DASH;
            type = ChangeType.NO_PERMISSION;
        }

        if ((source && sourceType == ChangeType.VISIBLE_CHANGES)
                || (target && targetType == ChangeType.VISIBLE_CHANGES)) {
            lineStyle = SWT.LINE_SOLID;
            type = ChangeType.VISIBLE_CHANGES;
        }

        if (colorProvider != null) {
            Color foreground = colorProvider.getForeground(type);
            setForegroundColor(foreground);
            sourceCountLabel.setForegroundColor(colorProvider
                    .getForeground(sourceType));
            targetCountLabel.setForegroundColor(colorProvider
                    .getForeground(targetType));
        }

        setLineStyle(lineStyle);

        sourceCountLabel.setVisible(source);
        sourceDecoration.setVisible(source);

        targetCountLabel.setVisible(target);
        targetDecoration.setVisible(target);

        sourceCountLabel.updateChanges(sourceType,
                mapping.getTargetToSourceCount());
        targetCountLabel.updateChanges(targetType,
                mapping.getSourceToTargetCount());
    }

    /**
     * Get source count figure
     * 
     * @return source count figure
     */
    public IFigure getSourceCountFigure() {
        return this.sourceCountLabel;
    }

    /**
     * Get target count figure
     * 
     * @return target count figure
     */
    public IFigure getTargetCountFigure() {
        return this.targetCountLabel;
    }
}
