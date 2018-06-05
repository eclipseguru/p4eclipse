/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model.factory;

import com.perforce.team.core.mergequest.model.BranchGraphContainer;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ContainerFactory implements IContainerFactory {

    /**
     * @see com.perforce.team.core.mergequest.model.factory.IContainerFactory#create()
     */
    public IBranchGraphContainer create() {
        return new BranchGraphContainer();
    }

}
