/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.folder;

import com.perforce.team.core.folder.P4CoreFolderPlugin;
import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PluginTest extends P4TestCase {

    /**
     * Basic core plugin assertion
     */
    public void testCorePlugin() {
        assertNotNull(P4CoreFolderPlugin.getDefault());
    }

    /**
     * Basic ui plugin assertion
     */
    public void testUiPlugin() {
        assertNotNull(PerforceUiFolderPlugin.getDefault());
        assertNull(PerforceUiFolderPlugin.getDescriptor("bad/path"));
    }

}
