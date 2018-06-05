/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.search;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.search.query.DepotPath;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathTest extends P4TestCase {

    /**
     * Test depot path
     */
    public void testPath() {
        DepotPath path = new DepotPath("//test");
        assertEquals("//test", path.getLabel(path));
        assertNotNull(path.getImageDescriptor(path));
        assertNull(path.getParent(path));
        assertNotNull(path.getChildren(path));
        assertEquals(0, path.getChildren(path).length);
    }

    /**
     * Test equals
     */
    public void testEquals() {
        DepotPath path1 = new DepotPath("//test");

        DepotPath path2 = new DepotPath("//test2");
        assertFalse(path1.equals(path2));

        DepotPath path3 = new DepotPath("//test");
        assertEquals(path1, path3);
    }

}
