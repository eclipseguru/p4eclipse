/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.Mapping;

import org.eclipse.gef.commands.Command;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingReconnectCommand extends Command {

    private Mapping mapping = null;
    private Branch currentSource = null;
    private Branch newSource = null;
    private Branch currentTarget = null;
    private Branch newTarget = null;
    private int currentTerminal = 0;
    private int newTerminal = 0;

    /**
     * Create mapping reconnect command
     * 
     * @param mapping
     */
    public MappingReconnectCommand(Mapping mapping) {
        this.mapping = mapping;
        this.currentSource = this.mapping.getSource();
        this.currentTarget = this.mapping.getTarget();
        setLabel(Messages.MappingReconnectCommand_DefaultLabel);
    }

    /**
     * Set new terminal
     * 
     * @param terminal
     */
    public void setNewTerminal(int terminal) {
        this.newTerminal = terminal;
    }

    /**
     * Set new source branch
     * 
     * @param branch
     */
    public void setNewSource(Branch branch) {
        this.newSource = branch;
    }

    /**
     * Set new target branch
     * 
     * @param branch
     */
    public void setNewTarget(Branch branch) {
        this.newTarget = branch;
    }

    /**
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        if (mapping == null) {
            return false;
        }
        if (newSource != null) {
            return currentTarget == null || !currentTarget.equals(newSource);
        } else if (newTarget != null) {
            return currentSource == null || !currentSource.equals(newTarget);
        }
        return false;
    }

    /**
     * @see org.eclipse.gef.commands.Command#canUndo()
     */
    @Override
    public boolean canUndo() {
        return this.currentSource != null && this.currentTarget != null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        if (!this.currentSource.equals(mapping.getSource())
                || !this.currentTarget.equals(mapping.getTarget())) {
            this.mapping.connect(this.currentSource, this.currentTarget);
        }
        if (this.newSource != null) {
            this.mapping.setSourceAnchor(this.currentTerminal);
        } else if (this.newTarget != null) {
            this.mapping.setTargetAnchor(this.currentTerminal);
        }
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        if (this.newSource != null) {
            if (this.currentSource == null
                    || !this.currentSource.equals(this.newSource)) {
                this.mapping.connect(this.newSource, this.currentTarget);
            }
            this.currentTerminal = this.mapping.getSourceAnchor();
            this.mapping.setSourceAnchor(this.newTerminal);
        } else if (this.newTarget != null) {
            if (this.currentTarget == null
                    || !this.currentTarget.equals(this.newTarget)) {
                this.mapping.connect(this.currentSource, this.newTarget);
            }
            this.currentTerminal = this.mapping.getTargetAnchor();
            this.mapping.setTargetAnchor(this.newTerminal);
        }
    }

}
