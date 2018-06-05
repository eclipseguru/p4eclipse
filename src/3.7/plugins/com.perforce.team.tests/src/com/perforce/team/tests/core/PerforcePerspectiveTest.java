/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.tests.P4TestCase;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforcePerspectiveTest extends P4TestCase {

    /**
     * Tests the perforce perspective can open with the proper initial views
     */
    public void testPerspective() {
        try {
            PlatformUI.getWorkbench().showPerspective(
                    "com.perforce.team.ui.perforcePerspective",
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        } catch (WorkbenchException e) {
            assertFalse(true);
        }
        IPerspectiveDescriptor desc = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getPerspective();
        assertEquals("com.perforce.team.ui.perforcePerspective", desc.getId());
        assertNotNull(desc.getDescription());
        assertNotNull(desc.getImageDescriptor());
        assertNotNull(desc.getLabel());
        assertTrue(desc.getLabel().length() > 0);
    }

}
