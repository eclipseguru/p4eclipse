/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.diff;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.diff.DiffRegistry;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffRegistryTest extends P4TestCase {

    /**
     * Test diff extension point registry
     */
    public void testRegistry() {
        DiffRegistry registry = DiffRegistry.getRegistry();
        assertNotNull(registry);
        assertNotNull(registry.getContentTypes());
        assertTrue(registry.getContentTypes().length > 0);
        for (String type : registry.getContentTypes()) {
            assertNotNull(registry.getDiffer(type));
            assertNotNull(registry.getDiffer(type).getDiff(null));
        }
    }

}
