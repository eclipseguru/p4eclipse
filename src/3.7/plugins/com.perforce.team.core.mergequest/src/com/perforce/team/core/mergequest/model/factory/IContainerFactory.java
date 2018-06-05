/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model.factory;

import com.perforce.team.core.mergequest.model.IBranchGraphContainer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IContainerFactory {

    /**
     * Create a branch graph container
     * 
     * @return branch graph container
     */
    IBranchGraphContainer create();

}
