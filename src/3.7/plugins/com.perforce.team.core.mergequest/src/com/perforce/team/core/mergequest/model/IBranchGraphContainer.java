/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IBranchGraphContainer extends IAdaptable {

    /**
     * Create a new branch graph with the specified id. If the id is invalid or
     * not specified then a new valid id will be generated
     * 
     * @param id
     * @return branch graph
     */
    IBranchGraph createGraph(String id);

    /**
     * Get graph by id
     * 
     * @param id
     * @return branch graph or null if not found
     */
    IBranchGraph getGraph(String id);

    /**
     * Get graph by name
     * 
     * @param name
     * @return branch graph or null if not found
     */
    IBranchGraph getGraphByName(String name);

    /**
     * Add a graph to this container
     * 
     * @param graph
     * @return true if added, false otherwise
     */
    boolean add(IBranchGraph graph);

    /**
     * Import a graph into this container. This method will generate a new graph
     * owned by this container and place all elements from the specified graph
     * into the generate graph.
     * 
     * @param graph
     * @return imported graph or null if imported fails
     */
    IBranchGraph importGraph(IBranchGraph graph);

    /**
     * Get branch graph in this container
     * 
     * @return non-null but possibly empty array of graphs
     */
    IBranchGraph[] getGraphs();

    /**
     * Set graphs in containers
     * 
     * @param graphs
     */
    void setGraphs(IBranchGraph[] graphs);

    /**
     * Remove graph from container
     * 
     * @param graph
     * @return - true if removed, false otherwise
     */
    boolean remove(IBranchGraph graph);

    /**
     * Get number of graphs in container
     * 
     * @return size
     */
    int size();

}
