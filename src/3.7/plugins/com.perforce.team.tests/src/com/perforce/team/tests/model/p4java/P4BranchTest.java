/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Branch;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4BranchTest extends ConnectionBasedTestCase {

    /**
     * Basic empty branch test
     */
    public void testEmpty() {
        IP4Connection connection = createConnection();
        IP4Branch branch = new P4Branch(connection, null, true);
        assertTrue(branch.needsRefresh());
        assertNull(branch.getDescription());
        assertNull(branch.getView());
        assertNull(branch.getName());
        assertNull(branch.getOwner());
        assertNull(branch.getUpdateTime());
        assertNull(branch.getAccessTime());
        assertNull(branch.getActionPath());
        assertNull(branch.getActionPath(Type.REMOTE));
        assertNull(branch.getLocalPath());
        assertNull(branch.getClientPath());
        assertNull(branch.getRemotePath());
        assertFalse(branch.isContainer());
        assertEquals(connection, branch.getParent());
        assertNotNull(branch.getClient());
        assertFalse(branch.isLocked());
        assertEquals(connection, branch.getConnection());
        assertNull(branch.getView());
        assertNotNull(branch.getInterchanges());
        assertNotNull(branch.getInterchanges(true));
        assertNotNull(branch.getDiffs());
        try {
            branch.refresh(-1);
            branch.refresh();
            branch.refresh(1);
        } catch (Throwable t) {
            handle(t);
        }
    }
}
