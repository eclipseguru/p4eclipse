/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.Mapping;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchSpecMappingTest extends BaseMappingTest {

    /**
     * @see com.perforce.team.tests.branchgraph.model.BaseMappingTest#createMapping(java.lang.String)
     */
    @Override
    protected Mapping createMapping(String id) {
        return new BranchSpecMapping(id);
    }

    /**
     * @see com.perforce.team.tests.branchgraph.model.BaseMappingTest#testEmpty()
     */
    @Override
    public void testEmpty() {
        super.testEmpty();
        Mapping mapping = createMapping("m1");
        assertNull(mapping.getSourceContext(null, null));
        assertNull(mapping.getTargetContext(null, null));
    }

    /**
     * Test equality
     */
    public void testEquals() {
        BranchSpecMapping mapping = new BranchSpecMapping("test");
        assertEquals(mapping, mapping);
        assertFalse(mapping.equals(""));
        BranchSpecMapping mapping2 = new BranchSpecMapping(mapping.getId());
        assertEquals(mapping, mapping2);

        Branch sameId = new Branch(mapping.getId());
        assertFalse(mapping.equals(sameId));
    }

}
