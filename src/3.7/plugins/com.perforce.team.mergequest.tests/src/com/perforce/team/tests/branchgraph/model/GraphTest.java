/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.factory.GraphFactory;
import com.perforce.team.core.mergequest.model.factory.IBranchGraphElementFactory;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphTest extends P4TestCase {

    /**
     * Test empty graph
     */
    public void testEmpty() {
        IBranchGraph graph = new BranchGraph("graph1");
        assertTrue(graph.isEmpty());
        assertNotNull(graph.getElements());
        assertNotNull(graph.getBranches());
        assertNotNull(graph.getMappings());
        assertNull(graph.getElementById(null));
        assertNull(graph.getElementById(null, null));
        assertNull(graph.getConnection());
        assertNull(graph.getBranch(null));
        assertNull(graph.getMapping(null));
        assertFalse(graph.containsElement(null));
        assertFalse(graph.containsElement("test"));
        assertEquals(graph, graph.getGraph());
        graph.setGraph(null);
        assertEquals(graph, graph.getGraph());
    }

    /**
     * Test factory creation
     */
    public void testFactory() {
        IBranchGraphElementFactory factory = new GraphFactory();
        IBranchGraph created = (IBranchGraph) factory.create(null, null);
        assertNotNull(created);
        assertNotNull(created.getId());

        created = (IBranchGraph) factory.create("test1", null);
        assertNotNull(created);
        assertEquals("test1", created.getId());

        created = (IBranchGraph) factory.create("grapha", null);
        assertNotNull(created);
        assertEquals("grapha", created.getId());

        assertNotNull(created.createBranch(null));
        assertNotNull(created.createBranchSpecMapping(null));
        assertNotNull(created.createDepotPathMapping(null));
    }
}
