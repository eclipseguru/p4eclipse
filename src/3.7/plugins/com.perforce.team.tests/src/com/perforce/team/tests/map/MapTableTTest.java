/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.map;

import com.perforce.team.core.map.MapTableT;
import com.perforce.team.tests.BaseEnumTest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MapTableTTest extends BaseEnumTest {

    /**
     * @see com.perforce.team.tests.BaseEnumTest#getEnum()
     */
    @Override
    protected Enum<?>[] getEnum() {
        return MapTableT.values();
    }

    /**
     * @see com.perforce.team.tests.BaseEnumTest#valueOf(java.lang.String)
     */
    @Override
    protected Enum<?> valueOf(String name) {
        return MapTableT.valueOf(name);
    }

}
