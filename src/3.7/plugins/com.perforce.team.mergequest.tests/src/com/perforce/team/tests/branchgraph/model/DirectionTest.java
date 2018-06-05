/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.Mapping.Direction;
import com.perforce.team.tests.BaseEnumTest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DirectionTest extends BaseEnumTest {

    /**
     * @see com.perforce.team.tests.BaseEnumTest#getEnum()
     */
    @Override
    protected Direction[] getEnum() {
        return Direction.values();
    }

    /**
     * @see com.perforce.team.tests.BaseEnumTest#valueOf(java.lang.String)
     */
    @Override
    protected Direction valueOf(String name) {
        return Direction.valueOf(name);
    }

}
