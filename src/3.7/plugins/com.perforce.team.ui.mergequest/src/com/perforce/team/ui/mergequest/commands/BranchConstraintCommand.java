/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Branch;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchConstraintCommand extends Command {

    private Branch branch;
    private ChangeBoundsRequest request;
    private Rectangle bounds;
    private Rectangle previousBounds;

    /**
     * @param branch
     * @param request
     * @param constraint
     */
    public BranchConstraintCommand(Branch branch, ChangeBoundsRequest request,
            Rectangle constraint) {
        this.branch = branch;
        this.request = request;
        this.bounds = constraint.getCopy();
        setLabel(Messages.BranchConstraintCommand_DefaultLabel);
    }

    /**
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        Object type = request.getType();
        return RequestConstants.REQ_MOVE.equals(type)
                || RequestConstants.REQ_MOVE_CHILDREN.equals(type)
                || RequestConstants.REQ_RESIZE.equals(type)
                || RequestConstants.REQ_RESIZE_CHILDREN.equals(type);
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        this.previousBounds = new Rectangle(this.branch.getX(),
                this.branch.getY(), this.branch.getWidth(),
                this.branch.getHeight());
        this.branch.setLocation(bounds.x, bounds.y);
        this.branch.setSize(bounds.width, bounds.height);
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        this.branch.setLocation(previousBounds.x, previousBounds.y);
        this.branch.setSize(previousBounds.width, previousBounds.height);
    }

}
