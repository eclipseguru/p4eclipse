/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.OpenAction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class OpenActionTest extends ProjectBasedTestCase {

    /**
     * Create the files to open
     * 
     * @return - files to open
     */
    protected abstract IFile[] createFiles();

    /**
     * Validate the state after performing the action
     * 
     * @param file
     */
    protected abstract void validateState(IP4File file);

    /**
     * Create the action to use
     * 
     * @return - open action
     */
    protected abstract OpenAction createAction();

    /**
     * Get the selection to run the action with. Returning null will signal to
     * use the result of {@link #createFiles()}
     * 
     * @return - selection or null
     */
    protected abstract IStructuredSelection getSelection();

    /**
     * Tests the delete action with 3 files in a project
     */
    public void testAction() {
        final List<P4Event> events = new ArrayList<P4Event>();
        IP4Listener listener = new IP4Listener() {

            public void resoureChanged(P4Event event) {
                if (event.getType() == EventType.OPENED) {
                    events.add(event);
                }
                if (!events.isEmpty()) {
                    events.add(event);
                }
            }
			public String getName() {
				return OpenActionTest.this.getClass().getSimpleName();
			}
        };

        IFile[] files = createFiles();
        for (IFile file : files) {
            assertTrue(file.exists());
            P4Workspace.getWorkspace().getResource(file);
        }

        Action wrap = Utils.getDisabledAction();
        OpenAction action = createAction();
        action.setAsync(false);
        IStructuredSelection selection = getSelection();
        if (selection == null) {
            selection = new StructuredSelection(files);
        }
        action.selectionChanged(wrap, selection);
        assertTrue(wrap.isEnabled());

        P4Workspace.getWorkspace().addListener(listener);
        action.run(wrap);
        P4Workspace.getWorkspace().removeListener(listener);

        for (IFile file : files) {
            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            validateState((IP4File) resource);
        }

        assertEquals(2, events.size());
        for (P4Event event : events) {
            assertEquals(files.length, event.getFiles().length);
            assertEquals(files.length, event.getResources().length);
            assertEquals(files.length, event.getLocalFiles().length);
            assertEquals(0, event.getLocalContainers().length);
            assertEquals(1, event.getCommonConnections().length);
            assertTrue(event.getContainers().length > 0);
            assertEquals(3, event.getLocalResources().length);
            assertTrue(event.getType() == EventType.ADDED
                    || event.getType() == EventType.REFRESHED
                    || event.getType() == EventType.OPENED);
        }
    }
}
