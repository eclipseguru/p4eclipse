/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ConnectionMappedException;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;

import org.eclipse.core.runtime.IStatus;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConnectionMappedExceptionTest extends ProjectBasedTestCase {

    /**
     * Tests a valid exception being thrown
     */
    public void testMapped() {
        try {
            P4Workspace.getWorkspace().getConnection(project);
            P4Workspace.getWorkspace().removeConnection(parameters);
        } catch (ConnectionMappedException cme) {
            assertNotNull(cme.getStatus());
            assertTrue(cme.getStatus().isMultiStatus());
            assertEquals(IStatus.ERROR, cme.getStatus().getSeverity());
            assertNotNull(cme.getStatus().getMessage());
            assertNotNull(cme.getStatus().getChildren());
            assertEquals(cme.getStatus().getChildren().length, 2);
            return;
        }
        assertFalse("Connection mapped exception not thrown", true);
    }

    /**
     * Tests creation of an empty exception
     */
    public void testEmpty() {
        ConnectionMappedException exception = new ConnectionMappedException(
                null);
        assertNotNull(exception.getStatus());
        assertEquals(IStatus.OK, exception.getStatus().getSeverity());
        assertNotNull(exception.getStatus().getMessage());
        assertEquals(PerforceProviderPlugin.ID, exception.getStatus()
                .getPlugin());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
