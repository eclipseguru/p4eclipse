/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.branchgraph.model;

import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.tests.BaseEnumTest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangeTypeTest extends BaseEnumTest {

    /**
     * @see com.perforce.team.tests.BaseEnumTest#getEnum()
     */
    @Override
    protected ChangeType[] getEnum() {
        return ChangeType.values();
    }

    /**
     * @see com.perforce.team.tests.BaseEnumTest#valueOf(java.lang.String)
     */
    @Override
    protected ChangeType valueOf(String name) {
        return ChangeType.valueOf(name);
    }

}
