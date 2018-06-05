/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.ShareProjectsAction;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShareProjectsActionTest extends ConnectionBasedTestCase {

    /**
     * Test sharing a project with a connection
     */
    public void testShare() {
        IProject unshared = ResourcesPlugin.getWorkspace().getRoot()
                .getProject("unshared_project");
        assertFalse(unshared.exists());
        try {
            try {
                unshared.create(new NullProgressMonitor());
                unshared.open(new NullProgressMonitor());
            } catch (CoreException e) {
                handle(e);
            }
            assertTrue(unshared.exists());
            assertNull(PerforceTeamProvider.getPerforceProvider(unshared));
            assertNull(P4ConnectionManager.getManager().getConnection(unshared));
            ShareProjectsAction share = new ShareProjectsAction();
            share.setAsync(false);
            IP4Connection connection = createConnection();
            share.shareProjects(connection, new IProject[] { unshared });
            assertNotNull(PerforceTeamProvider.getPerforceProvider(unshared));
            assertEquals(connection, P4ConnectionManager.getManager()
                    .getConnection(unshared));
        } finally {
            try {
                unshared.delete(true, new NullProgressMonitor());
            } catch (CoreException e) {
            }
        }
    }

    /**
     * Test share project action enablement
     */
    public void testEnablement() {
        IP4Connection connection = createConnection();
        assertFalse(connection.isOffline());
        Action wrap = Utils.getDisabledAction();
        ShareProjectsAction share = new ShareProjectsAction();
        share.selectionChanged(wrap, new StructuredSelection(connection));
        assertTrue(wrap.isEnabled());
        connection.setOffline(true);
        assertTrue(connection.isOffline());
        share.selectionChanged(wrap, new StructuredSelection(connection));
        assertFalse(wrap.isEnabled());
    }
}
