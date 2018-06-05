/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.factory.DepotPathMappingFactory;
import com.perforce.team.core.mergequest.model.factory.IBranchGraphElementFactory;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathMappingTest extends BaseMappingTest {

    /**
     * Test empty depot path mapping
     */
    @Override
    public void testEmpty() {
        super.testEmpty();
        DepotPathMapping mapping = (DepotPathMapping) createMapping("m1");
        assertNotNull(mapping.getSourcePath());
        assertNotNull(mapping.getTargetPath());
        assertNotNull(mapping.getSourceContext(null, null));
        assertNotNull(mapping.getTargetContext(null, null));
    }

    /**
     * Test equality
     */
    public void testEquals() {
        DepotPathMapping mapping = new DepotPathMapping("test");
        assertEquals(mapping, mapping);
        assertFalse(mapping.equals(""));
        DepotPathMapping mapping2 = new DepotPathMapping(mapping.getId());
        assertEquals(mapping, mapping2);

        Branch sameId = new Branch(mapping.getId());
        assertFalse(mapping.equals(sameId));
    }

    /**
     * Test depot path mapping factory
     */
    public void testFactory() {
        IBranchGraphElementFactory factory = new DepotPathMappingFactory();
        DepotPathMapping mapping = (DepotPathMapping) factory
                .create(null, null);
        assertNotNull(mapping.getId());
    }

    /**
     * @see com.perforce.team.tests.branchgraph.model.BaseMappingTest#createMapping(java.lang.String)
     */
    @Override
    protected Mapping createMapping(String id) {
        return new DepotPathMapping(id);
    }

}
