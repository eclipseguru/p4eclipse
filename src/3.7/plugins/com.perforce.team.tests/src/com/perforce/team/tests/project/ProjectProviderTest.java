/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.project;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.PerforceProjectProviderType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ProjectProviderTest extends P4TestCase {

    /**
     * Basic test of perforc project provider type
     */
    public void testProvider() {
        PerforceProjectProviderType type = new PerforceProjectProviderType();
        assertNotNull(type.getProjectSetCapability());
    }

}
