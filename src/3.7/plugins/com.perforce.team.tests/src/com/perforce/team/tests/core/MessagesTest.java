/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MessagesTest extends P4TestCase {

    /**
     * Test {@link com.perforce.p4api.Messages}
     */
    public void testAPIMessages() {
        String found = com.perforce.p4api.Messages.IChangelist_DESCR;
        assertNotNull(found);
        assertTrue(found.length() > 0);
    }

}
