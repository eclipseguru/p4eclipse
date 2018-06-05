/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;

import org.eclipse.gef.commands.Command;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingDeleteCommand extends Command {

    private IBranchGraph previousGraph;
    private Mapping mapping;

    /**
     * Create mapping delete command
     * 
     * @param mapping
     */
    public MappingDeleteCommand(Mapping mapping) {
        this.mapping = mapping;
        setLabel(Messages.MappingDeleteCommand_DefaultLabel);
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        this.mapping.disconnect();
        IBranchGraph graph = mapping.getGraph();
        if (graph != null && graph.remove(this.mapping)) {
            this.previousGraph = graph;
        }
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        if (previousGraph != null && previousGraph.add(this.mapping)) {
            this.mapping.connect();
        }
    }

}
