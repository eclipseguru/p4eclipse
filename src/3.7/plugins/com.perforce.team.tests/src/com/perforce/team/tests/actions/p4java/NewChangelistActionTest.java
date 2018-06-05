/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.NewChangelistAction;
import com.perforce.team.ui.p4java.actions.SyncAction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class NewChangelistActionTest extends ConnectionBasedTestCase {

    /**
     * @throws Exception
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createJob();

        addDepotFile(createConnection().getClient(),
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/about.ini");
    }

    /**
     * Tests the new changelist action
     */
    public void testAction() {
        IP4Changelist createdList = null;
        try {
            Action wrap = Utils.getDisabledAction();
            NewChangelistAction action = new NewChangelistAction();
            action.setAsync(false);
            action.selectionChanged(wrap, new StructuredSelection());
            assertTrue(wrap.isEnabled());

            IP4Connection connection = createConnection();
            connection.getPendingChangelist(0);
            action.selectionChanged(wrap, new StructuredSelection(connection));
            assertTrue(wrap.isEnabled());

            final List<P4Event> events = new ArrayList<P4Event>();
            IP4Listener listener = new IP4Listener() {

                public void resoureChanged(P4Event event) {
                    events.add(event);
                }
				public String getName() {
					return NewChangelistActionTest.this.getClass().getSimpleName();
				}
            };
            P4Workspace.getWorkspace().addListener(listener);
            String description = "new changelist description test1";
            action.runAction(false, description);
            P4Workspace.getWorkspace().removeListener(listener);

            assertTrue(events.size() >= 2);

            P4Event event = events.get(events.size() - 2);
            assertSame(EventType.REFRESHED, event.getType());
            IP4Changelist[] lists = event.getChangelists();
            assertNotNull(lists);
            assertEquals(1, lists.length);

            event = events.get(events.size() - 1);
            assertSame(EventType.CREATE_CHANGELIST, event.getType());
            lists = event.getChangelists();
            assertNotNull(lists);
            assertEquals(1, lists.length);
            createdList = lists[0];
            assertNotNull(lists[0].getDescription());
            assertEquals(description, lists[0].getDescription().trim());
            IP4Resource[] resources = event.getResources();
            assertNotNull(resources);
            assertEquals(1, resources.length);
            assertSame(lists[0], resources[0]);
        } finally {
            if (createdList != null) {
                createdList.delete();
            }
        }
    }

    /**
     * Tests the new changelist action with a job initially part of the list
     */
    public void testActionWithJob() {
        IP4Changelist createdList = null;
        IP4Job fixed = null;
        try {
            Action wrap = Utils.getDisabledAction();
            NewChangelistAction action = new NewChangelistAction();
            action.setAsync(false);
            action.selectionChanged(wrap, new StructuredSelection());
            assertTrue(wrap.isEnabled());

            IP4Connection connection = createConnection();
            action.selectionChanged(wrap, new StructuredSelection(connection));
            assertTrue(wrap.isEnabled());

            IP4Job[] jobs = connection.getJobs(1);
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            fixed = jobs[0];
            assertNotNull(fixed.getId());

            final List<P4Event> events = new ArrayList<P4Event>();
            IP4Listener listener = new IP4Listener() {

                public void resoureChanged(P4Event event) {
                    events.add(event);
                }
				public String getName() {
					return NewChangelistActionTest.this.getClass().getSimpleName()+"2";
				}
            };
            P4Workspace.getWorkspace().addListener(listener);
            String description = "new changelist description test2";
            action.runAction(false, description, null, new IP4Job[] { fixed });
            P4Workspace.getWorkspace().removeListener(listener);

            assertTrue(events.size() >= 2);

            P4Event event = events.get(events.size() - 2);
            assertSame(EventType.CREATE_CHANGELIST, event.getType());
            IP4Changelist[] lists = event.getChangelists();
            assertNotNull(lists);
            assertEquals(1, lists.length);
            createdList = lists[0];
            assertNotNull(lists[0].getDescription());
            assertEquals(description, lists[0].getDescription().trim());
            IP4Resource[] resources = event.getResources();
            assertNotNull(resources);
            assertEquals(1, resources.length);
            assertSame(lists[0], resources[0]);

            event = events.get(events.size() - 1);
            assertSame(EventType.FIXED, event.getType());
            IP4Resource[] eventJobs = event.getResources();
            assertNotNull(eventJobs);
            assertEquals(1, eventJobs.length);
            assertTrue(eventJobs[0] instanceof IP4Job);
            assertEquals(fixed, eventJobs[0]);

            assertNotNull(createdList.getJobs());
            assertEquals(1, createdList.getJobs().length);
            assertEquals(fixed.getId(), createdList.getJobs()[0].getId());

        } finally {
            if (createdList != null) {
                if (fixed != null) {
                    createdList.unfix(fixed);
                }
                createdList.delete();
            }
        }
    }

    /**
     * Test creating a changelist with a file
     */
    public void testActionWithFile() {
        IP4Changelist createdList = null;
        IP4File p4File = null;
        try {
            Action wrap = Utils.getDisabledAction();
            NewChangelistAction action = new NewChangelistAction();
            action.setAsync(false);
            action.selectionChanged(wrap, new StructuredSelection());
            assertTrue(wrap.isEnabled());

            IP4Connection connection = createConnection();

            p4File = connection
                    .getFile("//depot/p08.1/p4-eclipse/com.perforce.team.plugin/about.ini");
            assertNotNull(p4File);
            assertFalse(p4File.isOpened());
            SyncAction sync = new SyncAction();
            sync.setAsync(false);
            sync.selectionChanged(null, new StructuredSelection(p4File));
            sync.run(null);
            assertTrue(p4File.isSynced());
            assertTrue(p4File.getHaveRevision() > 0);
            EditAction edit = new EditAction();
            edit.setAsync(false);
            edit.selectionChanged(null, new StructuredSelection(p4File));
            edit.run(null);
            assertTrue(p4File.openedForEdit());

            action.selectionChanged(wrap, new StructuredSelection(connection));
            assertTrue(wrap.isEnabled());

            final List<P4Event> events = new ArrayList<P4Event>();
            IP4Listener listener = new IP4Listener() {

                public void resoureChanged(P4Event event) {
                    events.add(event);
                }

				public String getName() {
					return NewChangelistActionTest.this.getClass().getSimpleName()+"1";
				}

            };
            P4Workspace.getWorkspace().addListener(listener);
            String description = "new changelist description test3";
            action.runAction(false, description, new IP4File[] { p4File }, null);
            P4Workspace.getWorkspace().removeListener(listener);

            int size = events.size();
            assertTrue(size >= 5);

            P4Event event = events.get(size - 5);
            assertSame(EventType.REFRESHED, event.getType());
            IP4Changelist[] lists = event.getChangelists();
            assertNotNull(lists);
            assertEquals(1, lists.length);

            event = events.get(size - 4);
            assertSame(EventType.CREATE_CHANGELIST, event.getType());
            lists = event.getChangelists();
            assertNotNull(lists);
            assertEquals(1, lists.length);
            createdList = lists[0];
            assertNotNull(lists[0].getDescription());
            assertEquals(description, lists[0].getDescription().trim());
            IP4Resource[] resources = event.getResources();
            assertNotNull(resources);
            assertEquals(1, resources.length);
            assertSame(lists[0], resources[0]);

            event = events.get(size - 3);
            assertSame(EventType.REVERTED, event.getType());
            IP4File[] files = event.getFiles();
            assertNotNull(files);
            assertEquals(1, files.length);
            assertEquals(p4File, files[0]);

            event = events.get(size - 2);
            assertSame(EventType.REFRESHED, event.getType());
            files = event.getFiles();
            assertNotNull(files);
            assertEquals(1, files.length);
            assertEquals(p4File, files[0]);

            event = events.get(size - 1);
            assertSame(EventType.OPENED, event.getType());
            files = event.getFiles();
            assertNotNull(files);
            assertEquals(1, files.length);
            assertEquals(p4File, files[0]);

            assertNotNull(createdList.getFiles());
            assertEquals(1, createdList.getFiles().length);
            assertEquals(p4File, createdList.getFiles()[0]);

        } finally {
            if (p4File != null) {
                p4File.revert();
            }
            if (createdList != null) {
                createdList.delete();
                assertNull(createdList.getChangelist());
            }
        }
    }

    /**
     * Test creating a changelist with a file and a job
     */
    public void testActionWithFileAndJob() {
        IP4Changelist createdList = null;
        IP4Job fixed = null;
        IP4File p4File = null;
        try {
            Action wrap = Utils.getDisabledAction();
            NewChangelistAction action = new NewChangelistAction();
            action.setAsync(false);
            action.selectionChanged(wrap, new StructuredSelection());
            assertTrue(wrap.isEnabled());

            IP4Connection connection = createConnection();

            IP4Job[] jobs = connection.getJobs(1);
            assertNotNull(jobs);
            assertEquals(1, jobs.length);
            fixed = jobs[0];
            assertNotNull(fixed.getId());

            p4File = connection
                    .getFile("//depot/p08.1/p4-eclipse/com.perforce.team.plugin/about.ini");
            assertNotNull(p4File);
            assertFalse(p4File.isOpened());

            SyncAction sync = new SyncAction();
            sync.setAsync(false);
            sync.selectionChanged(null, new StructuredSelection(p4File));
            sync.run(null);
            assertTrue(p4File.isSynced());
            assertTrue(p4File.getHaveRevision() > 0);

            EditAction edit = new EditAction();
            edit.setAsync(false);
            edit.selectionChanged(null, new StructuredSelection(p4File));
            edit.run(null);
            assertTrue(p4File.openedForEdit());

            action.selectionChanged(wrap, new StructuredSelection(connection));
            assertTrue(wrap.isEnabled());

            final List<P4Event> events = new ArrayList<P4Event>();
            IP4Listener listener = new IP4Listener() {

                public void resoureChanged(P4Event event) {
                    events.add(event);
                }

				public String getName() {
					return NewChangelistActionTest.this.getClass().getSimpleName()+"3";
				}

            };
            P4Workspace.getWorkspace().addListener(listener);
            String description = "new changelist description test4";
            action.runAction(false, description, new IP4File[] { p4File },
                    new IP4Job[] { fixed });
            P4Workspace.getWorkspace().removeListener(listener);

            int size = events.size();
            assertTrue(size >= 5);

            P4Event event = events.get(size - 5);
            assertSame(EventType.CREATE_CHANGELIST, event.getType());
            IP4Changelist[] lists = event.getChangelists();
            assertNotNull(lists);
            assertEquals(1, lists.length);
            createdList = lists[0];
            assertNotNull(lists[0].getDescription());
            assertEquals(description, lists[0].getDescription().trim());
            IP4Resource[] resources = event.getResources();
            assertNotNull(resources);
            assertEquals(1, resources.length);
            assertSame(lists[0], resources[0]);

            event = events.get(size - 4);
            assertSame(EventType.REVERTED, event.getType());
            IP4File[] files = event.getFiles();
            assertNotNull(files);
            assertEquals(1, files.length);
            assertEquals(p4File, files[0]);

            event = events.get(size - 3);
            assertSame(EventType.REFRESHED, event.getType());
            files = event.getFiles();
            assertNotNull(files);
            assertEquals(1, files.length);
            assertEquals(p4File, files[0]);

            event = events.get(size - 2);
            assertSame(EventType.OPENED, event.getType());
            files = event.getFiles();
            assertNotNull(files);
            assertEquals(1, files.length);
            assertEquals(p4File, files[0]);

            event = events.get(size - 1);
            assertSame(EventType.FIXED, event.getType());
            IP4Resource[] eventJobs = event.getResources();
            assertNotNull(eventJobs);
            assertEquals(1, eventJobs.length);
            assertTrue(eventJobs[0] instanceof IP4Job);
            assertEquals(fixed, eventJobs[0]);

            assertNotNull(createdList.getFiles());
            assertEquals(1, createdList.getFiles().length);
            assertEquals(p4File, createdList.getFiles()[0]);

            assertNotNull(createdList.getJobs());
            assertEquals(1, createdList.getJobs().length);
            assertEquals(fixed.getId(), createdList.getJobs()[0].getId());

        } finally {
            if (p4File != null) {
                p4File.revert();
            }
            if (createdList != null) {
                if (fixed != null) {
                    createdList.unfix(fixed);
                }
                createdList.delete();
                assertNull(createdList.getChangelist());
            }
        }
    }

}
