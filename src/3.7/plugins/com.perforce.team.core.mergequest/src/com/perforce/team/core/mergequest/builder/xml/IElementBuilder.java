/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.builder.xml;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphElement;

import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IElementBuilder {

    /**
     * 
     * @param parent
     * @param element
     */
    void save(Element parent, IBranchGraphElement element);

    /**
     * Initialize the branch graph element and add it to the graph if
     * successfully initialized
     * 
     * @param element
     * @param graph
     * @return branch graph element
     */
    IBranchGraphElement initialize(Element element, IBranchGraph graph);

    /**
     * Completing the building of the element.
     * 
     * @param element
     * @param graph
     */
    void complete(IBranchGraphElement element, IBranchGraph graph);

}
