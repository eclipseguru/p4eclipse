/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model.factory;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IBranchGraphElementFactory {

    /**
     * Create an element with a specified id
     * 
     * @param graph
     * @param id
     *            - if null, an id will be generated
     * @param memento
     * @param elementType
     * @param type
     *            - sub-type of element
     * @return branch graph element
     */
    IBranchGraphElement create(String id, IBranchGraph graph);

    /**
     * Update the factory
     * 
     * @param id
     */
    void update(String id);

}
