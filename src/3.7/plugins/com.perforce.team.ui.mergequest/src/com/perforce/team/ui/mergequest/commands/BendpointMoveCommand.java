/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Mapping.Joint;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BendpointMoveCommand extends BendpointCommand {

    private Joint previous = null;

    /**
     * Move bendpoint command
     */
    public BendpointMoveCommand() {
        setLabel(Messages.BendpointMoveCommand_DefaultLabel);
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        Joint joint = new Joint(dimension1.width, dimension1.height,
                dimension2.width, dimension2.height);
        previous = mapping.setJoint(index, joint);
    }

    /**
     * @see org.eclipse.gef.commands.Command#canUndo()
     */
    @Override
    public boolean canUndo() {
        return previous != null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        mapping.setJoint(index, previous);
    }

}
