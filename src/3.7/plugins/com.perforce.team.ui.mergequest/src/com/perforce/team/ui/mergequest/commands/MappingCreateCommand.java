/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.actions.MappingCreateAction;

import org.eclipse.gef.commands.Command;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingCreateCommand extends Command {

    private IBranchGraph graph;
    private Branch source;
    private Branch target;
    private Mapping created;
    private Object type;
    private int sourceTerminal = 0;
    private int targetTerminal = 0;

    /**
     * Create mapping command with specified graph and type
     * 
     * @param graph
     * @param type
     */
    public MappingCreateCommand(IBranchGraph graph, Object type) {
        this.graph = graph;
        this.type = type;
        setLabel(Messages.MappingCreateCommand_DefaultLabel);
    }

    /**
     * Set source terminal
     * 
     * @param terminal
     */
    public void setSourceTerminal(int terminal) {
        this.sourceTerminal = terminal;
    }

    /**
     * Set target terminal
     * 
     * @param terminal
     */
    public void setTargetTerminal(int terminal) {
        this.targetTerminal = terminal;
    }

    /**
     * Set source
     * 
     * @param source
     */
    public void setSource(Branch source) {
        this.source = source;
    }

    /**
     * Set target branch
     * 
     * @param target
     */
    public void setTarget(Branch target) {
        this.target = target;
    }

    /**
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        if (this.graph != null) {
            if (this.source != null && this.target != null) {
                return !this.source.equals(this.target);
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        if (this.graph != null) {
            MappingCreateAction create = new MappingCreateAction(graph,
                    this.type);
            create.setInitialSource(this.source);
            create.setInitialTarget(this.target);
            create.setSourceTerminal(this.sourceTerminal);
            create.setTargetTerminal(this.targetTerminal);
            create.run();
            this.created = create.getLastCreated();
        }
    }

    /**
     * @see org.eclipse.gef.commands.Command#canUndo()
     */
    @Override
    public boolean canUndo() {
        return this.graph != null && this.created != null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        if (this.created != null && this.graph != null) {
            this.created.disconnect();
            this.graph.remove(this.created);
        }
    }

    /**
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        if (this.created != null && this.graph != null) {
            if (this.graph.add(this.created)) {
                this.created.connect();
            }
        }
    }
}
