/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import org.eclipse.core.runtime.Assert;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BranchGraphElement extends PropertyElement implements
        IBranchGraphElement {

    private final String id;
    private String name;
    private IBranchGraph graph;

    /**
     * Create a new branch graph element
     * 
     * @param id
     * @param name
     * @param graph
     */
    public BranchGraphElement(String id, String name, IBranchGraph graph) {
        this.id = id;
        this.name = name;
        this.graph = graph;
        Assert.isNotNull(this.id, "Id of branch graph element cannot be null"); //$NON-NLS-1$
        Assert.isTrue(this.id.length() > 0,
                "Id of branch graph element cannot be empty"); //$NON-NLS-1$
    }

    /**
     * Create a new branch graph element
     * 
     * @param id
     * @param graph
     */
    public BranchGraphElement(String id, IBranchGraph graph) {
        this(id, id, graph);
    }

    /**
     * Create a new branch graph element
     * 
     * @param id
     * @param name
     */
    public BranchGraphElement(String id, String name) {
        this(id, id, null);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphElement#setName(java.lang.String)
     */
    public boolean setName(String name) {
        boolean set = false;
        if (name != null && name.length() > 0 && !name.equals(this.name)) {
            String previous = this.name;
            this.name = name;
            changeSupport.firePropertyChange(NAME, previous, this.name);
            set = true;
        }
        return set;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphElement#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getId();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof IBranchGraphElement) {
            IBranchGraphElement other = (IBranchGraphElement) obj;
            IBranchGraph graph = getGraph();
            IBranchGraph otherGraph = other.getGraph();
            if (graph == null) {
                return otherGraph == null && getId().equals(other.getId());
            } else {
                return getId().equals(other.getId());
            }
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphElement#getId()
     */
    public String getId() {
        return this.id;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphElement#getGraph()
     */
    public IBranchGraph getGraph() {
        return this.graph;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphElement#setGraph(com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public void setGraph(IBranchGraph graph) {
        this.graph = graph;
    }

}
