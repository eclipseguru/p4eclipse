/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.patch;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.patch.P4PatchUiPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PluginTest extends P4TestCase {

    /**
     * Basic ui plugin assertion
     */
    public void testUiPlugin() {
        assertNotNull(P4PatchUiPlugin.getDefault());
        assertNull(P4PatchUiPlugin.getDescriptor("bad/path"));
    }

}
