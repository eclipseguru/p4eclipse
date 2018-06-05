/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.builder;

import com.perforce.team.core.mergequest.builder.FileBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.tests.Utils;
import com.perforce.team.tests.branchgraph.BranchGraphTestPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileBuilderTest extends BaseContainerBuilderTest {

    /**
     * Test sample branch graph file
     * 
     * @throws Exception
     */
    public void testLoadFile() throws Exception {
        String file = Utils.getBundlePath(BranchGraphTestPlugin.getDefault()
                .getBundle(), "/resources/sample.xml");

        FileBranchGraphBuilder builder = new FileBranchGraphBuilder(file);
        assertNotNull(builder.getContainerFactory());
        IBranchGraphContainer container = builder.load();
        assertNotNull(container);
        IBranchGraph[] graphs = container.getGraphs();
        assertNotNull(graphs);
        assertEquals(1, graphs.length);
        IBranchGraph graph = graphs[0];
        assertNotNull(graph.getId());
        assertNotNull(graph.getName());

        Branch[] branches = graph.getBranches();
        assertNotNull(branches);
        assertTrue(branches.length > 0);
        for (Branch branch : branches) {
            assertNotNull(branch.getId());
            assertNotNull(branch.getName());
            assertEquals(graph, branch.getGraph());
            assertTrue(branch.getMappingCount() > 0);
        }

        Mapping[] mappings = graph.getMappings();
        assertNotNull(mappings);
        assertTrue(mappings.length > 0);
        for (Mapping mapping : mappings) {
            assertNotNull(mapping.getId());
            assertNotNull(mapping.getName());
            assertEquals(graph, mapping.getGraph());
            assertNotNull(mapping.getSource());
            assertNotNull(mapping.getTarget());
        }

    }
}
