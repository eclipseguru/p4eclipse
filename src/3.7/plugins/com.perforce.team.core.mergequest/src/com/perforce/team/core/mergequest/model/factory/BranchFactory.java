/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model.factory;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchFactory extends BranchGraphElementFactory implements
        IBranchGraphElementFactory {

    /**
     * @see com.perforce.team.core.mergequest.model.factory.BranchGraphElementFactory#getPrefix()
     */
    @Override
    protected String getPrefix() {
        return "codeline"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.core.mergequest.model.factory.IBranchGraphElementFactory#create(java.lang.String,
     *      com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public IBranchGraphElement create(String id, IBranchGraph graph) {
        return new Branch(getId(id), graph);
    }

}
