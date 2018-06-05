/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Mapping.Joint;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BendpointDeleteCommand extends BendpointCommand {

    private Joint deleted = null;

    /**
     * Delete bendpoint command
     */
    public BendpointDeleteCommand() {
        setLabel(Messages.BendpointDeleteCommand_DefaultLabel);
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        deleted = mapping.removeJoint(index);
    }

    /**
     * @see org.eclipse.gef.commands.Command#canUndo()
     */
    @Override
    public boolean canUndo() {
        return this.deleted != null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        mapping.addJoint(index, deleted);
    }

}
