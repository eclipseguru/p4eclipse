/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.patch;

import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.patch.P4PatchUiPlugin;
import com.perforce.team.ui.patch.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BasePatchTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        String p4Path = System.getProperty("p4Path", "p4");
        log("Initialize preferece value: "+IPreferenceConstants.P4_PATH +"="+p4Path);
        P4PatchUiPlugin.getDefault().getPreferenceStore()
                .setValue(IPreferenceConstants.P4_PATH, p4Path);
    }

}
