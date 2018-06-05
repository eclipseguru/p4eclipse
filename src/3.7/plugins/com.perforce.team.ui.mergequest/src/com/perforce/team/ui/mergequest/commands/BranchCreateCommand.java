/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.ui.mergequest.actions.BranchCreateAction;

import java.text.MessageFormat;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Point;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchCreateCommand extends Command {

    private BranchType type;
    private IBranchGraph graph;
    private Branch created = null;
    private Rectangle location;

    /**
     * Create branch create command
     * 
     * @param type
     * @param graph
     * @param location
     */
    public BranchCreateCommand(BranchType type, IBranchGraph graph,
            Rectangle location) {
        this.type = type;
        this.graph = graph;
        this.location = location;
    }

    private void resetDefaultLabel() {
        setLabel(Messages.BranchCreateCommand_DefaultLabel);
    }

    /**
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        return this.type != null && this.graph != null && this.location != null
                && this.created == null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#canUndo()
     */
    @Override
    public boolean canUndo() {
        return this.created != null;
    }

    /**
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        if (this.created != null && this.graph != null) {
            this.graph.add(this.created);
        }
    }

    /**
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        BranchDeleteCommand delete = new BranchDeleteCommand(this.graph,
                created);
        delete.execute();
    }

    /**
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        BranchCreateAction create = new BranchCreateAction(this.graph,
                this.type, new Point(this.location.x, this.location.y));
        create.run();
        created = create.getCreated();
        if (created != null) {
            setLabel(MessageFormat.format(
                    Messages.BranchCreateCommand_BranchNameLabel,
                    created.getName()));
        } else {
            resetDefaultLabel();
        }
    }
}
