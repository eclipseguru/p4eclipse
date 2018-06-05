/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import com.perforce.team.core.mergequest.model.factory.GraphFactory;
import com.perforce.team.core.mergequest.model.factory.IBranchGraphElementFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.PlatformObject;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphContainer extends PlatformObject implements
        IBranchGraphContainer {

    private Map<String, IBranchGraph> graphs;
    private IBranchGraphElementFactory graphFactory = null;

    /**
     * Create empty branch graph container
     */
    public BranchGraphContainer() {
        this.graphs = new LinkedHashMap<String, IBranchGraph>();
        this.graphFactory = new GraphFactory();
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#createGraph(java.lang.String)
     */
    public IBranchGraph createGraph(String id) {
        return (IBranchGraph) this.graphFactory.create(id, null);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#add(com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public boolean add(IBranchGraph graph) {
        boolean added = false;
        if (graph != null) {
            synchronized (this.graphs) {
                if (!graphs.containsKey(graph.getId())) {
                    this.graphs.put(graph.getId(), graph);
                    added = true;
                }
            }
        }
        return added;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#getGraph(java.lang.String)
     */
    public IBranchGraph getGraph(String id) {
        return id != null ? this.graphs.get(id) : null;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#remove(com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public boolean remove(IBranchGraph graph) {
        return graph != null
                ? this.graphs.remove(graph.getId()) != null
                : false;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#getGraphs()
     */
    public IBranchGraph[] getGraphs() {
        return this.graphs.values().toArray(
                new IBranchGraph[this.graphs.size()]);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#size()
     */
    public int size() {
        return this.graphs.size();
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#setGraphs(com.perforce.team.core.mergequest.model.IBranchGraph[])
     */
    public void setGraphs(IBranchGraph[] graphs) {
        this.graphs.clear();
        if (graphs != null && graphs.length > 0) {
            for (IBranchGraph graph : graphs) {
                add(graph);
            }
        }
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#getGraphByName(java.lang.String)
     */
    public IBranchGraph getGraphByName(String name) {
        IBranchGraph graph = null;
        if (name != null) {
            for (IBranchGraph potential : getGraphs()) {
                if (name.equals(potential.getName())) {
                    graph = potential;
                    break;
                }
            }
        }
        return graph;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IBranchGraphContainer#importGraph(com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    public IBranchGraph importGraph(IBranchGraph graph) {
        IBranchGraph newGraph = null;
        if (graph != null) {
            String id = graph.getId();
            // Generate new id if current id is in use
            if (this.graphs.containsKey(id)) {
                id = null;
            }
            newGraph = createGraph(id);
            newGraph.setName(graph.getName());
            newGraph.setConnection(graph.getConnection());
            for (IBranchGraphElement element : graph.getElements()) {
                if (graph.remove(element)) {
                    newGraph.add(element);
                }
            }
            newGraph.updateFactories();
            if (!add(newGraph)) {
                newGraph = null;
            }
        }
        return newGraph;
    }

}
