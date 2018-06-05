/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.search;

import com.perforce.team.core.search.P4CoreSearchPlugin;
import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.search.P4UiSearchPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PluginTest extends P4TestCase {

    /**
     * Basic core plugin assertion
     */
    public void testCorePlugin() {
        assertNotNull(P4CoreSearchPlugin.getDefault());
    }

    /**
     * Basic ui plugin assertion
     */
    public void testUiPlugin() {
        assertNotNull(P4UiSearchPlugin.getDefault());
        assertNull(P4UiSearchPlugin.getDescriptor("bad/path"));
    }

}
