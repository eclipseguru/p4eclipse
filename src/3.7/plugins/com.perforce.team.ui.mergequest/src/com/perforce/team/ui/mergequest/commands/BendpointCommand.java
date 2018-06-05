/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.commands;

import com.perforce.team.core.mergequest.model.Mapping;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BendpointCommand extends Command {

    /**
     * Index
     */
    protected int index = 0;

    /**
     * Dimension 1
     */
    protected Dimension dimension1;

    /**
     * Dimension 2
     */
    protected Dimension dimension2;

    /**
     * Mapping
     */
    protected Mapping mapping;

    /**
     * Set index of joint
     * 
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @param dimension1
     * @param dimension2
     */
    public void setDimensions(Dimension dimension1, Dimension dimension2) {
        this.dimension1 = dimension1;
        this.dimension2 = dimension2;
    }

    /**
     * Set mapping
     * 
     * @param mapping
     */
    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        return this.mapping != null;
    }
}
