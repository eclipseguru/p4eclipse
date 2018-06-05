/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IBranchGraphElement extends IPropertyElement {

    /**
     * NAME
     */
    public static final String NAME = "name"; //$NON-NLS-1$

    /**
     * Get element id
     * 
     * @return non-null and non-empty id
     */
    String getId();

    /**
     * Get element name. This should default to the id if name doesn't pertain
     * to this element.
     * 
     * @return non-null and non-empty id
     */
    String getName();

    /**
     * Set the name of this element.
     * 
     * @param name
     * @return true if set, false otherwise
     */
    boolean setName(String name);

    /**
     * Set the graph that this element is associated with
     * 
     * @param graph
     */
    void setGraph(IBranchGraph graph);

    /**
     * Get graph that owns this element
     * 
     * @return branch graph or null if not associated
     */
    IBranchGraph getGraph();

}
