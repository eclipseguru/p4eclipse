/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.policies;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.mergequest.commands.MappingCreateCommand;
import com.perforce.team.ui.mergequest.commands.MappingReconnectCommand;
import com.perforce.team.ui.mergequest.figures.BranchFigure;
import com.perforce.team.ui.mergequest.figures.MappingConnectionAnchor;
import com.perforce.team.ui.mergequest.figures.MappingFigure;
import com.perforce.team.ui.mergequest.figures.theme.ThemeHelper;
import com.perforce.team.ui.mergequest.figures.theme.ThemeListenerAdapter;
import com.perforce.team.ui.mergequest.parts.BranchEditPart;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchConnectPolicy extends GraphicalNodeEditPolicy {

    private ThemeHelper themeHelper;
    private Color defaultLineColor;

    /**
     * Create new branch connect policy
     */
    public BranchConnectPolicy() {
        this.themeHelper = new ThemeHelper();
        this.themeHelper.setForegroundKey(ThemeHelper.FG_PREFIX
                + "mapping.disabled"); //$NON-NLS-1$
        this.themeHelper.setListener(new ThemeListenerAdapter() {

            @Override
            public void setForegroundColor(Color color) {
                defaultLineColor = color;
            }

        });
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#deactivate()
     */
    @Override
    public void deactivate() {
        this.themeHelper.deactivate();
        super.deactivate();
    }

    /**
     * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
     */
    @Override
    public void activate() {
        this.themeHelper.activate();
        super.activate();
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCompleteCommand(org.eclipse.gef.requests.CreateConnectionRequest)
     */
    @Override
    protected Command getConnectionCompleteCommand(
            CreateConnectionRequest request) {
        Branch target = (Branch) getHost().getModel();
        MappingCreateCommand command = (MappingCreateCommand) request
                .getStartCommand();
        command.setTarget(target);
        MappingConnectionAnchor anchor = (MappingConnectionAnchor) ((BranchEditPart) getHost())
                .getTargetConnectionAnchor(request);
        command.setTargetTerminal(anchor.getIndex());
        return command;
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getConnectionCreateCommand(org.eclipse.gef.requests.CreateConnectionRequest)
     */
    @Override
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        MappingCreateCommand command = null;
        Branch source = (Branch) getHost().getModel();
        if (source != null) {
            IP4Connection connection = null;
            IBranchGraph graph = source.getGraph();
            if (graph != null) {
                connection = graph.getConnection();
                if (connection != null) {
                    Object type = request.getNewObjectType();
                    if (type != null) {
                        command = new MappingCreateCommand(graph, type);
                        command.setSource(source);
                        request.setStartCommand(command);
                    }
                }
            }
            MappingConnectionAnchor anchor = (MappingConnectionAnchor) ((BranchEditPart) getHost())
                    .getTargetConnectionAnchor(request);
            command.setSourceTerminal(anchor.getIndex());
        }
        return command;
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectSourceCommand(org.eclipse.gef.requests.ReconnectRequest)
     */
    @Override
    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        Mapping mapping = (Mapping) request.getConnectionEditPart().getModel();
        Branch newSource = (Branch) getHost().getModel();
        if (newSource.equals(mapping.getTarget())) {
            return null;
        }
        MappingConnectionAnchor anchor = (MappingConnectionAnchor) ((BranchEditPart) getHost())
                .getSourceConnectionAnchor(request);
        MappingReconnectCommand command = new MappingReconnectCommand(mapping);
        command.setNewTerminal(anchor.getIndex());
        command.setNewSource(newSource);
        return command;
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getReconnectTargetCommand(org.eclipse.gef.requests.ReconnectRequest)
     */
    @Override
    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        Mapping mapping = (Mapping) request.getConnectionEditPart().getModel();
        Branch newTarget = (Branch) getHost().getModel();
        if (newTarget.equals(mapping.getSource())) {
            return null;
        }
        MappingConnectionAnchor anchor = (MappingConnectionAnchor) ((BranchEditPart) getHost())
                .getTargetConnectionAnchor(request);
        MappingReconnectCommand command = new MappingReconnectCommand(mapping);
        command.setNewTarget(newTarget);
        command.setNewTerminal(anchor.getIndex());
        return command;
    }

    /**
     * Get figure from request
     * 
     * @param request
     * @param source
     * @return figure
     */
    protected IFigure getRequestFigure(Request request, boolean source) {
        IFigure figure = null;
        if (request instanceof CreateConnectionRequest) {
            EditPart part = null;
            if (source) {
                part = ((CreateConnectionRequest) request).getSourceEditPart();
            } else {
                part = ((CreateConnectionRequest) request).getTargetEditPart();
            }
            if (part instanceof GraphicalEditPart) {
                figure = ((GraphicalEditPart) part).getFigure();
            }
        }
        return figure;
    }

    /**
     * Set anchor visibility on specified branch figure
     * 
     * @param figure
     * @param show
     */
    protected void showAnchors(IFigure figure, boolean show) {
        if (figure instanceof BranchFigure) {
            ((BranchFigure) figure).showAnchors(show);
        }
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#eraseSourceFeedback(org.eclipse.gef.Request)
     */
    @Override
    public void eraseSourceFeedback(Request request) {
        super.eraseSourceFeedback(request);
        showAnchors(getRequestFigure(request, true), false);
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#eraseTargetFeedback(org.eclipse.gef.Request)
     */
    @Override
    public void eraseTargetFeedback(Request request) {
        super.eraseTargetFeedback(request);
        showAnchors(getRequestFigure(request, false), false);
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#showSourceFeedback(org.eclipse.gef.Request)
     */
    @Override
    public void showSourceFeedback(Request request) {
        super.showSourceFeedback(request);
        showAnchors(getRequestFigure(request, true), true);
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#showTargetFeedback(org.eclipse.gef.Request)
     */
    @Override
    public void showTargetFeedback(Request request) {
        super.showTargetFeedback(request);
        showAnchors(getRequestFigure(request, false), true);
    }

    /**
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#createDummyConnection(org.eclipse.gef.Request)
     */
    @Override
    protected Connection createDummyConnection(Request req) {
        PolylineConnection connection = new PolylineConnection();
        connection.setLineWidth(MappingFigure.INACTIVE_LINE_WIDTH);
        connection.setForegroundColor(this.defaultLineColor);
        return connection;
    }

    private IFigure getFigure(EditPart part) {
        return part instanceof GraphicalEditPart ? ((GraphicalEditPart) part)
                .getFigure() : null;
    }

    /**
     * 
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getTargetConnectionAnchor(org.eclipse.gef.requests.CreateConnectionRequest)
     */
    @Override
    protected ConnectionAnchor getTargetConnectionAnchor(
            CreateConnectionRequest request) {
        ConnectionAnchor anchor = super.getTargetConnectionAnchor(request);
        if (anchor != null
                && anchor.getOwner().equals(
                        getFigure(request.getSourceEditPart()))) {
            anchor = null;
        }
        return anchor;
    }
}
