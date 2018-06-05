/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.map;

import com.perforce.team.core.map.MapCharClass;
import com.perforce.team.tests.BaseEnumTest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MapCharClassTest extends BaseEnumTest {

    /**
     * @see com.perforce.team.tests.BaseEnumTest#getEnum()
     */
    @Override
    protected Enum<?>[] getEnum() {
        return MapCharClass.values();
    }

    /**
     * @see com.perforce.team.tests.BaseEnumTest#valueOf(java.lang.String)
     */
    @Override
    protected Enum<?> valueOf(String name) {
        return MapCharClass.valueOf(name);
    }

}
