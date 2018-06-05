/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4WorkspaceAsyncTest extends ProjectBasedTestCase {

    /**
     * Tests async resource loading
     */
    public void testAsync() {
        final IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        P4Workspace.getWorkspace().clear();

        final List<P4Event> events = new ArrayList<P4Event>();
        IP4Listener listener = new IP4Listener() {

            public void resoureChanged(P4Event event) {
                IFile[] files = event.getLocalFiles();
                if (files.length > 0 && files[0].equals(file)) {
                    events.add(event);
                }
            }
			public String getName() {
				return P4WorkspaceAsyncTest.this.getClass().getSimpleName();
			}
        };
        P4Workspace.getWorkspace().addListener(listener);
        IP4Resource resource = P4Workspace.getWorkspace()
                .asyncGetResource(file);
        assertNull(resource);

        int seconds = 0;
        while (events.isEmpty() && seconds < 60) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            seconds++;
        }
        P4Workspace.getWorkspace().removeListener(listener);

        assertNotNull(P4Workspace.getWorkspace().asyncGetResource(file));
        assertEquals(1, events.size());
        assertEquals(EventType.ADDED, events.get(0).getType());
        assertEquals(1, events.get(0).getLocalFiles().length);
        assertEquals(file, events.get(0).getLocalFiles()[0]);
    }

    /**
     * 
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
