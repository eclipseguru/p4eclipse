/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.p4api;

import com.perforce.p4api.P4APIPlugin;
import com.perforce.p4api.PerforceConstants;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4APITest extends P4TestCase {

    /**
     * Test the p4 pref location in the p4api plugin
     */
    public void testP4Location() {
        assertNotNull(P4APIPlugin.getPlugin().getPreferenceStore()
                .getString(PerforceConstants.PREF_P4_LOCATION));
    }

    /**
     * Basic test of p4api plugin
     */
    public void testP4APIPlugin() {
        assertNotNull(P4APIPlugin.getPlugin());
        assertNotNull(P4APIPlugin.ID);
    }

}
