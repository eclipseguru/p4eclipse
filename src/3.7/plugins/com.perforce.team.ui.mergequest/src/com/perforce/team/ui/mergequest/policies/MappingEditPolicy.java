/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.policies;

import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.commands.MappingDeleteCommand;
import com.perforce.team.ui.mergequest.figures.BranchFigure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.requests.ReconnectRequest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingEditPolicy extends ConnectionEditPolicy {

    private Mapping mapping;
    private List<BranchFigure> targets;

    /**
     * Create new mapping edit policy
     * 
     * @param mapping
     */
    public MappingEditPolicy(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * @see org.eclipse.gef.editpolicies.ConnectionEditPolicy#getDeleteCommand(org.eclipse.gef.requests.GroupRequest)
     */
    @Override
    protected Command getDeleteCommand(GroupRequest request) {
        return new MappingDeleteCommand(mapping);
    }

    /**
     * Get figure from request
     * 
     * @param request
     * @return figure
     */
    protected BranchFigure getRequestFigure(ReconnectRequest request) {
        IFigure figure = null;
        EditPart part = request.getTarget();
        if (part instanceof GraphicalEditPart) {
            figure = ((GraphicalEditPart) part).getFigure();
        }
        return figure instanceof BranchFigure ? (BranchFigure) figure : null;
    }

    /**
     * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#eraseSourceFeedback(org.eclipse.gef.Request)
     */
    @Override
    public void eraseSourceFeedback(Request request) {
        super.eraseSourceFeedback(request);
        if (request instanceof ReconnectRequest) {
            if (this.targets != null) {
                for (BranchFigure figure : this.targets) {
                    figure.showAnchors(false);
                }
                this.targets = null;
            }
        }

    }

    /**
     * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#showSourceFeedback(org.eclipse.gef.Request)
     */
    @Override
    public void showSourceFeedback(Request request) {
        super.showSourceFeedback(request);
        if (request instanceof ReconnectRequest) {
            BranchFigure figure = getRequestFigure((ReconnectRequest) request);
            if (targets == null) {
                targets = new ArrayList<BranchFigure>();
            }
            if (figure != null) {
                targets.add(figure);
                figure.showAnchors(true);
            }
        }
    }

}
