/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Mapping.Joint;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BendpointCreateCommand extends BendpointCommand {

    /**
     * Create bendpoint command
     */
    public BendpointCreateCommand() {
        setLabel(Messages.BendpointCreateCommand_DefaultLabel);
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        Joint joint = new Joint(dimension1.width, dimension1.height,
                dimension2.width, dimension2.height);
        mapping.addJoint(index, joint);
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        mapping.removeJoint(index);
    }

}
