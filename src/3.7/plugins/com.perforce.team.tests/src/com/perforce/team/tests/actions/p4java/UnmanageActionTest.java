/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.actions.UnmanageAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class UnmanageActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

    /**
     * Test unmanage action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        UnmanageAction unmanage = new UnmanageAction();
        assertFalse(wrap.isEnabled());
        unmanage.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Test unmanage action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        UnmanageAction unmanage = new UnmanageAction();
        unmanage.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());
        assertNotNull(P4Workspace.getWorkspace().getConnection(project));
        unmanage.run(wrap);
        assertNull(P4Workspace.getWorkspace().getConnection(project));
        try {
            assertFalse(PerforceSubscriber.getSubscriber()
                    .isSupervised(project));
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
        PerforceProviderPlugin.manageProject(project, parameters);
        assertNotNull(P4Workspace.getWorkspace().getConnection(project));
        try {
            assertTrue(PerforceSubscriber.getSubscriber().isSupervised(project));
        } catch (TeamException e) {
            assertFalse("Team exception thrown", true);
        }
    }

}
