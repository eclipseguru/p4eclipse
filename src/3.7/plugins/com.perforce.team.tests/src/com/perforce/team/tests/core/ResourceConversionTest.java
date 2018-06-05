/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;

import org.eclipse.core.resources.IContainer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ResourceConversionTest extends ProjectBasedTestCase {

    /**
     * Test folder lookup
     */
    public void testContainer() {
        IContainer container = PerforceProviderPlugin.getFolderForPath(project
                .getLocation());
        assertNotNull(container);
        assertEquals(project, container);
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/com.perforce.team.plugin";
    }

}
