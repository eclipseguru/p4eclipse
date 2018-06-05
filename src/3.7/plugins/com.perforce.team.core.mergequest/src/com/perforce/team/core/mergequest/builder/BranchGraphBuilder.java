/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder;

import com.perforce.team.core.mergequest.model.factory.ContainerFactory;
import com.perforce.team.core.mergequest.model.factory.IContainerFactory;

import org.eclipse.core.runtime.Assert;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BranchGraphBuilder implements IBranchGraphBuilder {

    /**
     * Factory for creating new branch graph containers
     */
    protected IContainerFactory containerFactory;

    /**
     * Create a new branch graph builder
     */
    public BranchGraphBuilder() {
        this(new ContainerFactory());
    }

    /**
     * Create a new branch graph builder
     * 
     * @param factory
     */
    public BranchGraphBuilder(IContainerFactory factory) {
        Assert.isNotNull(factory, "Container factory cannot be null"); //$NON-NLS-1$
        this.containerFactory = factory;
    }

    /**
     * @see com.perforce.team.core.mergequest.builder.IBranchGraphBuilder#getContainerFactory()
     */
    public IContainerFactory getContainerFactory() {
        return this.containerFactory;
    }

}
