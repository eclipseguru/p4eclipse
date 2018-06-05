/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;

import org.eclipse.gef.commands.Command;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchDeleteCommand extends Command {

    private IBranchGraph graph;
    private Branch branch;
    private Mapping[] sourceMappings;
    private Mapping[] targetMappings;

    /**
     * @param graph
     * @param branch
     */
    public BranchDeleteCommand(IBranchGraph graph, Branch branch) {
        this.graph = graph;
        this.branch = branch;
        setLabel(Messages.BranchDeleteCommand_DefaultLabel);
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        Mapping[] currSource = branch.getSourceMappings();
        Mapping[] currTarget = branch.getTargetMappings();
        branch.disconnect();
        if (this.graph.remove(branch)) {
            this.sourceMappings = currSource;
            this.targetMappings = currTarget;
        }
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        if (this.graph.add(branch)) {
            if (sourceMappings != null) {
                for (Mapping mapping : sourceMappings) {
                    mapping.connect();
                }
            }
            if (targetMappings != null) {
                for (Mapping mapping : targetMappings) {
                    mapping.connect();
                }
            }
        }
    }
}
