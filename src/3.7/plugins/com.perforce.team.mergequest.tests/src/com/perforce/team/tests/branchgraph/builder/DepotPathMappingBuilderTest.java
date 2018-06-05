/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.builder;

import com.perforce.team.core.mergequest.builder.xml.DepotPathMappingBuilder;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.DepotPathMapping;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathMappingBuilderTest extends BaseElementBuilderTest {

    /**
     * Test builder
     * 
     * @throws ParserConfigurationException
     */
    public void testBuilder() throws ParserConfigurationException {
        BranchGraph graph1 = new BranchGraph("1");
        assertTrue(graph1.add(graph1.createBranch("b1")));
        assertTrue(graph1.add(graph1.createBranch("b2")));
        DepotPathMappingBuilder builder = new DepotPathMappingBuilder();
        DepotPathMapping mapping = graph1.createDepotPathMapping(null);
        assertNotNull(mapping);
        mapping.setName("test");
        mapping.setSourceId("b1");
        mapping.setTargetId("b2");
        Element element = save(builder, mapping);

        BranchGraph graph2 = new BranchGraph("1");
        assertTrue(graph2.add(graph2.createBranch("b1")));
        assertTrue(graph2.add(graph2.createBranch("b2")));
        DepotPathMapping created = (DepotPathMapping) builder.initialize(
                element, graph2);
        assertNotNull(created);
        builder.complete(created, graph2);

        assertEquals(mapping, created);
        assertNotSame(mapping, created);

        assertEquals(mapping.getName(), created.getName());
        assertEquals(mapping.getSourceId(), created.getSourceId());
        assertEquals(mapping.getTargetId(), created.getTargetId());
    }

}
