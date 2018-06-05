/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.ui.mergequest.actions.BranchEditAction;
import com.perforce.team.ui.mergequest.wizards.branch.BranchDescriptor;

import org.eclipse.gef.commands.Command;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchEditCommand extends Command {

    private Branch branch = null;
    private BranchDescriptor previous = null;

    /**
     * Branch edit command
     * 
     * @param branch
     */
    public BranchEditCommand(Branch branch) {
        this.branch = branch;
        setLabel(Messages.BranchEditCommand_DefaultLabel);
    }

    /**
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        return this.branch != null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#canUndo()
     */
    @Override
    public boolean canUndo() {
        return this.previous != null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        undo();
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        if (this.branch != null && this.previous != null) {
            BranchDescriptor current = new BranchDescriptor();
            current.setName(this.branch.getName());
            current.setType(this.branch.getType());
            this.branch.setName(this.previous.getName());
            this.branch.setType(this.previous.getType().getType());
            this.previous = current;
        }
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        this.previous = new BranchDescriptor();
        this.previous.setName(this.branch.getName());
        this.previous.setType(this.branch.getType());
        BranchEditAction edit = new BranchEditAction(this.branch);
        edit.run();
    }

}