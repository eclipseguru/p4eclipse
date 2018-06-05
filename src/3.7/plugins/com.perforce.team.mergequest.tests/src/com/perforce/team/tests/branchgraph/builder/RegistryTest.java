/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.builder;

import com.perforce.team.core.mergequest.builder.xml.BuilderRegistry;
import com.perforce.team.core.mergequest.builder.xml.XmlBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RegistryTest extends P4TestCase {

    /**
     * Test registry
     */
    public void testRegistry() {
        BuilderRegistry registry = new BuilderRegistry(
                XmlBranchGraphBuilder.EXTENSION_POINT_ID);
        assertNotNull(registry.getTagNames());
        assertTrue(registry.getTagNames().length > 0);
        for (String name : registry.getTagNames()) {
            assertNotNull(name);
            assertNotNull(registry.getBuilder(name));
        }
        assertNotNull(registry.getBuilder(new Branch("test")));
        assertNotNull(registry.getBuilder(new DepotPathMapping("test")));
        assertNotNull(registry.getBuilder(new BranchSpecMapping("test")));

    }
}
