/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.commands.BranchEditCommand;
import com.perforce.team.ui.mergequest.commands.CommandAction;
import com.perforce.team.ui.mergequest.figures.BranchFigure;
import com.perforce.team.ui.mergequest.figures.theme.FigureThemeHelper;
import com.perforce.team.ui.mergequest.policies.BranchConnectPolicy;
import com.perforce.team.ui.mergequest.policies.BranchEditPolicy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.jface.resource.JFaceResources;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchEditPart extends AbstractGraphicalEditPart implements
        PropertyChangeListener, NodeEditPart {

    private SharedResources images;
    private ToolTipHelper tooltipHelper = null;
    private FigureThemeHelper themeHelper = null;
    private Runnable refreshCallback = new Runnable() {

        public void run() {
            refreshVisuals();
        }
    };

    /**
     * Create a branch edit part
     * 
     * @param images
     */
    public BranchEditPart(SharedResources images) {
        this.images = images;
        tooltipHelper = new ToolTipHelper(this);
        themeHelper = new FigureThemeHelper();
        themeHelper.setDefaultBackground(ColorConstants.white);
        themeHelper.setDefaultOutline(ColorConstants.darkGray);
        themeHelper.setDefaultForeground(ColorConstants.darkGray);
        themeHelper.setDefaultFont(JFaceResources.getDialogFont());
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            getBranch().addPropertyListener(this);

            this.tooltipHelper.activate();
            this.themeHelper.activate(this.refreshCallback);
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class key) {
        if (key == Branch.class || key == IBranchGraphElement.class) {
            return getBranch();
        }
        return super.getAdapter(key);
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            getBranch().removePropertyListener(this);
            this.tooltipHelper.deactivate();
            this.themeHelper.deactivate();
            super.deactivate();
        }
    }

    private BranchFigure getBranchFigure() {
        return (BranchFigure) getFigure();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        Branch branch = getBranch();
        BranchFigure figure = new BranchFigure(branch, images);
        figure.setOutline(true);
        themeHelper.setFigure(figure);
        bindTypeTheme();
        return figure;
    }

    private void bindTypeTheme() {
        String type = getBranch().getType().toString()
                .toLowerCase(Locale.ENGLISH);
        String suffix = "branch." + type; //$NON-NLS-1$
        themeHelper.setBackgroundKey(FigureThemeHelper.BG_PREFIX + suffix);
        themeHelper.setForegroundKey(FigureThemeHelper.FG_PREFIX + suffix);
        themeHelper.setFontKey(FigureThemeHelper.FONT_PREFIX + suffix);
        themeHelper.setOutlineKey(FigureThemeHelper.OUTLINE_PREFIX + suffix);
        themeHelper.synchronize();
    }

    private Branch getBranch() {
        return (Branch) getModel();
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new BranchEditPolicy());
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
                new BranchConnectPolicy());
    }

    private int getNewTerminal(Branch source, Branch target, int current) {
        if (target.getY() > source.getY() && current < 3) {
            current += 5;
        } else if (target.getY() < source.getY() && current > 4 && current < 8) {
            current -= 5;
        } else if (target.getX() > source.getX() && current > 7) {
            current -= 5;
        } else if (target.getX() < source.getX() && current > 2 && current < 5) {
            current += 5;
        }
        return current;
    }

    private void refreshAnchors() {
        Branch branch = getBranch();
        for (Mapping mapping : branch.getSourceMappings()) {
            if (mapping.getJointCount() == 0) {
                Branch target = mapping.getTarget();
                if (target != null) {
                    mapping.setSourceAnchor(getNewTerminal(branch, target,
                            mapping.getSourceAnchor()));
                    mapping.setTargetAnchor(getNewTerminal(target, branch,
                            mapping.getTargetAnchor()));
                }
            }
        }
        for (Mapping mapping : branch.getTargetMappings()) {
            if (mapping.getJointCount() == 0) {
                Branch source = mapping.getSource();
                if (source != null) {
                    mapping.setSourceAnchor(getNewTerminal(source, branch,
                            mapping.getSourceAnchor()));
                    mapping.setTargetAnchor(getNewTerminal(branch, source,
                            mapping.getTargetAnchor()));
                }
            }
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        if (isActive()) {
            if (PerforceUIPlugin.isUIThread()) {
                Branch branch = getBranch();
                BranchFigure figure = getBranchFigure();
                figure.refresh();
                Dimension preferred = figure.getPreferredSize();
                Rectangle bounds = new Rectangle(branch.getX(), branch.getY(),
                        preferred.width, preferred.height);
                bounds.width = Math.max(branch.getWidth(), bounds.width);
                bounds.height = Math.max(branch.getHeight(), bounds.height);
                branch.setSize(bounds.width, bounds.height);
                ((GraphicalEditPart) getParent()).setLayoutConstraint(this,
                        figure, bounds);
                refreshAnchors();
            } else {
                PerforceUIPlugin.asyncExec(new Runnable() {

                    public void run() {
                        refreshVisuals();
                    }
                });
            }
        }
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
     */
    @Override
    protected List<Mapping> getModelSourceConnections() {
        return Arrays.asList(getBranch().getSourceMappings());
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
     */
    @Override
    protected List<Mapping> getModelTargetConnections() {
        return Arrays.asList(getBranch().getSourceOwnedTargetMappings());
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (Branch.LOCATION.equals(property) || Branch.NAME.equals(property)
                || Branch.SIZE.equals(property)) {
            refreshVisuals();
        } else if (Branch.TYPE.equals(property)) {
            bindTypeTheme();
        } else if (Branch.SOURCE_MAPPINGS.equals(property)
                || Branch.TARGET_MAPPINGS.equals(property)) {
            refreshAnchors();
            refreshSourceConnections();
            refreshTargetConnections();
        }
    }

    /**
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getTargetConnectionAnchor(
            ConnectionEditPart connection) {
        return getBranchFigure().getAnchor(
                ((MappingEditPart) connection).getMapping());
    }

    /**
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return getSourceConnectionAnchor(request);
    }

    /**
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getSourceConnectionAnchor(
            ConnectionEditPart connection) {
        return getTargetConnectionAnchor(connection);
    }

    /**
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        Point point = new Point(((DropRequest) request).getLocation());
        return getBranchFigure().getAnchor(point);
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
     */
    @Override
    public void performRequest(Request req) {
        if (RequestConstants.REQ_OPEN.equals(req.getType())) {
            CommandAction edit = new CommandAction(new BranchEditCommand(
                    getBranch()), getViewer());
            edit.run();
        } else {
            super.performRequest(req);
        }
    }

}
