/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.IRefreshEvent;
import org.eclipse.team.internal.ui.synchronize.IRefreshSubscriberListener;
import org.eclipse.team.internal.ui.synchronize.RefreshParticipantJob;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantReference;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.TeamSynchronizeAction;
import com.perforce.team.ui.synchronize.PerforceSynchronizeParticipant;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TeamSynchronizeActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("p4eclipse.properties"));
    }

    /**
     * Tests the action enablement
     */
    public void testEnablement() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        connection.setOffline(true);
        assertTrue(connection.isOffline());

        Action wrap = Utils.getDisabledAction();
        TeamSynchronizeAction action = new TeamSynchronizeAction();
        action.selectionChanged(wrap, new StructuredSelection(project));
        assertFalse(wrap.isEnabled());

        connection.setOffline(false);
        assertFalse(connection.isOffline());

        action.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());
    }

    private void clearParticipants() {
        ISynchronizeManager manager = TeamUI.getSynchronizeManager();
        ISynchronizeParticipantReference[] refs = manager
                .getSynchronizeParticipants();
        List<ISynchronizeParticipant> removals = new ArrayList<ISynchronizeParticipant>();
        for (ISynchronizeParticipantReference ref : refs) {
            ISynchronizeParticipant p;
            try {
                p = ref.getParticipant();
                if (!p.isPinned())
                    removals.add(p);
            } catch (TeamException e) {
            }
        }
        ISynchronizeParticipant[] toRemove = removals
                .toArray(new ISynchronizeParticipant[removals.size()]);
        manager.removeSynchronizeParticipants(toRemove);
        assertEquals(0, manager.getSynchronizeParticipants().length);
    }

    /**
     * Tests the synchronize action
     */
    public void testAction() {
        clearParticipants();

        IFile file = project.getFile("p4eclipse.properties");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        IP4File p4File = (IP4File) resource;

        P4Collection collection = new P4Collection(new IP4Resource[] { p4File });
        collection.revert();
        collection.sync(new NullProgressMonitor());
        collection.edit();

        assertTrue(p4File.isOpened());
        assertTrue(p4File.isSynced());

        PerforceSynchronizeParticipant perforceParticipant = (PerforceSynchronizeParticipant) SubscriberParticipant
                .getMatchingParticipant(PerforceSynchronizeParticipant.ID,
                        new IResource[] { file });
        assertNull(perforceParticipant);

        Action wrap = Utils.getDisabledAction();
        TeamSynchronizeAction action = new TeamSynchronizeAction();
        action.setAsync(false);
        action.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());

        // Suppress question dialog about switching perspectives
        TeamUIPlugin
                .getPlugin()
                .getPreferenceStore()
                .setValue(IPreferenceIds.SYNCHRONIZING_COMPLETE_PERSPECTIVE,
                        MessageDialogWithToggle.NEVER);
        final int[] rc = new int[] { 0 };
        IRefreshSubscriberListener listener = new IRefreshSubscriberListener() {

            public void refreshStarted(IRefreshEvent event) {
            }

            public IWorkbenchAction refreshDone(IRefreshEvent event) {
                rc[0] = 1;
                return null;
            }

        };
        RefreshParticipantJob.addRefreshListener(listener);
        action.run(wrap);
        Utils.sleep(.1);
        try {
            IJobManager manager = Job.getJobManager();
            manager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
            manager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
            manager.join(RefreshParticipantJob.getFamily(), null);
        } catch (OperationCanceledException e) {
        } catch (InterruptedException e) {
        }
        RefreshParticipantJob.removeRefreshListener(listener);

        assertEquals(1, rc[0]);

        perforceParticipant = (PerforceSynchronizeParticipant) SubscriberParticipant
                .getMatchingParticipant(PerforceSynchronizeParticipant.ID,
                        new IResource[] { file });
        assertNotNull(perforceParticipant);
        assertNotNull(perforceParticipant.getId());
        assertNotNull(perforceParticipant.getImageDescriptor());
        assertNotNull(perforceParticipant.getName());
        assertNotNull(perforceParticipant.getResources());
        assertEquals(1, perforceParticipant.getResources().length);
        assertEquals(file, perforceParticipant.getResources()[0]);
        assertNotNull(perforceParticipant.getSubscriber());
        assertSame(PerforceSubscriber.getSubscriber(),
                perforceParticipant.getSubscriber());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

}
