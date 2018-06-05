/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.policies;

import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.commands.BendpointCommand;
import com.perforce.team.ui.mergequest.commands.BendpointCreateCommand;
import com.perforce.team.ui.mergequest.commands.BendpointDeleteCommand;
import com.perforce.team.ui.mergequest.commands.BendpointMoveCommand;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.requests.BendpointRequest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingBendpointEditPolicy extends BendpointEditPolicy {

    /**
     * @see org.eclipse.gef.editpolicies.BendpointEditPolicy#getCreateBendpointCommand(org.eclipse.gef.requests.BendpointRequest)
     */
    @Override
    protected Command getCreateBendpointCommand(BendpointRequest request) {
        BendpointCreateCommand command = new BendpointCreateCommand();
        setCommandDimensions(request, command);
        setCommandLocation(request, command);
        return command;
    }

    private void setCommandDimensions(BendpointRequest request,
            BendpointCommand command) {
        Point p = request.getLocation();
        Connection conn = getConnection();
        conn.translateToRelative(p);
        Point source = getConnection().getSourceAnchor().getReferencePoint();
        Point target = getConnection().getTargetAnchor().getReferencePoint();
        conn.translateToRelative(source);
        conn.translateToRelative(target);
        command.setDimensions(p.getDifference(source), p.getDifference(target));
    }

    private void setCommandLocation(BendpointRequest request,
            BendpointCommand command) {
        command.setMapping((Mapping) request.getSource().getModel());
        command.setIndex(request.getIndex());
    }

    /**
     * @see org.eclipse.gef.editpolicies.BendpointEditPolicy#getDeleteBendpointCommand(org.eclipse.gef.requests.BendpointRequest)
     */
    @Override
    protected Command getDeleteBendpointCommand(BendpointRequest request) {
        BendpointDeleteCommand command = new BendpointDeleteCommand();
        setCommandLocation(request, command);
        return command;
    }

    /**
     * @see org.eclipse.gef.editpolicies.BendpointEditPolicy#getMoveBendpointCommand(org.eclipse.gef.requests.BendpointRequest)
     */
    @Override
    protected Command getMoveBendpointCommand(BendpointRequest request) {
        BendpointMoveCommand command = new BendpointMoveCommand();
        setCommandDimensions(request, command);
        setCommandLocation(request, command);
        return command;
    }

}
