/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.builder;

import com.perforce.team.core.mergequest.builder.xml.BranchBuilder;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchGraph;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchBuilderTest extends BaseElementBuilderTest {

    /**
     * Test builder
     * 
     * @throws ParserConfigurationException
     */
    public void testBuilder() throws ParserConfigurationException {
        BranchGraph graph1 = new BranchGraph("1");
        BranchBuilder builder = new BranchBuilder();
        Branch branch = graph1.createBranch(null);
        assertNotNull(branch);
        branch.setLocation(10, 10);
        branch.setSize(100, 200);
        branch.setName("test");
        Element element = save(builder, branch);

        BranchGraph graph2 = new BranchGraph("1");
        Branch created = (Branch) builder.initialize(element, graph2);
        assertNotNull(created);
        builder.complete(created, graph2);

        assertEquals(branch, created);
        assertNotSame(branch, created);

        assertEquals(branch.getX(), created.getX());
        assertEquals(branch.getY(), created.getY());
        assertEquals(branch.getWidth(), created.getWidth());
        assertEquals(branch.getHeight(), created.getHeight());
        assertEquals(branch.getName(), created.getName());
    }
}
