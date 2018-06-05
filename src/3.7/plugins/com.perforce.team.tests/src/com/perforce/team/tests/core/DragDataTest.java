/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.views.DragData;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DragDataTest extends P4TestCase {

    /**
     * Tests the drag data object
     */
    public void testDragData() {
        DragData.setConnection(null);
        DragData.setSource(null);
        assertNull(DragData.getSource());
        assertNull(DragData.getConnection());
        IP4Connection connection = new P4Connection(null);
        DragData.setConnection(connection);
        Object source = new Object();
        DragData.setSource(source);
        assertSame(source, DragData.getSource());
        assertSame(connection, DragData.getConnection());
        DragData.clear();
        assertNull(DragData.getSource());
        assertNull(DragData.getConnection());
        assertNotNull(new DragData());
    }

}
