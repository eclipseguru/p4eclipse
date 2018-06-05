/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.BranchGraphContainer;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.mergequest.model.factory.ContainerFactory;
import com.perforce.team.core.mergequest.model.factory.IContainerFactory;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ContainerTest extends P4TestCase {

    /**
     * Test empty container
     */
    public void testEmpty() {
        IBranchGraphContainer container = new BranchGraphContainer();
        assertNotNull(container.getGraphs());
        assertNull(container.getGraph(null));
        assertNull(container.getGraph("graph1"));
        assertNull(container.getGraphByName(null));
        assertNull(container.getGraphByName("name1"));
        assertEquals(0, container.size());
    }

    /**
     * Test adding a graph to a container
     */
    public void testAdd() {
        IBranchGraphContainer container = new BranchGraphContainer();
        assertFalse(container.add(null));
        IBranchGraph graph = container.createGraph(null);
        assertNotNull(graph);
        assertNotNull(graph.getId());
        assertTrue(container.add(graph));
        assertEquals(graph, container.getGraph(graph.getId()));
        assertEquals(graph, container.getGraphByName(graph.getName()));
    }

    /**
     * Test removing a graph from the container
     */
    public void testRemove() {
        IBranchGraphContainer container = new BranchGraphContainer();
        assertFalse(container.remove(null));
        IBranchGraph graph = container.createGraph(null);
        assertNotNull(graph);
        assertNotNull(graph.getId());
        assertFalse(container.remove(graph));
        assertTrue(container.add(graph));
        assertTrue(container.remove(graph));
    }

    /**
     * Test setting the graphs in a container
     */
    public void testSet() {
        IBranchGraphContainer container = new BranchGraphContainer();
        IBranchGraph graph = container.createGraph(null);
        assertNotNull(graph);

        assertTrue(container.add(graph));
        assertEquals(graph, container.getGraph(graph.getId()));
        assertEquals(1, container.size());
        container.setGraphs(null);
        assertNull(container.getGraph(graph.getId()));
        assertEquals(0, container.size());

        container.setGraphs(new IBranchGraph[] { graph });
        assertEquals(graph, container.getGraph(graph.getId()));
        assertEquals(1, container.size());
    }

    /**
     * Test importing a graph into a container
     */
    public void testImport() {
        IBranchGraphContainer container = new BranchGraphContainer();
        IBranchGraph graph = container.createGraph(null);
        assertNotNull(graph);
        assertTrue(container.add(graph));
        assertEquals(graph, container.getGraph(graph.getId()));
        IBranchGraphContainer container2 = new BranchGraphContainer();
        container2.importGraph(graph);
        assertEquals(graph, container2.getGraph(graph.getId()));
        assertEquals(1, container2.size());
    }

    /**
     * Test importing a graph into a container that already contains it
     */
    public void testImportExisting() {
        IBranchGraphContainer container = new BranchGraphContainer();
        IBranchGraph graph = container.createGraph(null);
        assertNotNull(graph);
        assertTrue(container.add(graph));
        assertEquals(graph, container.getGraph(graph.getId()));
        assertTrue(graph.add(graph.createBranch(null)));
        container.importGraph(graph);
        assertEquals(2, container.size());
    }

    /**
     * Test container factory
     */
    public void testFactory() {
        IContainerFactory factory = new ContainerFactory();
        assertNotNull(factory.create());
    }

}
