/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder;

import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.mergequest.model.factory.IContainerFactory;

import java.io.IOException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IBranchGraphBuilder {

    /**
     * Load the branch graph that this builder is configured to build
     * 
     * @return - non-null branch graph
     * @throws IOException
     */
    IBranchGraphContainer load() throws IOException;

    /**
     * Persist the specified container
     * 
     * @param container
     * @throws IOException
     */
    void persist(IBranchGraphContainer container) throws IOException;

    /**
     * Get container factory
     * 
     * @return container factory
     */
    IContainerFactory getContainerFactory();

}
