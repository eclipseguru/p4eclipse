/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceStatus;
import com.perforce.team.tests.P4TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceStatusTest extends P4TestCase {

    /**
     * Test the perforce status class
     */
    public void testStatus() {
        Exception exception = new Exception();
        PerforceStatus status = new PerforceStatus(IStatus.ERROR,
                TeamException.UNABLE, "Error message", exception);
        assertTrue(status.isMultiStatus());
        assertFalse(status.isOK());
        assertEquals(PerforceProviderPlugin.ID, status.getPlugin());
        for (IStatus child : status.getChildren()) {
            assertEquals(exception, child.getException());
            assertEquals(IStatus.ERROR, child.getSeverity());
            assertEquals(TeamException.UNABLE, child.getCode());
            assertEquals(PerforceProviderPlugin.ID, child.getPlugin());
        }
    }

}
